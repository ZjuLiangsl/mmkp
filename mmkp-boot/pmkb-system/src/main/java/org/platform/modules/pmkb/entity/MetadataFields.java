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
@ApiModel(value="元数据字段信息")
@Data
public class MetadataFields implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;

    /**
     * 数据源ID
     */
    private String dsCode;

    /**
     * 库名
     */
    private String dbName;

    /**
     * 表名
     */
    private String tableName;

    /**
     * 字段名
     */
    private String fieldName;

    /**
     * 完整字段类型
     */
    private String fieldTypeFull;


    /**
     * 字段类型
     */
    private String fieldType;

    /**
     * 字段长度
     */
    private BigInteger fieldLength;

    /**
     * 小数位数
     */
    private BigInteger fieldDigit;

    /**
     * 字段注释
     */
    private String fieldComment;

    /**
     * 是否可为空，0-否；1-是
     */
    private String isNullable;

    /**
     * 是否主键列，0-否；1-是
     */
    private String isPk;

    /**
     * 是否唯一列，0-否；1-是
     */
    private String isUnique;

    /**
     * 默认值
     */
    private String defaultValue;

    /**
     * 外键,参考库名
     */
    private String fkDbName;

    /**
     * 外键,参考表名
     */
    private String fkTableName;

    /**
     * 外键,参考字段名
     */
    private String fkFieldName;

    private String dataState;

    private static final long serialVersionUID = 1L;
}