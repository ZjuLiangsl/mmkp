package org.platform.modules.pmkb.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.platform.modules.pmkb.service.IMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Slf4j
@Api(tags = "元数据管理")
@RestController
@RequestMapping("/md")
public class MetadataController {
    @Value("${md.exclude-schemas}")
    String excludeSchemas;

    @Autowired
    IMetadataService metadataService;

    @ApiOperation(value = "元数据-库列表", notes = "元数据-库列表")
    @GetMapping(value = "/show-databases")
    public Result<?> showDatabases(String dsCode) {

        List<String> list = metadataService.showDatabases(dsCode);
        if(StrUtil.isNotBlank(excludeSchemas)){
            List<String> excludeList = Arrays.asList(excludeSchemas.split(","));
            list = list.stream().filter(s->!excludeList.contains(s)).collect(Collectors.toList());
        }
        return Result.ok(list);
    }

    @ApiOperation(value = "元数据-表列表", notes = "元数据-表列表")
    @PostMapping(value = "/show-tables")
    public Result<?> showTables(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        JSONArray schemaNames = jsonObject.getJSONArray("schemaNames");
        List<String> schemaList = schemaNames.toJavaList(String.class);
        List<Map<String, Object>> list = metadataService.showTables(dsCode, schemaList);
        return Result.ok(list);
    }

    @ApiOperation(value = "元数据-字段列表", notes = "元数据-字段列表")
    @PostMapping(value = "/show-fields")
    public Result<?> showFields(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbName");
        String tableName = jsonObject.getString("tableName");
        List<Map<String, Object>> list = metadataService.showFields(dsCode, dbName,tableName);
        return Result.ok(list);
    }

    @ApiOperation(value = "元数据-库列表", notes = "元数据-采集")
    @PostMapping(value = "/extract-metadata")
    public Result<?> extractMetadata(@RequestBody JSONObject jsonObject){
        try {
            String dsCode = jsonObject.getString("dsCode");
            JSONArray schemaNames = jsonObject.getJSONArray("schemaNames");
            JSONArray tablesNames = jsonObject.getJSONArray("tablesNames");
            List<String> schemaList = schemaNames.toJavaList(String.class);
            List<String> tableList = tablesNames.toJavaList(String.class);
            metadataService.extractMetadata(dsCode,schemaList,tableList);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.error(e.getMessage());
        }
        return Result.ok("ok");
    }
}
