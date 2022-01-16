package org.platform.modules.pmkb.controller;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.api.vo.Result;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.entity.ReferenceRelation;
import org.platform.modules.pmkb.service.*;
import org.platform.modules.pmkb.util.MetadataUtil;
import org.platform.modules.pmkb.vo.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Api(tags = "结构数据编辑")
@RestController
@RequestMapping("/editor")
public class StructEditorController {
    @Autowired
    IMetadataService metadataService;
    @Autowired
    private IMetadataTablesService tablesService;
    @Autowired
    private IMetadataFieldsService fieldsService;
    @Autowired
    private IMetadataTablesExtService tablesExtService;
    @Autowired
    private IMetadataFieldsExtService fieldsExtService;
    @Autowired
    private IStageDataService dataService;
    @Autowired
    private IReferenceRelationService referenceRelationService;


    @ApiOperation(value = "元数据-表", notes = "元数据-表")
    @PostMapping(value = "/load-tables")
    public Result<?> loadTables(@RequestBody JSONObject jsonObject) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        String dsCode = jsonObject.getString("dsCode");
        String dbCode = jsonObject.getString("dbCode");
        String text = jsonObject.getString("searchText");

        Map<String, Object> paramsMap = new HashMap<>();
        paramsMap.put("dsCode", dsCode);
        paramsMap.put("dbCode", dbCode);
        paramsMap.put("searchText", text);
        List<MetadataTablesVO> tablesList = tablesService.listTables(paramsMap);
        Map<String, MetadataTablesVO> tablesMap = tablesList.stream().collect(Collectors.toMap(MetadataTablesVO::getTableName, ext -> ext,(item1, item2) -> item1));
//        List<Map<String, Object>> tablesMapList = metadataService.showTablesByText(dsCode, dbCode, text);

//        for (Map map:tablesMapList) {
//            String tableName = String.valueOf(map.get("tableName"));
//            String tableComment = ObjectUtil.isNull(map.get("tableComment"))?"":String.valueOf(map.get("tableComment"));
//            Map<String, Object> tmp = new HashMap<>();
//            String key = dsCode + "." + dbCode + "." + tableName;
//            tmp.put("key", key);
//            keys.add(key);
//            tmp.put("tableName", tableName);
//            tmp.put("is-leaf", false);
//            if(tablesMap.containsKey(tableName)){
//                MetadataTablesVO vo = tablesMap.get(tableName);
//                if (vo != null) {
//                    tmp.put("title", tableName + ":" + (StrUtil.isNotBlank(vo.getTableRemark())?vo.getTableRemark():tableComment));
//                    tmp.put("from", vo.getDataState());
//                    if("1".equals(vo.getDataState())){
//                        JSONObject slot = new JSONObject();
//                        slot.put("title", "staging");
//                        tmp.put("scopedSlots", slot);
//                    }else if("2".equals(vo.getDataState())){
//                        JSONObject slot = new JSONObject();
//                        slot.put("title", "editing");
//                        tmp.put("scopedSlots", slot);
//                    }
//                }else{
//                    tmp.put("title", tableName + ":" + tableComment);
//                    tmp.put("from", "0");
//                }
//            }
//            else{
//                tmp.put("title", tableName + ":" + tableComment);
//                tmp.put("from", "0");
//            }
//            list.add(tmp);
//        }

