package org.platform.modules.system.vo;

import lombok.Data;
import org.platform.modules.system.entity.SysDictItem;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.jeecgframework.poi.excel.annotation.ExcelCollection;

import java.util.List;

@Data
public class SysDictPage {

    /**
     */
    private String id;
    /**
     */
    @Excel(name = "dictName", width = 20)
    private String dictName;

    /**
     */
    @Excel(name = "dictCode", width = 30)
    private String dictCode;
    /**
     */
    private Integer delFlag;
    /**
     */
    @Excel(name = "description", width = 30)
    private String description;

    @ExcelCollection(name = "sysDictItemList")
    private List<SysDictItem> sysDictItemList;

}
