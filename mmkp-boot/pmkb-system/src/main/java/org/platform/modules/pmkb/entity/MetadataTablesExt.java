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
@ApiModel(value="元数据表扩展信息")
@Data
public class MetadataTablesExt implements Serializable {
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

    /**
     * 表注释
     */
    @ApiModelProperty(value="表注释")
    private String tableComment;

    /**
     * 表备注
     */
    @ApiModelProperty(value="表备注")
    private String tableRemark;

    /**
     * 是否分页展示，0-否；1-是
     */
    @ApiModelProperty(value="是否分页展示，0-否；1-是")
    private String isPaging;

    /**
     * 表单宽度，默认450px
     */
    @ApiModelProperty(value="表单宽度，默认450px")
    private Integer formWidth;

    /**
     * 扩展字段1
     */
    @ApiModelProperty(value="扩展字段1")
    private String extField1;

    /**
     * 扩展字段2
     */
    @ApiModelProperty(value="扩展字段2")
    private String extField2;

    /**
     * 扩展字段3
     */
    @ApiModelProperty(value="扩展字段3")
    private String extField3;

    private static final long serialVersionUID = 1L;
}