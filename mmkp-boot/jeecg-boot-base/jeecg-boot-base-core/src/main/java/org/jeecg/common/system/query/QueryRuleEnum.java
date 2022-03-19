package org.jeecg.common.system.query;

import org.jeecg.common.util.oConvertUtils;

/**
 * Query
 * @Author Scott
 * @Date 2019-02-14
 */
public enum QueryRuleEnum {

    GT(">","gt","Greater than"),
    GE(">=","ge","Greater than or equal"),
    LT("<","lt","Less than"),
    LE("<=","le","Less than or equal"),
    EQ("=","eq","Equal"),
    NE("!=","ne","Not equal"),
    IN("IN","in","in"),
    LIKE("LIKE","like","like"),
    LEFT_LIKE("LEFT_LIKE","left_like","Left like"),
    RIGHT_LIKE("RIGHT_LIKE","right_like","Right like"),
    EQ_WITH_ADD("EQWITHADD","eq_with_add","eq with add"),
    LIKE_WITH_AND("LIKEWITHAND","like_with_and","like with and"),
    SQL_RULES("USE_SQL_RULES","ext","Customize SQL");

    private String value;
    
    private String condition; 

    private String msg;

    QueryRuleEnum(String value, String condition, String msg){
        this.value = value;
        this.condition = condition;
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

    public String getCondition() {
		return condition;
	}

	public void setCondition(String condition) {
		this.condition = condition;
	}

	public static QueryRuleEnum getByValue(String value){
    	if(oConvertUtils.isEmpty(value)) {
    		return null;
    	}
        for(QueryRuleEnum val :values()){
            if (val.getValue().equals(value) || val.getCondition().equals(value)){
                return val;
            }
        }
        return  null;
    }
}
