package org.platform.modules.pmkb.vo;

import lombok.Data;

@Data
public class EdgeVO {
    private Integer id;
    private String sourceNode;
    private String targetNode;
    private String source;
    private String target;
    private String label;
}
