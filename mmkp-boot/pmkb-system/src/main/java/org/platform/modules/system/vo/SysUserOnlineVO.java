package org.platform.modules.system.vo;

import java.util.Date;

import org.jeecg.common.aspect.annotation.Dict;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Data;

/**
 *
 * @Author: chenli
 * @Date: 2020-06-07
 * @Version: V1.0
 */
@Data
public class SysUserOnlineVO {
    /**
     */
    private String id;

    /**
     */
    private String token;

    /**
     */
    private String username;

    /**
     */
    private String realname;

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
    @Dict(dicCode = "sex")
    private Integer sex;

    /**
     */
    private String phone;
}
