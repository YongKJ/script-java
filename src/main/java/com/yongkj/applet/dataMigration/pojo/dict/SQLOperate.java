package com.yongkj.applet.dataMigration.pojo.dict;

public enum SQLOperate {

    and("and"),
    andWrapper("and (%s)"),
    or("or"),
    orWrapper("or (%s)"),

    eq("="),
    ne("!="),
    gt(">"),
    ge(">="),
    lt("<"),
    le("<="),
    in("IN (%s)"),
    notIn("NOT IN (%s)"),
    isNull("IS NULL"),
    isNotNUll("IS NOT NULL"),
    like("LIKE '%%%s%%'"),
    notLike("NOT LIKE '%%%s%%'"),
    likeLeft("LIKE '%%%s'"),
    likeRight("LIKE '%s%%'"),
    between("BETWEEN %s AND %s"),
    notBetween("NOT BETWEEN %s AND %s"),
    groupBy("GROUP BY %s"),
    orderByAsc("ORDER BY %s ASC"),
    orderByDesc("ORDER BY %s DESC"),
    limit("LIMIT %s");

    private final String value;

    SQLOperate(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
