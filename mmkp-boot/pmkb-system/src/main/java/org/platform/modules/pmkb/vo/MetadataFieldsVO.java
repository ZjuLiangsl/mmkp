package org.platform.modules.pmkb.vo;

import lombok.Data;
import org.platform.modules.pmkb.entity.MetadataFields;

@Data
public class MetadataFieldsVO  extends MetadataFields {

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

}
