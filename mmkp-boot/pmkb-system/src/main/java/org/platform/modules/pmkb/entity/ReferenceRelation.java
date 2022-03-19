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
@TableName("pmkb_reference_relation")
@ApiModel(value="ReferenceRelation")
@Data
public class ReferenceRelation implements Serializable {
    /**
     */
    @TableId(type = IdType.AUTO)
    @ApiModelProperty(value="id")
    private String id;

    /**
     */
    @ApiModelProperty(value="dsCode")
    private String dsCode;

    /**
     */
    @ApiModelProperty(value="dbName")
    private String dbName;

    /**
     */
    @ApiModelProperty(value="tableName")
    private String tableName;

    private String fieldName;

    private String refTableName;

    private String refFieldName;


    private static final long serialVersionUID = 1L;
}