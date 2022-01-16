package org.platform.modules.pmkb.vo;

import io.swagger.models.auth.In;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class TableNodeVO {

    private Integer top;
    private Integer left;
    private String id;
    private String title;
    private String pkField;
    private String dictCodes="";
    private Map<String,String> tableState;
    private List<FieldNodeVO> fields;
}
