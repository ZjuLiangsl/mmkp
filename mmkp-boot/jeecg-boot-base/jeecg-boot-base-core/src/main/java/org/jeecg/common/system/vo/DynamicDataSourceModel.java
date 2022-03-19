package org.jeecg.common.system.vo;

import lombok.Data;
import org.springframework.beans.BeanUtils;

@Data
public class DynamicDataSourceModel {

    public DynamicDataSourceModel() {

    }

    public DynamicDataSourceModel(Object dbSource) {
        if (dbSource != null) {
            BeanUtils.copyProperties(dbSource, this);
        }
    }

    /**
     * id
     */
    private java.lang.String id;
    /**
     *
     */
    private java.lang.String code;
    /**
     *
     */
    private java.lang.String dbType;
    /**
     *
     */
    private java.lang.String dbDriver;
    /**
     *
     */
    private java.lang.String dbUrl;

//    /**
//     *
//     */
//    private java.lang.String dbName;

    /**
     *
     */
    private java.lang.String dbUsername;
    /**
     *
     */
    private java.lang.String dbPassword;

}