package com.yongkj.util;

import com.yongkj.pojo.dto.TrConsumer;
import org.apache.poi.xssf.streaming.SXSSFCell;
import org.apache.poi.xssf.streaming.SXSSFRow;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class ThreadUtil {

    private ThreadUtil() {

    }

    public static <K, V> void executeWithMapDataByThreadPool(long awaitTime, Map<K, V> mapData, BiConsumer<K, V> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithMapDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(mapData.size());
        try {
            for (Map.Entry<K, V> map : mapData.entrySet()) {
                executor.execute(() -> {
                    try {
                        function.accept(map.getKey(), map.getValue());
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
        }
    }

    public static <T> void executeWithListDataByThreadPool(long awaitTime, SXSSFSheet sheet, List<T> lstData, BiConsumer<SXSSFRow, T> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithListDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(lstData.size());
        try {
            int rowIndex = sheet.getLastRowNum() + 1;
            for (int i = 0; i < lstData.size(); i++, rowIndex++) {
                T data = lstData.get(i);
                SXSSFRow row = sheet.createRow(rowIndex);
                executor.execute(() -> {
                    try {
                        function.accept(row, data);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
        }
    }

    public static <T> void executeWithListDataByThreadPool(long awaitTime, SXSSFSheet sheet, int startIndex, int endIndex, BiConsumer<SXSSFRow, Integer> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithListDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(endIndex - startIndex);
        try {
            int rowIndex = sheet.getLastRowNum() + 1;
            for (int index = startIndex; index < endIndex; index++, rowIndex++) {
                int finalIndex = index;
                SXSSFRow row = sheet.createRow(rowIndex);
                executor.execute(() -> {
                    try {
                        function.accept(row, finalIndex);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
        }
    }

    public static <T> void executeWithListDataByThreadPool(long awaitTime, SXSSFSheet sheet, List<T> lstData, TrConsumer<SXSSFRow, Integer, T> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithListDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(lstData.size());
        try {
            int rowIndex = sheet.getLastRowNum() + 1;
            for (int i = 0; i < lstData.size(); i++, rowIndex++) {
                T data = lstData.get(i);
                int finalRowIndex = rowIndex;
                SXSSFRow row = sheet.createRow(rowIndex);
                executor.execute(() -> {
                    try {
                        function.accept(row, finalRowIndex, data);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
        }
    }

    public static <T> void executeWithListDataByThreadPool(long awaitTime, List<T> lstData, Consumer<T> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithListDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(lstData.size());
        try {
            for (T data : lstData) {
                executor.execute(() -> {
                    try {
                        function.accept(data);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
        }
    }

    public static <K, V> void executeWithMapDataByThreadPool(Map<String, ThreadPoolTaskExecutor> mapExecutor, long awaitTime, Map<K, V> mapData, BiConsumer<K, V> function) {
        ThreadPoolTaskExecutor executor = new ExecutorBuilder().prefix("executeWithMapDataByThreadPool-pool-").build();
        CountDownLatch latch = new CountDownLatch(mapData.size());
        try {
            for (Map.Entry<K, V> map : mapData.entrySet()) {
                executor.execute(() -> {
                    try {
                        function.accept(map.getKey(), map.getValue());
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        } finally {
            executor.shutdown();
            mapExecutor.forEach((k, v) -> v.shutdown());
        }
    }

    public static <T> void executeWithListDataByExecutor(ThreadPoolTaskExecutor executor, long awaitTime, List<T> lstData, Consumer<T> function) {
        CountDownLatch latch = new CountDownLatch(lstData.size());
        try {
            for (T data : lstData) {
                executor.execute(() -> {
                    try {
                        function.accept(data);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        }
    }

    public static <T> void executeWithListDataByExecutor(ThreadPoolTaskExecutor executor, long awaitTime, SXSSFRow row, int colIndex, List<T> lstData, BiConsumer<SXSSFCell, T> function) {
        CountDownLatch latch = new CountDownLatch(lstData.size());
        try {
            for (T data : lstData) {
                SXSSFCell cell = row.createCell(colIndex++);
                executor.execute(() -> {
                    try {
                        function.accept(cell, data);
                    } catch (Exception e) {
                        throw new RuntimeException("数据处理异常！", e);
                    } finally {
                        latch.countDown();
                    }
                });
            }
            latch.await(awaitTime, TimeUnit.MINUTES);
        } catch (Exception e) {
            throw new RuntimeException("多线程执行异常！", e);
        }
    }

}
