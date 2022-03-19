package org.platform.modules.pmkb.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.platform.modules.pmkb.entity.MetadataTables;

@Data
public class MetadataTablesVO extends MetadataTables {

    private String dsName;

    /**
     */
    @ApiModelProperty(value="tableRemark")
    private String tableRemark;

    /**
     */
    @ApiModelProperty(value="isPaging")
    private String isPaging;


    /**
     */
    @ApiModelProperty(value="formWidth 450px")
    private Integer formWidth;


    /**
     */
    @ApiModelProperty(value="extField1")
    private String extField1;

    /**
     */
    @ApiModelProperty(value="extField2")
    private String extField2;

    /**
     */
    @ApiModelProperty(value="extField3")
    private String extField3;
}
