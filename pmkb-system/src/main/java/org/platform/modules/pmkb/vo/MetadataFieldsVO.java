package org.platform.modules.pmkb.vo;

import lombok.Data;
import org.platform.modules.pmkb.entity.MetadataFields;

@Data
public class MetadataFieldsVO  extends MetadataFields {

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

}
