package org.jeecg.common.util.superSearch;

import org.jeecg.common.util.oConvertUtils;

/**
 * Query
 * @Author Scott
 * @Date 2019 02 14
 */
public enum QueryRuleEnum {

    GT(">","  "),
    GE(">=","    "),
    LT("<","  "),
    LE("<=","    "),
    EQ("=","  "),
    NE("!=","   "),
    IN("IN","  "),
    LIKE("LIKE","   "),
    LEFT_LIKE("LEFT_LIKE","   "),
    RIGHT_LIKE("RIGHT_LIKE","   "),
    SQL_RULES("EXTEND_SQL","   SQL  ");

    private String value;

    private String msg;

    QueryRuleEnum(String value, String msg){
        this.value = value;
        this.msg = msg;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public static QueryRuleEnum getByValue(String value){
    	if(oConvertUtils.isEmpty(value)) {
    		return null;
    	}
        for(QueryRuleEnum val :values()){
            if (val.getValue().equals(value)){
                return val;
            }
        }
        return  null;
    }
}
