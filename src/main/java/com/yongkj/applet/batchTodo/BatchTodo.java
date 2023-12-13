package com.yongkj.applet.batchTodo;

import com.yongkj.pojo.dto.Log;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import com.yongkj.util.LogUtil;

import java.io.File;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class BatchTodo {

    private final boolean batchTodoEnable;
    private final List<String> entityFolders;

    private BatchTodo() {
        entityFolders = GenUtil.getList("entity-folders");
        batchTodoEnable = GenUtil.getBoolean("batch-todo-enable");
    }

    private void apply() {
        if (!batchTodoEnable) {
            return;
        }

//        List<String> entityFiles = new ArrayList<>();
//        for (String folder : entityFolders) {
//            entityFiles.addAll(getEntityFiles(folder));
//        }
//
//        for (String entityFile : entityFiles) {
//            batchTodo(entityFile);
//        }
//        LogUtil.loggerLine(Log.of("BatchTodo", "apply", "entityFiles", entityFiles));
        replaceTest();
    }

    private void batchTodo(String codeFile) {
        String content = FileUtil.read(codeFile);

        Map<String, List<String>> mapClassData = getClassDataMap(content);
        LogUtil.loggerLine(Log.of("BatchTodo", "batchTodo", "mapClassData.size()", mapClassData.size()));
        LogUtil.loggerLine(Log.of("BatchTodo", "batchTodo", "mapClassData", mapClassData));

        Map<String, String> mapResult = getResultMap(mapClassData);
        LogUtil.loggerLine(Log.of("BatchTodo", "batchTodo", "mapResult.size()", mapResult.size()));
        LogUtil.loggerLine(Log.of("BatchTodo", "batchTodo", "mapResult", mapResult));

        for (Map.Entry<String, String> map : mapResult.entrySet()) {
            content = content.replace(map.getKey(), map.getValue());
        }

        FileUtil.write(codeFile, content);
    }

    private Map<String, String> getResultMap(Map<String, List<String>> mapClassData) {
        Map<String, String> mapResult = new HashMap<>();
        for (Map.Entry<String, List<String>> map : mapClassData.entrySet()) {
            String classData = map.getKey();

            for (String fileData : map.getValue()) {
                String tempRegStr = "((\r\n[\\s]*?)private[\\s\\S]*?;)";
                Pattern tempPattern = Pattern.compile(tempRegStr);
                Matcher tempMatcher = tempPattern.matcher(fileData);

                while (tempMatcher.find()) {
                    String fieldData = tempMatcher.group(0);
                    String blankData = tempMatcher.group(2);
                    String segFieldData = tempMatcher.group(1);
                    String todoData = String.format("%s//TODO", blankData);

                    String segFieldDataLatest = todoData + segFieldData;
                    String fieldDataLatest = fieldData.replace(segFieldData, segFieldDataLatest);

                    classData = classData.replace(fieldData, fieldDataLatest);

                    LogUtil.loggerLine(Log.of("BatchTodo", "getResultMap", "blankData", blankData));
                    LogUtil.loggerLine(Log.of("BatchTodo", "getResultMap", "segFieldData", segFieldData));
                }

                mapResult.put(map.getKey(), classData);
            }
        }
        return mapResult;
    }

    private Map<String, List<String>> getClassDataMap(String content) {
        String regStr = "class[\\s\\S]*?(\\{([\\s\\S]*?)\\})";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(content);

        Map<String, List<String>> mapClassData = new HashMap<>();
        LogUtil.loggerLine(Log.of("BatchTodo", "getClassDataMap", "matcher.find()", matcher.find()));
        while (matcher.find()) {
            String classData = matcher.group(1);
            String fieldDataStr = matcher.group(2);
            LogUtil.loggerLine(Log.of("BatchTodo", "getClassDataMap", "fieldDataStr", fieldDataStr));

            String tempRegStr = ";?([\\s\\S]*?private[\\s\\S]*?;)";
            Pattern tempPattern = Pattern.compile(tempRegStr);
            Matcher tempMatcher = tempPattern.matcher(fieldDataStr);
            while (tempMatcher.find()) {
                String fieldData = tempMatcher.group(1);
                if (fieldData.contains("@ApiModelProperty")) {
                    continue;
                }
                if (!mapClassData.containsKey(classData)) {
                    mapClassData.put(classData, new ArrayList<>());
                }
                mapClassData.get(classData).add(fieldData);
                LogUtil.loggerLine(Log.of("BatchTodo", "getClassDataMap", "fieldData", fieldData));
            }
            System.out.println("----------------------------------------------------------------------------------------------");
        }
        return mapClassData;
    }

    private List<String> getEntityFiles(String folder) {
        List<File> lstFile = FileUtil.list(folder);
        List<String> entityFiles = new ArrayList<>();
        for (File file : lstFile) {
            String path = folder + File.separator + file.getName();
            if (FileUtil.isFolder(path)) {
                entityFiles.addAll(getEntityFiles(path));
            } else {
                entityFiles.add(path);
            }
        }
        return entityFiles;
    }

    private void replaceTest() {
        String filePath = "D:\\Document\\MyCodes\\Worker\\Codes\\service-user\\service-user-service\\src\\main\\java\\com\\bc\\user\\service\\entity\\params\\worker\\WorkerTypeParams.java";
        String content = FileUtil.read(filePath);
        String tempContent = getTempContent(content);

        List<String> lstContent = Arrays.asList(tempContent.split("\\{"));
        List<String> contents = Arrays.asList(tempContent.split("\\}"));
        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "lstContent", lstContent));
        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "contents", contents));

        String regStr = "\\{([\\s\\S]*?)\\}";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(tempContent);

        Map<String, List<String>> mapClassData = new HashMap<>();
        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "matcher.find()", matcher.find()));
        while (matcher.find()) {
            String classData = matcher.group(0);
            String fieldDataStr = matcher.group(1);
            LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "fieldDataStr", fieldDataStr));

            String tempRegStr = ";?([\\s\\S]*?private[\\s\\S]*?;)";
            Pattern tempPattern = Pattern.compile(tempRegStr);
            Matcher tempMatcher = tempPattern.matcher(fieldDataStr);
            while (tempMatcher.find()) {
                String fieldData = tempMatcher.group(1);
                if (fieldData.contains("@ApiModelProperty")) {
                    continue;
                }
                if (!mapClassData.containsKey(classData)) {
                    mapClassData.put(classData, new ArrayList<>());
                }
                mapClassData.get(classData).add(fieldData);
                LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "fieldData", fieldData));
            }
            System.out.println("----------------------------------------------------------------------------------------------");
        }

        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "mapClassData.size()", mapClassData.size()));
        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "mapClassData", mapClassData));
        Map<String, String> mapResult = new HashMap<>();
        for (Map.Entry<String, List<String>> map : mapClassData.entrySet()) {
            String classData = map.getKey();

            for (String fileData : map.getValue()) {
                String tempRegStr = "((\r\n\\s*?)private[\\s\\S]*?;)";
                Pattern tempPattern = Pattern.compile(tempRegStr);
                Matcher tempMatcher = tempPattern.matcher(fileData);

                while (tempMatcher.find()) {
                    String fieldData = tempMatcher.group(0);
                    String blankData = tempMatcher.group(2);
                    String segFieldData = tempMatcher.group(1);
                    String todoData = String.format("%s//TODO", blankData);

                    String segFieldDataLatest = todoData + segFieldData;
                    String fieldDataLatest = fieldData.replace(segFieldData, segFieldDataLatest);

                    classData = classData.replace(fieldData, fieldDataLatest);

                    LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "blankData", blankData));
                    LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "segFieldData", segFieldData));
                }

                mapResult.put(map.getKey(), classData);
            }
        }

        LogUtil.loggerLine(Log.of("BatchTodo", "replaceTest", "mapResult", mapResult));

        for (Map.Entry<String, String> map : mapResult.entrySet()) {
            content = content.replace(map.getKey(), map.getValue());
        }

        FileUtil.write(filePath, content);
    }

    private String getTempContent(String content) {
        String regStr = "\\{([\\s\\S]*)\\}";
        Pattern pattern = Pattern.compile(regStr);
        Matcher matcher = pattern.matcher(content);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return content;
    }

    public static void run(String[] args) {
        new BatchTodo().apply();
    }

}
