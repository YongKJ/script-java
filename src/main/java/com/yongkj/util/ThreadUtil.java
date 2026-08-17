package com.yongkj.util;

import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * 批量 / 多线程执行工具。
 *
 * <p>相较旧实现的关键改动：
 * <ul>
 *     <li>共享一个线程池（守护线程），不再每次调用都新建/销毁线程（旧实现分块循环时每 100 行建一次池）；</li>
 *     <li>队列有界，满时由提交线程兜底执行（CallerRunsPolicy），形成背压，避免无限排队；</li>
 *     <li>任务异常会被收集并在结束后向上抛出，等待超时也会抛异常，不再静默吞掉；</li>
 *     <li>SXSSFSheet 非线程安全且写单元格是极小任务，涉及 sheet 的方法改为串行执行。</li>
 * </ul>
 */
public class ThreadUtil {

    /** 并行度：核数 * 2，夹在 [8, 64] 区间，适配 IO 密集型批量任务。 */
    private static final int PARALLELISM = Math.max(8, Math.min(64, Runtime.getRuntime().availableProcessors() * 2));
    private static final int QUEUE_CAPACITY = 10_000;
    private static final AtomicInteger THREAD_INDEX = new AtomicInteger();

    /** 共享线程池。守护线程：批处理脚本跑完即可退出，不阻止 JVM 关闭。 */
    private static final ThreadPoolExecutor EXECUTOR = new ThreadPoolExecutor(
            PARALLELISM,
            PARALLELISM,
            0L, TimeUnit.MILLISECONDS,
            new LinkedBlockingQueue<>(QUEUE_CAPACITY),
            new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "thread-util-" + THREAD_INDEX.incrementAndGet());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private ThreadUtil() {
    }

    /**
     * 并行处理列表元素（IO/CPU 密集型通用入口，如 LLM 调用、DB 更新）。
     *
     * @param awaitMinutes 最长等待分钟数，超时抛 {@link RuntimeException}
     */
    public static <T> void executeWithListDataByThreadPool(long awaitMinutes, List<T> lstData, Consumer<T> function) {
        if (lstData == null || lstData.isEmpty()) {
            return;
        }
        CountDownLatch latch = new CountDownLatch(lstData.size());
        AtomicReference<Throwable> firstError = new AtomicReference<>();
        for (T data : lstData) {
            EXECUTOR.execute(() -> {
                try {
                    function.accept(data);
                } catch (Throwable t) {
                    firstError.compareAndSet(null, t);
                } finally {
                    latch.countDown();
                }
            });
        }
        awaitAndCheck(latch, awaitMinutes, firstError);
    }

    /**
     * 按行写入 SXSSFSheet（串行执行）。
     *
     * <p>SXSSFSheet 非线程安全（窗口刷盘、共享字符串表），且写单元格是极小任务，并行开销大于收益，
     * 故串行执行。保留原签名以兼容旧调用，{@code awaitMinutes} 在串行模式下无意义。
     */
    public static <T> void executeWithListDataByThreadPool(long awaitMinutes, SXSSFSheet sheet, List<T> lstData, BiConsumer<SXSSFRow, T> function) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (T data : lstData) {
            function.accept(sheet.createRow(rowIndex++), data);
        }
    }

    /**
     * 按行写入 SXSSFSheet 的 [startIndex, endIndex) 区间（串行执行），理由同上。
     */
    public static void executeWithListDataByThreadPool(long awaitMinutes, SXSSFSheet sheet, int startIndex, int endIndex, BiConsumer<SXSSFRow, Integer> function) {
        int rowIndex = sheet.getLastRowNum() + 1;
        for (int index = startIndex; index < endIndex; index++, rowIndex++) {
            function.accept(sheet.createRow(rowIndex), index);
        }
    }

    private static void awaitAndCheck(CountDownLatch latch, long awaitMinutes, AtomicReference<Throwable> firstError) {
        boolean finished;
        try {
            finished = latch.await(awaitMinutes, TimeUnit.MINUTES);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("多线程执行被中断！", e);
        }
        if (!finished) {
            throw new RuntimeException("多线程执行超时（超过 " + awaitMinutes + " 分钟）！");
        }
        Throwable t = firstError.get();
        if (t != null) {
            if (t instanceof RuntimeException) {
                throw (RuntimeException) t;
            }
            if (t instanceof Error) {
                throw (Error) t;
            }
            throw new RuntimeException("数据处理异常！", t);
        }
    }
}
