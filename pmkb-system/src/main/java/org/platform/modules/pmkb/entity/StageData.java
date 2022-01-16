package org.platform.modules.pmkb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;

/**
 * pmkb_metadata_tables
 * @author 
 */
@TableName("pmkb_stage_data")
@ApiModel(value="元数据表信息")
@Data
public class StageData implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value="主键ID")
    private String id;

    /**
     * 数据源ID
     */
    @ApiModelProperty(value="数据源ID")
    private String dsCode;

    /**
     * 库名
     */
    @ApiModelProperty(value="库名")
    private String dbName;

    /**
     * 表名
     */
    @ApiModelProperty(value="表名")
    private String tableName;

    private String pkFieldName;

    private String pkFieldValue;
    /**
     * 表注释
     */
    @ApiModelProperty(value="表注释")
    private String dataJsonStr;

    private String dataState;


    private static final long serialVersionUID = 1L;
}