package com.yongkj.applet.dataMigration.service;

import com.yongkj.applet.dataMigration.DataMigration;
import com.yongkj.applet.dataMigration.core.BaseService;
import com.yongkj.applet.dataMigration.pojo.po.Field;
import com.yongkj.applet.dataMigration.pojo.po.Table;
import com.yongkj.util.FileUtil;
import com.yongkj.util.GenUtil;
import org.springframework.util.StringUtils;

import java.util.*;

public class ClassTemplateGenerateService extends BaseService {

    private final boolean enable;

    public ClassTemplateGenerateService(DataMigration dataMigration) {
        super(dataMigration);
        Map<String, Object> maxComputeHistory = GenUtil.getMap("class-template-generate");
        this.enable = GenUtil.objToBoolean(maxComputeHistory.get("enable"));
    }

    public void apply() {
        if (!enable) return;

        generateQuestionnaireTemplate();
    }

    private void generateQuestionnaireTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\java-class";

        List<String> tableNames = Arrays.asList(
                "questionnaire_classification",
                "questionnaire_sheet",
                "questionnaire_question",
                "questionnaire_question_option",
                "questionnaire_score_criteria",
                "questionnaire_score_criteria_option",
                "rel_questionnaire_question_score_criteria",
                "questionnaire_question_option_rel_criteria",
                "rel_questionnaire_sheet_consumer",
                "rel_questionnaire_sheet_consumer_score_criteria"
        );

        List<String> classTypes = Arrays.asList(
                "Controller",
                "Service",
                "ServiceImpl",
                "Mapper",
                "PO",
                "DTO",
                "Params"
        );

        for (String tableName : tableNames) {
            Table table = desDatabase.getMapTable().get(tableName);
            if (table == null) {
                continue;
            }

            for (String classType : classTypes) {
                String dir = folder + "\\" + classType;
                if (!FileUtil.exist(dir)) {
                    FileUtil.mkdir(dir);
                }

                String classPath = getClassPath(dir, tableName, classType);
                String classContent = getClassContent(table, tableName, classType);
                if (!FileUtil.exist(classPath)) {
                    FileUtil.create(classPath);
                }
                FileUtil.write(classPath, classContent);
            }
        }
    }

    private String getClassContent(Table table, String tableName, String classType) {
        String templatePath = "/template/java/questionnaire/" + classType + ".txt";
        String classContent = FileUtil.read(templatePath);

        classContent = getClassContent(tableName, classContent);
        if (Objects.equals(classType, "PO")) {
            classContent = getPOClassContent(table, classContent);
        }
        return classContent;
    }

    private String getClassContent(String tableName, String classContent) {
        String tableHumpName = GenUtil.toHump(tableName.replace("_", "-"));
        String controllerClass = tableHumpName + "Controller";
        String serviceClass = tableHumpName + "Service";
        String serviceName = serviceClass.substring(0, 1).toUpperCase() + serviceClass.substring(1);
        String serviceImplClass = tableHumpName + "ServiceImpl";
        String mapperClass = tableHumpName + "Mapper";
        String poClass = tableHumpName + "PO";
        String dtoClass = tableHumpName + "DTO";
        String paramsClass = tableHumpName + "Params";

        Map<String, String> mapType = new HashMap<>();
        mapType.put("TableOriginalName", tableName);
        mapType.put("TableHumpName", tableHumpName);
        mapType.put("ControllerClass", controllerClass);
        mapType.put("ServiceClass", serviceClass);
        mapType.put("ServiceName", serviceName);
        mapType.put("ServiceImplClass", serviceImplClass);
        mapType.put("MapperClass", mapperClass);
        mapType.put("POClass", poClass);
        mapType.put("DTOClass", dtoClass);
        mapType.put("ParamsClass", paramsClass);

        for (Map.Entry<String, String> map : mapType.entrySet()) {
            if (classContent.contains(map.getKey())) {
                classContent = classContent.replace(map.getKey(), map.getValue());
            }
        }

        return classContent;
    }

    private String getPOClassContent(Table table, String classContent) {
        String fieldPath = "/template/java/questionnaire/Field.txt";
        String fieldContent = FileUtil.read(fieldPath);

        String fieldDeletedPath = "/template/java/questionnaire/FieldDeleted.txt";
        String fieldDeletedContent = FileUtil.read(fieldDeletedPath);

        String fieldJsonPath = "/template/java/questionnaire/FieldJson.txt";
        String fieldJsonContent = FileUtil.read(fieldJsonPath);

        String fieldListStr = "";
        List<String> fieldNames = Arrays.asList("id", "utc_created", "utc_modified");
        for (Map.Entry<String, Field> map : table.getMapField().entrySet()) {
            if (fieldNames.contains(map.getKey())) {
                continue;
            }

            String tempFieldContent = fieldContent;
            if (Objects.equals(map.getKey(), "utc_deleted")) {
                tempFieldContent = fieldDeletedContent;
            }

            String filedType = "";
            String type = map.getValue().getType().toUpperCase();
            switch (type) {
                case "INT":
                case "INT4":
                case "TINYINT":
                    filedType = "Integer";
                    break;
                case "INT8":
                case "BIGINT":
                    filedType = "Long";
                    break;
                case "DOUBLE":
                case "DECIMAL":
                    filedType = "Double";
                    break;
                case "JSON":
                    filedType = "String";
                    tempFieldContent = fieldJsonContent;
                    break;
                default:
                    filedType = type.contains("DECIMAL") ||
                            type.contains("NUMERIC") ? "Double" : "String";
            }

            String fieldHumpName = GenUtil.toHump(map.getKey().replace("_", "-"));
            String fieldNote = map.getValue().getComment();
            if (!StringUtils.hasText(fieldNote)) {
                fieldNote = fieldHumpName;
            }

            tempFieldContent = tempFieldContent.replace("FieldNote", fieldNote);
            tempFieldContent = tempFieldContent.replace("FieldType", filedType);
            tempFieldContent = tempFieldContent.replace("FieldName", fieldHumpName);
            fieldListStr += tempFieldContent;
        }

        return classContent.replace("FieldList", fieldListStr);
    }

    private String getClassPath(String folder, String tableName, String classType) {
        String name = GenUtil.toHump(tableName.replace("_", "-"));
        String fileName = name + classType + ".java";
        return folder + "\\" + fileName;
    }

}
