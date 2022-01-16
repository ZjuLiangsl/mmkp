package org.platform.modules.pmkb.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.platform.modules.pmkb.service.IMetadataFieldsService;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/md/fields")
public class MetadataFieldsController {
    @Autowired
    private IMetadataFieldsService fieldsService;

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public Result<List<MetadataFieldsVO>> queryList(MetadataTablesVO tablesVO, HttpServletRequest req) {
        Result<List<MetadataFieldsVO>> result = new Result<>();
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("ds_code", tablesVO.getDsCode());
        queryWrapper.eq("db_name", tablesVO.getDbName());
        queryWrapper.eq("table_name", tablesVO.getTableName());
        List<MetadataFieldsVO> list = fieldsService.list(queryWrapper);
        result.setSuccess(true);
        result.setResult(list);
        log.info(list.toString());
        return result;
    }
}
