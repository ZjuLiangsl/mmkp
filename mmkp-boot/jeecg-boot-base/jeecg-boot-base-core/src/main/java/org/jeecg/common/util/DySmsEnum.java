package org.jeecg.common.util;

import org.apache.commons.lang3.StringUtils;

public enum DySmsEnum {
	
	LOGIN_TEMPLATE_CODE("SMS_175435174","JEECG","code"),
	FORGET_PASSWORD_TEMPLATE_CODE("SMS_175435174","JEECG","code"),
	REGISTER_TEMPLATE_CODE("SMS_175430166","JEECG","code"),
	/**    */
	MEET_NOTICE_TEMPLATE_CODE("SMS_201480469","H5    ","username,title,minute,time"),
	/**      */
	PLAN_NOTICE_TEMPLATE_CODE("SMS_201470515","H5    ","username,title,time");

	/**
	 *
	 */
	private String templateCode;
	/**
	 *
	 */
	private String signName;
	/**
	 *            ，  key     ，
	 */
	private String keys;
	
	private DySmsEnum(String templateCode,String signName,String keys) {
		this.templateCode = templateCode;
		this.signName = signName;
		this.keys = keys;
	}
	
	public String getTemplateCode() {
		return templateCode;
	}
	
	public void setTemplateCode(String templateCode) {
		this.templateCode = templateCode;
	}
	
	public String getSignName() {
		return signName;
	}
	
	public void setSignName(String signName) {
		this.signName = signName;
	}
	
	public String getKeys() {
		return keys;
	}

	public void setKeys(String keys) {
		this.keys = keys;
	}

	public static DySmsEnum toEnum(String templateCode) {
		if(StringUtils.isEmpty(templateCode)){
			return null;
		}
		for(DySmsEnum item : DySmsEnum.values()) {
			if(item.getTemplateCode().equals(templateCode)) {
				return item;
			}
		}
		return null;
	}
}

