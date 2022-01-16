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
@ApiModel(value="元数据表信息")
@Data
public class ReferenceRelation implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.AUTO)
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

    private String fieldName;

    private String refTableName;

    private String refFieldName;


    private static final long serialVersionUID = 1L;
}