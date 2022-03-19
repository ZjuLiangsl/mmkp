package org.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

/**
 * @Description:
 * @Author: jeecg-boot
 * @Date: 2019-12-25
 * @Version: V1.0
 */
@Data
@TableName("sys_data_source")
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
@ApiModel(value = "sys_data_source", description = "sys_data_source")
public class SysDataSource {

    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value = "id")
    private String id;
    /**
     */
    @Excel(name = "code", width = 15)
    @ApiModelProperty(value = "code")
    private String code;
    /**
     */
    @Excel(name = "name", width = 15)
    @ApiModelProperty(value = "name")
    private String name;
    /**
     */
    @Excel(name = "remark", width = 15)
    @ApiModelProperty(value = "remark")
    private String remark;
    /**
     */
    @Dict(dicCode = "database_type")
    @Excel(name = "dbType", width = 15, dicCode = "database_type")
    @ApiModelProperty(value = "dbType")
    private String dbType;
    /**
     */
    @Excel(name = "dbDriver", width = 15)
    @ApiModelProperty(value = "dbDriver")
    private String dbDriver;
    /**
     */
    @Excel(name = "dbUrl", width = 15)
    @ApiModelProperty(value = "dbUrl")
    private String dbUrl;
    /**
     */
    @Excel(name = "dbName", width = 15)
    @ApiModelProperty(value = "dbName")
    private String dbName;
    /**
     */
    @Excel(name = "dbUsername", width = 15)
    @ApiModelProperty(value = "dbUsername")
    private String dbUsername;
    /**
     */
    @Excel(name = "dbPassword", width = 15)
    @ApiModelProperty(value = "dbPassword")
    private String dbPassword;
    /**
     */
    @ApiModelProperty(value = "createBy")
    private String createBy;
    /**
     * createTime
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "createTime")
    private java.util.Date createTime;
    /**
     * updateBy
     */
    @ApiModelProperty(value = "updateBy")
    private String updateBy;
    /**
     * updateTime
     */
    @JsonFormat(timezone = "GMT+8", pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty(value = "updateTime")
    private java.util.Date updateTime;
    /**
     * sysOrgCode
     */
    @Excel(name = "sysOrgCode", width = 15)
    @ApiModelProperty(value = "sysOrgCode")
    private String sysOrgCode;
}
