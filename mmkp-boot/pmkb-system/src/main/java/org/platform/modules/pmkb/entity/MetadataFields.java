package org.platform.modules.pmkb.entity;

import java.io.Serializable;
import java.math.BigInteger;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * pmkb_metadata_fields
 * @author 
 */
@TableName("pmkb_metadata_fields")
@ApiModel(value="MetadataFields")
@Data
public class MetadataFields implements Serializable {
    /**
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     */
    private String dsCode;

    /**
     */
    private String dbName;

    /**
     */
    private String tableName;

    /**
     */
    private String fieldName;

    /**
     */
    private String fieldTypeFull;


    /**
     */
    private String fieldType;

    /**
     */
    private BigInteger fieldLength;

    /**
     */
    private BigInteger fieldDigit;

    /**
     */
    private String fieldComment;

    /**
     */
    private String isNullable;

    /**
     */
    private String isPk;

    /**
     */
    private String isUnique;

    /**
     */
    private String defaultValue;

    /**
     */
    private String fkDbName;

    /**
     */
    private String fkTableName;

    /**
     */
    private String fkFieldName;

    private String dataState;

    private static final long serialVersionUID = 1L;
}