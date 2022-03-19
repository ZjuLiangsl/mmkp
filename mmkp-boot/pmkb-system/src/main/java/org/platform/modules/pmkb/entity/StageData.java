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
@Data
public class StageData implements Serializable {
    /**
     */
    @TableId(type = IdType.ASSIGN_ID)
    @ApiModelProperty(value="id")
    private String id;

    /**
     * dsCode
     */
    @ApiModelProperty(value="dsCode")
    private String dsCode;

    /**
     * dbName
     */
    @ApiModelProperty(value="dbName")
    private String dbName;

    /**
     * tableName
     */
    @ApiModelProperty(value="tableName")
    private String tableName;

    private String pkFieldName;

    private String pkFieldValue;
    /**
     * dataJsonStr
     */
    @ApiModelProperty(value="dataJsonStr")
    private String dataJsonStr;

    private String dataState;


    private static final long serialVersionUID = 1L;
}