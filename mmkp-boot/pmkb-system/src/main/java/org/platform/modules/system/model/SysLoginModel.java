package org.platform.modules.system.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 *
 * @Author scott
 * @since  2019-01-18
 */
@ApiModel(value="username", description="username")
public class SysLoginModel {
	@ApiModelProperty(value = "username")
    private String username;
	@ApiModelProperty(value = "password")
    private String password;
	@ApiModelProperty(value = "captcha")
    private String captcha;
	@ApiModelProperty(value = "checkKey")
    private String checkKey;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCaptcha() {
        return captcha;
    }

    public void setCaptcha(String captcha) {
        this.captcha = captcha;
    }

	public String getCheckKey() {
		return checkKey;
	}

	public void setCheckKey(String checkKey) {
		this.checkKey = checkKey;
	}
    
}