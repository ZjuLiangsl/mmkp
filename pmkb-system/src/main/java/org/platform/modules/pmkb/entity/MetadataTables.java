package org.platform.modules.pmkb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.io.Serializable;
import lombok.Data;

/**
 * pmkb_metadata_tables
 * @author 
 */
@TableName("pmkb_metadata_tables")
@ApiModel(value="元数据表信息")
@Data
public class MetadataTables implements Serializable {
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

    private String dataState;

    private static final long serialVersionUID = 1L;
}