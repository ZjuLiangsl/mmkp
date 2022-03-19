package org.jeecg.common.system.vo;

import java.util.Date;

import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * </p>
 *
 * @Author scott
 * @since 2018-12-20
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class LoginUser {

	/**
	 */
	private String id;

	/**
	 */
	private String username;

	/**
	 */
	private String realname;

	/**
	 */
	private String password;

     /**
      */
    private String orgCode;
	/**
	 */
	private String avatar;

	/**
	 */
	@JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd")
	@DateTimeFormat(pattern = "yyyy-MM-dd")
	private Date birthday;

	/**
	 */
	private Integer sex;

	/**
	 */
	private String email;

	/**
	 */
	private String phone;

	/**
	 */
	private Integer status;
	
	private Integer delFlag;
	/**
     */
    private Integer activitiSync;

	/**
	 */
	private Date createTime;

	/**
	 */
	private Integer userIdentity;

	/**
	 */
	private String departIds;

	/**
	 */
	private String post;

	/**
	 */
	private String telephone;

	private String relTenantIds;

	private String clientId;

}
