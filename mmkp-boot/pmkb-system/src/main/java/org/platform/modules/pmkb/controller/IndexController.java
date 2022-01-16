package org.platform.modules.pmkb.controller;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.platform.modules.pmkb.service.IMetadataTablesService;
import org.platform.modules.pmkb.service.IStageDataService;
import org.platform.modules.system.service.ISysDataSourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/idx")
public class IndexController {
    @Autowired
    private ISysDataSourceService sysDataSourceService;
    @Autowired
    IMetadataTablesService tablesService;
    @Autowired
    IStageDataService stageDataService;


    @PostMapping(value = "/total")
    public Result<?> loadTables(@RequestBody JSONObject jsonObject) {
        int dsTotal = sysDataSourceService.count();
        int tableTotal = tablesService.count();
        QueryWrapper qw = new QueryWrapper();
        qw.eq("data_state", "1");
        int stageTableTotal = tablesService.count(qw);
        int stageDataTotal = stageDataService.count();
        Map map = new HashMap();
        map.put("dsTotal", dsTotal);
        map.put("tableTotal", tableTotal);
        map.put("stageTableTotal", stageTableTotal);
        map.put("stageDataTotal", stageDataTotal);
        return Result.ok(map);
    }
}
