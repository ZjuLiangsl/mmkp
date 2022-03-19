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
@TableName("pmkb_metadata_tables_ext")
@ApiModel(value="MetadataTablesExt")
@Data
public class MetadataTablesExt implements Serializable {
    /**
     */
    @TableId(type = IdType.ASSIGN_ID)
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

    /**
     */
    @ApiModelProperty(value="tableComment")
    private String tableComment;

    /**
     */
    @ApiModelProperty(value="tableRemark")
    private String tableRemark;

    /**
     */
    @ApiModelProperty(value="isPaging")
    private String isPaging;

    /**
     */
    @ApiModelProperty(value="formWidth 450px")
    private Integer formWidth;

    /**
     */
    @ApiModelProperty(value="extField1")
    private String extField1;

    /**
     */
    @ApiModelProperty(value="extField2")
    private String extField2;

    /**
     */
    @ApiModelProperty(value="extField3")
    private String extField3;

    private static final long serialVersionUID = 1L;
}