        for(MetadataTablesVO vo:tablesList){
            String tableName = vo.getTableName();
            String key = dsCode + "." + dbCode + "." + tableName;
            if(!keys.contains(key)){
                Map<String, Object> tmp = new HashMap<>();
                tmp.put("key", key);
                tmp.put("tableName", tableName);
                tmp.put("is-leaf", false);
                tmp.put("title", tableName + ":" + (StrUtil.isNotBlank(vo.getTableRemark()) ? vo.getTableRemark() : vo.getTableComment()));
                tmp.put("from", vo.getDataState());
                if("1".equals(vo.getDataState())){
                    JSONObject slot = new JSONObject();
                    slot.put("title", "staging");
                    tmp.put("scopedSlots", slot);
                }else if("2".equals(vo.getDataState())){
                    JSONObject slot = new JSONObject();
                    slot.put("title", "editing");
                    tmp.put("scopedSlots", slot);
                }
                list.add(tmp);
            }
        }
        return Result.ok(list);
    }

    @ApiOperation(value = "元数据-字段", notes = "元数据-字段")
    @PostMapping(value = "/load-fields")
    public Result<?> loadFields(@RequestBody JSONObject jsonObject) {
        List<Map<String, Object>> list = new ArrayList<>();
        List<String> keys = new ArrayList<>();
        String dsCode = jsonObject.getString("dsCode");
        String dbCode = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");

        MetadataTables po = new MetadataTables();
        po.setDsCode(dsCode);
        po.setDbName(dbCode);
        po.setTableName(tableName);
        List<MetadataFieldsVO> tablesList = fieldsService.listMetadataFieldsVO(po);
//        Map<String, MetadataFieldsVO> fieldsMap = tablesList.stream().collect(Collectors.toMap(MetadataFieldsVO::getFieldName, ext -> ext));
//        List<MetadataFields> fieldsMapList = metadataService.showFieldsForCanvas(dsCode, dbCode, tableName);

//        for (MetadataFields map : fieldsMapList) {
//            String fieldName = String.valueOf(map.getFieldName());
//            String fieldComment = ObjectUtil.isNull(map.getFieldComment()) ? "" : String.valueOf(map.getFieldComment());
//            Map<String, Object> tmp = new HashMap<>();
//            String key = dsCode + "." + dbCode + "." + tableName + "." + fieldName;
//            tmp.put("key", key);
//            keys.add(key);
//            tmp.put("is-leaf", true);
//            tmp.put("fieldName", fieldName);
//            tmp.put("fieldType", map.getFieldType());
//            tmp.put("fieldLength", map.getFieldLength());
//            tmp.put("fieldDigit", map.getFieldDigit());
//            if (fieldsMap.containsKey(fieldName)) {
//                MetadataFieldsVO vo = fieldsMap.get(fieldName);
//                if (vo != null) {
//                    tmp.put("title", fieldName + ":" + (StrUtil.isNotBlank(vo.getFieldComment())?vo.getFieldComment():fieldComment));
//                    tmp.put("from", vo.getDataState());
//                    if("1".equals(vo.getDataState())){
//                        JSONObject slot = new JSONObject();
//                        slot.put("title", "staging");
//                        tmp.put("scopedSlots", slot);
//                    }else if("2".equals(vo.getDataState())){
//                        JSONObject slot = new JSONObject();
//                        slot.put("title", "editing");
//                        tmp.put("scopedSlots", slot);
//                    }
//                } else {
//                    tmp.put("title", fieldName + ":" + (StrUtil.isBlank(fieldComment) ? "" : fieldComment));
//                    tmp.put("from", "0");
//                }
//            }
//            else {
//                tmp.put("title", fieldName + ":" + (StrUtil.isBlank(fieldComment) ? "" : fieldComment));
//                tmp.put("from", "0");
//            }
//            list.add(tmp);
//        }

        for(MetadataFieldsVO vo:tablesList){
            String fieldName = vo.getFieldName();
            String key = dsCode + "." + dbCode + "." + tableName + "." + fieldName;
            if (!keys.contains(key)) {
                Map<String, Object> tmp = new HashMap<>();
                tmp.put("key", key);
                tmp.put("is-leaf", true);
                tmp.put("fieldName", fieldName);
                tmp.put("fieldType", vo.getFieldType());
                tmp.put("fieldLength", vo.getFieldLength());
                tmp.put("fieldDigit", vo.getFieldDigit());
                tmp.put("isPk", vo.getIsPk());
                tmp.put("fieldType", vo.getFieldType());
                tmp.put("fieldComment", (StrUtil.isBlank(vo.getFieldComment()) ? "" : vo.getFieldComment()));
                tmp.put("title", fieldName + ":" + (StrUtil.isBlank(vo.getFieldComment()) ? "" : vo.getFieldComment()));
                tmp.put("from", vo.getDataState());
                if("1".equals(vo.getDataState())){
                    JSONObject slot = new JSONObject();
                    slot.put("title", "staging");
                    tmp.put("scopedSlots", slot);
                }else if("2".equals(vo.getDataState())){
                    JSONObject slot = new JSONObject();
                    slot.put("title", "editing");
                    tmp.put("scopedSlots", slot);
                }
                list.add(tmp);
            }
        }

        return Result.ok(list);
    }

    @ApiOperation(value = "元数据-画布数据", notes = "元数据-画布数据")
    @PostMapping(value = "/load-canvas")
    public Result<?> loadCanvas(@RequestBody JSONObject jsonObject) {
//        Map<String, Object> result = new HashMap<>();
//        List<TableNodeVO> nodes = new ArrayList<>();
//        List<EdgeVO> edges = new ArrayList<>();

        String key = jsonObject.getString("key");
        String title = jsonObject.getString("title");
        String from = jsonObject.getString("from");
        String[] keyDatas = key.split("\\.");
        String dsCode = keyDatas[0];
        String dbCode = keyDatas[1];
        String tableName = keyDatas[2];

        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ds_code", dsCode);
        wrapper.eq("db_name", dbCode);
        wrapper.eq("table_name", tableName);
        MetadataTables one = tablesService.getOne(wrapper);

        TableNodeVO tableNode = new TableNodeVO();
        String tableState = "0";
        Map<String, String> tableStateMap = new HashMap<>();
        if(one==null){
            tableStateMap.put("state", "0");
            tableState = "0";
            List<Map<String, Object>> maps = metadataService.showTablesByText(dsCode, dbCode, tableName);
            if(maps==null || maps.size()==0){
                return Result.error("未找到表：" + tableName + ",可能已被删除!");
            }
            tableNode.setTitle(String.valueOf(maps.get(0).get("tableName"))+":"+String.valueOf(maps.get(0).get("tableComment")));
            tableStateMap.put("title", String.valueOf(maps.get(0).get("tableName"))+":"+String.valueOf(maps.get(0).get("tableComment")));
        }
        else{
            tableStateMap.put("state", one.getDataState());
            tableState = one.getDataState();
            tableNode.setTitle(one.getTableName() +":" +one.getTableComment());
            tableStateMap.put("title", one.getTableName() +":" +one.getTableComment());
        }

        List<String> stageField = new ArrayList<>();
        List<String> editField = new ArrayList<>();
        List<String> referenceField = new ArrayList<>();
        List<String> pkField = new ArrayList<>();
        Map<String, List<ReferenceRelation>> referenceMap = new HashMap<>();
        Map<String, String> keyMap = new HashMap<>();

        tableNode.setTop(50);
        tableNode.setLeft(50);
        tableNode.setId(key);

        tableNode.setTableState(tableStateMap);

        MetadataTables po = new MetadataTables();
        po.setDsCode(dsCode);
        po.setDbName(dbCode);
        po.setTableName(tableName);
        List<MetadataFieldsVO> fieldsVOList = fieldsService.listMetadataFieldsVO(po);
        if(fieldsVOList!=null && fieldsVOList.size()>0){
            List<FieldNodeVO> fieldNodeList = fieldsVOList.stream().map(vo -> {
                FieldNodeVO fieldNode = new FieldNodeVO();
                BeanUtil.copyProperties(vo,fieldNode);
                if(StrUtil.isNotBlank(vo.getFieldRemark())){
                    fieldNode.setFieldComment(vo.getFieldRemark());
                }
                if("1".equals(vo.getIsPk())){
                    tableNode.setPkField(vo.getFieldName());
                    pkField.add(vo.getFieldName());
                }
                fieldNode.setFieldCompType(MetadataUtil.compSelector(vo.getFieldType()));
                if(StrUtil.isNotBlank(vo.getFkTableName()) || StrUtil.isNotBlank(vo.getFkFieldName())){
                    fieldNode.setFieldCompType(MetadataUtil.SEARCH);
                    fieldNode.setRefKeys(vo.getFkTableName()+":"+vo.getFkFieldName());
                }
                if(StrUtil.isNotBlank(vo.getCompDictCode())){
                    tableNode.setDictCodes(tableNode.getDictCodes()+","+vo.getCompDictCode());
                }
                if("1".equals(vo.getDataState())){
                    stageField.add(vo.getFieldName());
                }
                if("2".equals(vo.getDataState())){
                    editField.add(vo.getFieldName());
                }
                if("reference".equals(vo.getFieldType())){
                    referenceField.add(vo.getFieldName());
                }
                return fieldNode;
            }).collect(Collectors.toList());
            tableNode.setFields(fieldNodeList);
        }
        else{
            List<MetadataFields> fieldsList = metadataService.showFieldsForCanvas(dsCode, dbCode, tableName);
            List<FieldNodeVO> fieldNodeList = new ArrayList<>();
            for (int i = 0; i < fieldsList.size(); i++) {
                MetadataFields vo = fieldsList.get(i);
                FieldNodeVO fieldNode = new FieldNodeVO();
                BeanUtil.copyProperties(vo,fieldNode);
                if("1".equals(vo.getIsPk())){
                    tableNode.setPkField(vo.getFieldName());
                    pkField.add(vo.getFieldName());
                }
                fieldNode.setFieldCompType(MetadataUtil.compSelector(vo.getFieldType()));
                if(StrUtil.isNotBlank(vo.getFkTableName()) || StrUtil.isNotBlank(vo.getFkFieldName())){
                    fieldNode.setFieldCompType(MetadataUtil.SEARCH);
                    fieldNode.setRefKeys(vo.getFkTableName()+":"+vo.getFkFieldName());
                }
                if("1".equals(vo.getDataState())){
                    stageField.add(vo.getFieldName());
                }
                if("2".equals(vo.getDataState())){
                    editField.add(vo.getFieldName());
                }
                if("reference".equals(vo.getFieldType())){
                    referenceField.add(vo.getFieldName());
                }
                fieldNode.setId("remote-field-" + i);
                fieldNodeList.add(fieldNode);
            }
            tableNode.setFields(fieldNodeList);
        }

        List<ReferenceRelation> list = referenceRelationService.list(wrapper);
        list.stream().forEach(rr->{
            if(referenceMap.containsKey(rr.getFieldName())){
                referenceMap.get(rr.getFieldName()).add(rr);
            }else{
                List<ReferenceRelation> tmp = new ArrayList();
                tmp.add(rr);
                referenceMap.put(rr.getFieldName(), tmp);
            }
        });

        QueryWrapper qw = new QueryWrapper();
        qw.eq("ds_code", dsCode);
        qw.eq("db_name", dbCode);
        qw.eq("ref_table_name", tableName);
        List leftLine = referenceRelationService.list(qw);

        QueryWrapper qwr = new QueryWrapper();
        qwr.eq("ds_code", dsCode);
        qwr.eq("db_name", dbCode);
        qwr.eq("table_name", tableName);
        List rightLine = referenceRelationService.list(qwr);

        Map result = new HashMap();
        result.put("stageField", stageField);
        result.put("editField", editField);
        result.put("referenceField", referenceField);
        result.put("pkField", pkField);
        result.put("referenceMap", referenceMap);
        result.put("tableState", tableState);
        result.put("nodeData", tableNode);
        result.put("key", key);
        result.put("leftLine", leftLine);
        result.put("rightLine", rightLine);
        return Result.ok(result);
    }






}
