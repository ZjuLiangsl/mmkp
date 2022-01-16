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
@ApiModel(value="元数据字段扩展信息")
@Data
public class MetadataFieldsExt implements Serializable {
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
     * 字段备注
     */
    private String fieldRemark;

    /**
     * 是否在表格中展示，0-否；1-是
     */
    private String isTableDisplay;

    /**
     * 是否在表单中展示，0-否；1-是
     */
    private String isFormDisplay;

    /**
     * 是否查询字段，0-否；1-是
     */
    private String isSearchField;

    /**
     * 组件类型
     */
    private String fieldCompType;

    /**
     * 字典编码
     */
    private String compDictCode;

    /**
     * 校验规则
     */
    private String compRuleCodes;

    /**
     * 扩展字段1
     */
    private String extField1;

    /**
     * 扩展字段2
     */
    private String extField2;

    /**
     * 扩展字段3
     */
    private String extField3;

    private String extField4;

    private String extField5;

    private static final long serialVersionUID = 1L;
}