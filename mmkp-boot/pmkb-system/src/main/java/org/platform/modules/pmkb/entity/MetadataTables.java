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
@ApiModel(value="MetadataTables")
@Data
public class MetadataTables implements Serializable {
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

    private String dataState;

    private static final long serialVersionUID = 1L;
}