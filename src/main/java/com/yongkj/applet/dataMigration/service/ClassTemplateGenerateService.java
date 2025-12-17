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

//        generateQuestionnaireTemplate();
//        generateFoodTemplate();
//        generateDeviceContactTemplate();
//        generateFoodTemplateLatest();
//        generateDeviceIotCardTemplateLatest();
//        generateQuestionnaireTemplateLatest();
//        generateFoodTemplatePlus();
//        generateFoodTemplatePlusLatest();
//        generateFoodTemplateMax();
//        generateEcgRecordReportTemplate();
//        generateMedicineTemplate();
//        generateOrderEntityExtTemplate();
//        generateMaxComputeTemplate();
//        generateSportsTemplate();
        generateAuthorizeTemplate();
    }

    private void generateAuthorizeTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\authorize";

        List<String> tableNames = Arrays.asList(
//                "device_patient_authorize_code",
//                "rel_device_patient_authorize_code_shop",
                "rel_device_patient_authorize_shop"
        );

        generateTemplate("service-user", tableNames, folder, "authorizeCode");
    }

    private void generateSportsTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\sports";

        List<String> tableNames = Arrays.asList(
                "sports_classification",
                "sports_records"
        );

        generateTemplate("service-smart-care", tableNames, folder, "sports");
    }

    private void generateMaxComputeTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\max-compute";

        List<String> tableNames = Arrays.asList(
                "dws_device_usage_statistics",
                "dwd_device_usage_tracking"
        );

        this.desDatabase = this.preDatabaseMaxCompute;
        generateTemplate("service-warehouse", tableNames, folder, "deviceUsage");
    }

    private void generateOrderEntityExtTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\order-entity-ext";

        List<String> tableNames = Arrays.asList(
                "order_entity_ext"
        );

        generateTemplate("service-order", tableNames, folder, "order");
    }

    private void generateMedicineTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\medicine";

        List<String> tableNames = Arrays.asList(
                "medicine_disease",
                "medicine_national_drug",
                "medicine_surgical_procedure"
        );

        generateTemplate("medicine", tableNames, folder, "medicine");
    }

    private void generateEcgRecordReportTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\ecg-record-report";

        List<String> tableNames = Collections.singletonList(
                "ecg_record_report"
        );

        generateTemplate("service-smart-care", tableNames, folder, "ecg");
    }

    private void generateFoodTemplateMax() {
        String folder = "C:\\Users\\Admin\\Desktop\\food-class-max";

        List<String> tableNames = Collections.singletonList(
                "food_cookbook_details_search"
        );

        generateTemplate("food", tableNames, folder, "food");
    }

    private void generateFoodTemplatePlusLatest() {
        String folder = "C:\\Users\\Admin\\Desktop\\food-class-plus-latest";

        List<String> tableNames = Collections.singletonList(
                "food_cookbook_class_history"
        );

        generateTemplate("food", tableNames, folder, "food");
    }

    private void generateFoodTemplatePlus() {
        String folder = "C:\\Users\\Admin\\Desktop\\food-class-plus";

        List<String> tableNames = Arrays.asList(
                "food_cookbook_tag",
                "rel_food_cookbook_tag",
                "food_cookbook",
                "food_cookbook_process",
                "food_cookbook_class",
                "rel_food_cookbook_details"
        );

        generateTemplate("food", tableNames, folder, "food");
    }

    private void generateQuestionnaireTemplateLatest() {
        String folder = "C:\\Users\\Admin\\Desktop\\questionnaire-risk-class-latest";
        if (!FileUtil.exist(folder)) {
            FileUtil.mkdir(folder);
        }

        List<String> tableNames = Arrays.asList(
//                "rel_property_questionnaire_sheet",
//                "questionnaire_sheet_property",
//                "rel_risk_questionnaire_sheet",
//                "questionnaire_risk",
//                "questionnaire_risk_suggestion",
                "rel_risk_questionnaire_score_criteria"
        );

        generateTemplate("questionnaire", tableNames, folder, "questionnaire");
    }

    private void generateDeviceIotCardTemplateLatest() {
        String folder = "C:\\Users\\Admin\\Desktop\\device-iot-card-latest";
        if (!FileUtil.exist(folder)) {
            FileUtil.mkdir(folder);
        }

        List<String> tableNames = Arrays.asList(
                "rel_iot_card_device"
        );

        generateTemplate("contact", tableNames, folder, "contact");
    }

    private void generateDeviceIotCardTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\device-iot-card";
        if (!FileUtil.exist(folder)) {
            FileUtil.mkdir(folder);
        }

        List<String> tableNames = Arrays.asList(
                "device_iot_card"
        );

        generateTemplate("contact", tableNames, folder, "contact");
    }

    private void generateFoodTemplateLatest() {
        String folder = "C:\\Users\\Admin\\Desktop\\food-class-latest";

        List<String> tableNames = Arrays.asList(
                "food_tag",
                "rel_food_recipes_tag",
                "food_recipes",
                "food_recipes_step",
                "rel_food_recipes_details"
        );

        generateTemplate("food", tableNames, folder, "food");
    }

    private void generateDeviceContactTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\device-contact";

        List<String> tableNames = Arrays.asList(
                "device_contact",
                "device_contact_recycle_bin"
        );

        generateTemplate("contact", tableNames, folder, "contact");
    }

    private void generateFoodTemplate() {
        String folder = "C:\\Users\\Admin\\Desktop\\food-class";

        List<String> tableNames = Arrays.asList(
                "food_nutrition",
                "rel_food_details_nutrition",
                "food_details",
                "food_category"
        );

        generateTemplate("food", tableNames, folder, "food");
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

        generateTemplate("questionnaire", tableNames, folder, "questionnaire");
    }


    private void generateTemplate(String projectName, List<String> tableNames, String folder, String packageName) {
        List<String> classTypes = Arrays.asList(
                "Controller",
                "Service",
                "ServiceImpl",
                "Mapper",
                "PO",
                "DTO",
                "Params",
                "Dict"
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
                String classContent = getClassContent(projectName, table, tableName, classType, packageName);
                if (!FileUtil.exist(classPath)) {
                    FileUtil.create(classPath);
                }
                FileUtil.write(classPath, classContent);
            }
        }
    }

    private String getClassContent(String projectName, Table table, String tableName, String classType, String packageName) {
        String templatePath = "/template/java/" + projectName + "/" + classType + ".txt";
        String classContent = FileUtil.read(templatePath);

        classContent = getClassContent(tableName, classContent, packageName);
        if (Objects.equals(classType, "PO")) {
            classContent = getPOClassContent(table, classContent, projectName);
        }
        return classContent;
    }

    private String getClassContent(String tableName, String classContent, String packageName) {
        String tableHumpName = GenUtil.toHump(tableName.replace("_", "-"));
        String tableHumpNameMini = tableHumpName.substring(0, 1).toLowerCase() + tableHumpName.substring(1);
        String controllerClass = tableHumpName + "Controller";
        String serviceClass = tableHumpName + "Service";
        String serviceName = serviceClass.substring(0, 1).toLowerCase() + serviceClass.substring(1);
        String serviceImplClass = tableHumpName + "ServiceImpl";
        String mapperClass = tableHumpName + "Mapper";
        String poClass = tableHumpName + "PO";
        String dtoClass = tableHumpName + "DTO";
        String paramsClass = tableHumpName + "Params";
        String dictClass = tableHumpName + "Dict";

        Map<String, String> mapType = new HashMap<>();
        mapType.put("TableOriginalName", tableName);
        mapType.put("TableHumpName", tableHumpName);
        mapType.put("TableHumpNameMini", tableHumpNameMini);
        mapType.put("ControllerClass", controllerClass);
        mapType.put("ServiceClass", serviceClass);
        mapType.put("ServiceName", serviceName);
        mapType.put("ServiceImplClass", serviceImplClass);
        mapType.put("MapperClass", mapperClass);
        mapType.put("POClass", poClass);
        mapType.put("DTOClass", dtoClass);
        mapType.put("ParamsClass", paramsClass);
        mapType.put("DictClass", dictClass);
        mapType.put("PackageName", packageName);

        for (Map.Entry<String, String> map : mapType.entrySet()) {
            if (classContent.contains(map.getKey())) {
                classContent = classContent.replace(map.getKey(), map.getValue());
            }
        }

        return classContent;
    }

    private String getPOClassContent(Table table, String classContent, String projectName) {
        String fieldPath = "/template/java/" + projectName + "/Field.txt";
        String fieldContent = FileUtil.read(fieldPath);

        String fieldDeletedPath = "/template/java/" + projectName + "/FieldDeleted.txt";
        String fieldDeletedContent = FileUtil.read(fieldDeletedPath);

        String fieldJsonPath = "/template/java/" + projectName + "/FieldJson.txt";
        String fieldJsonContent = FileUtil.read(fieldJsonPath);

        return getPOClassContent(table, classContent, fieldContent, fieldDeletedContent, fieldJsonContent);
    }

    private String getPOClassContent(Table table, String classContent, String fieldContent, String fieldDeletedContent, String fieldJsonContent) {
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
            fieldHumpName = fieldHumpName.substring(0, 1).toLowerCase() + fieldHumpName.substring(1);
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
