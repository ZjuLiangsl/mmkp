package org.platform.modules.pmkb.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.io.Serializable;
import java.math.BigInteger;

/**
 * pmkb_metadata_fields
 * @author 
 */
@TableName("pmkb_metadata_fields_ext")
@ApiModel(value="MetadataFieldsExt")
@Data
public class MetadataFieldsExt implements Serializable {
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
    private String fieldRemark;

    /**
     */
    private String isTableDisplay;

    /**
     */
    private String isFormDisplay;

    /**
     */
    private String isSearchField;

    /**
     */
    private String fieldCompType;

    /**
     */
    private String compDictCode;

    /**
     */
    private String compRuleCodes;

    /**
     */
    private String extField1;

    /**
     */
    private String extField2;

    /**
     */
    private String extField3;

    private String extField4;

    private String extField5;

    private static final long serialVersionUID = 1L;
}