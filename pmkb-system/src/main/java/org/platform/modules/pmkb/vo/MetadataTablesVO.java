package org.platform.modules.pmkb.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.platform.modules.pmkb.entity.MetadataTables;

@Data
public class MetadataTablesVO extends MetadataTables {

    private String dsName;

    /**
     * 表备注
     */
    @ApiModelProperty(value="表备注")
    private String tableRemark;

    /**
     * 是否分页展示，0-否；1-是
     */
    @ApiModelProperty(value="是否分页展示，0-否；1-是")
    private String isPaging;


    /**
     * 表单宽度，默认450px
     */
    @ApiModelProperty(value="表单宽度，默认450px")
    private Integer formWidth;


    /**
     * 扩展字段1
     */
    @ApiModelProperty(value="扩展字段1")
    private String extField1;

    /**
     * 扩展字段2
     */
    @ApiModelProperty(value="扩展字段2")
    private String extField2;

    /**
     * 扩展字段3
     */
    @ApiModelProperty(value="扩展字段3")
    private String extField3;
}
