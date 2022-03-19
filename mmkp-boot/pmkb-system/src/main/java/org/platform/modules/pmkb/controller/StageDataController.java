package org.platform.modules.pmkb.controller;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.util.dynamic.db.DynamicDBUtil;
import org.platform.modules.pmkb.entity.MetadataFields;
import org.platform.modules.pmkb.entity.MetadataTables;
import org.platform.modules.pmkb.entity.ReferenceRelation;
import org.platform.modules.pmkb.entity.StageData;
import org.platform.modules.pmkb.service.*;
import org.platform.modules.pmkb.util.MetadataUtil;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.LinkedCaseInsensitiveMap;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/stage-data")
public class StageDataController {

    @Autowired
    private IMetadataTablesService tablesService;
    @Autowired
    private IMetadataFieldsService fieldsService;
    @Autowired
    private IStageDataService stageDataService;
    @Autowired
    IMetadataService metadataService;

    @Autowired
    IReferenceRelationService referenceRelationService;

    @PostMapping(value = "/list")
    public Result<?> list(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");

        List<StageData> stageDataList = new ArrayList<>();
        List<Map<String, Object>> remoteDateList = new ArrayList<>();

        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ds_code", dsCode);
        wrapper.eq("db_name", dbName);
        wrapper.eq("table_name", tableName);
        MetadataTables one = tablesService.getOne(wrapper);

        List<String> pkFields = new ArrayList<>();
        AtomicBoolean pkFieldIsDate = new AtomicBoolean(false);
        MetadataTables po = new MetadataTables();
        po.setDsCode(dsCode);
        po.setDbName(dbName);
        po.setTableName(tableName);
        List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(po);
        StringBuilder sql = new StringBuilder();
        sql.append(" select ");
        List<String> fields = new ArrayList<>();
        List<String> conditions = new ArrayList<>();
        listFieldsVO.forEach(vo -> {
            if (vo.getFieldType().indexOf("time") > -1) {
                fields.add("date_format(`" + vo.getFieldName() + "`,'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d %H:%i:%S") + "') as " + vo.getFieldName());
            } else if (vo.getFieldType().indexOf("date") > -1) {
                fields.add("date_format(`" + vo.getFieldName() + "`,'" + (StrUtil.isNotBlank(vo.getExtField2()) ? vo.getExtField2() : "%Y-%m-%d") + "') as " + vo.getFieldName());
            } else {
                fields.add("`"+vo.getFieldName()+"`");
            }

            if("1".equals(vo.getIsPk())){
                pkFields.add(vo.getFieldName());
                if(vo.getFieldType().indexOf("time") > -1 || vo.getFieldType().indexOf("date") > -1){
                    pkFieldIsDate.set(true);
                }
            }
        });
        sql.append(String.join(",", fields));
        sql.append(" from ");
        sql.append("`" + po.getDbName() + "`." + po.getTableName());
        if (conditions.size() > 0) {
            sql.append(" where ");
            sql.append(String.join(" and ", conditions));
        }

        if("0".equals(one.getDataState())){
            stageDataList = stageDataService.list(wrapper);
            remoteDateList = DynamicDBUtil.findList(po.getDsCode(), sql.toString());
        }
        else{
            stageDataList = stageDataService.list(wrapper);
        }
        int stageMaxId = 0,remoteMaxId=0;
        List<Map> dataList = new ArrayList<>();
        List<Integer> stageDataPkValueList = new ArrayList<>();
        for (StageData s:stageDataList){
            Map map = JSONObject.parseObject(s.getDataJsonStr(), Map.class);
            map.put("dataState", s.getDataState());
            int id = 0;
            try{
                id = Integer.parseInt(s.getPkFieldValue());
            }catch (Exception e){
                id = 0;
            }
            stageDataPkValueList.add(id);
            if(id>stageMaxId){
                stageMaxId = id;
            }
            dataList.add(map);
        }
        for (int i = 0; i < remoteDateList.size(); i++) {
            Map<String, Object> m = remoteDateList.get(i);
            m.put("dataState", "0");
            if(!pkFieldIsDate.get() && pkFields.size()==1){
                int id = 0;
                try{
                    id = Integer.parseInt(String.valueOf(m.get(pkFields.get(0))));
                }catch (Exception e){
                    id = 0;
                }
                if (stageDataPkValueList.contains(id)) {
                    continue;
                }
                if(id>remoteMaxId){
                    remoteMaxId = id;
                }
            }
            dataList.add(m);
        }
        Map result = new HashMap();
        result.put("dataList", dataList);
        if(pkFieldIsDate.get()){
            result.put("add", false);
            result.put("del", true);
            result.put("edit", true);
            result.put("msg", "The current table has a primary key of date type and does not support new operations!");
        }
        else if(pkFields.size()==0){
            result.put("add", true);
            result.put("del", false);
            result.put("edit", false);
            result.put("msg", "The current table does not have a primary key and cannot be modified or deleted!");
        }
        else if(pkFields.size()>1){
            result.put("add", false);
            result.put("del", true);
            result.put("edit", true);
            result.put("msg", "The current table is a multi-field combined primary key and does not support new operations!");
        }
        else{
            result.put("add", true);
            result.put("del", true);
            result.put("edit", true);
            result.put("maxId", stageMaxId > remoteMaxId ? stageMaxId+1 : remoteMaxId+1);
        }

        return Result.ok(result);
    }

    @PostMapping(value = "/load-option")
    public Result<?> loadOption(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");

        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ds_code", dsCode);
        wrapper.eq("db_name", dbName);
        wrapper.eq("table_name", tableName);
        MetadataTables one = tablesService.getOne(wrapper);

        List<String> pkFields = new ArrayList<>();
        AtomicBoolean pkFieldIsDate = new AtomicBoolean(false);
        MetadataTables po = new MetadataTables();
        po.setDsCode(dsCode);
        po.setDbName(dbName);
        po.setTableName(tableName);
        List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(po);
        listFieldsVO.forEach(vo -> {
            if("1".equals(vo.getIsPk())){
                pkFields.add(vo.getFieldName());
                if(vo.getFieldType().indexOf("time") > -1 || vo.getFieldType().indexOf("date") > -1){
                    pkFieldIsDate.set(true);
                }
            }
        });
        Map result = new HashMap();
        if(pkFieldIsDate.get()){
            result.put("add", false);
            result.put("del", true);
            result.put("edit", true);
            result.put("msg", "The current table has a primary key of date type and does not support new operations!");
        }else if(pkFields.size()==0){
            result.put("add", true);
            result.put("del", false);
            result.put("edit", false);
            result.put("msg", "The current table does not have a primary key and cannot be modified or deleted!");
        }else if(pkFields.size()>1){
            result.put("add", false);
            result.put("del", true);
            result.put("edit", true);
            result.put("msg", "The current table is a multi-field combined primary key and does not support new operations!");
        }else{
            result.put("add", true);
            result.put("del", true);
            result.put("edit", true);

            StringBuilder sql = new StringBuilder();
            sql.append(" select ");
            sql.append(" max(" + pkFields.get(0) + ") maxid ");
            sql.append(" from ");
            sql.append("`" + po.getDbName() + "`." + po.getTableName());

            int stageMaxId = 0,remoteMaxId=0;
            if("1".equals(one.getDataState())){
                {
                    wrapper.select("max(cast(pk_field_value as UNSIGNED INTEGER)) maxid");
                    Map map = stageDataService.getMap(wrapper);
                    if(map==null){
                        stageMaxId = 0;
                    }else{
                        stageMaxId = Integer.parseInt(String.valueOf(map.get("maxid")));
                    }
                }
            }else{
                wrapper.select("max(cast(pk_field_value as UNSIGNED INTEGER)) maxid");
                Map map = stageDataService.getMap(wrapper);
                if(map==null){
                    stageMaxId = 0;
                }else{
                    stageMaxId = Integer.parseInt(String.valueOf(map.get("maxid")));
                }

                Object obj = DynamicDBUtil.findOne(po.getDsCode(), sql.toString());
                remoteMaxId = (int) ((LinkedCaseInsensitiveMap) obj).get("maxid");
            }
            result.put("maxId", stageMaxId > remoteMaxId ? stageMaxId+1 : remoteMaxId+1);
        }
        return Result.ok(result);
    }

    @PostMapping(value = "/list-reference-relation")
    public Result<?> listReferenceRelation(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");

        List<ReferenceRelation> list = referenceRelationService.listByParam(dsCode,dbName,tableName);

        Map<String, List<String>> result = new HashMap<>();
        for (ReferenceRelation rr:list){
            String fieldName = rr.getFieldName();
            if(result.get(fieldName)!=null){
                result.get(fieldName).add(rr.getDsCode() + "." + rr.getDbName() + "." + rr.getRefTableName());
            }else{
                List<String> tmp = new ArrayList<>();
                tmp.add(rr.getDsCode() + "." + rr.getDbName() + "." + rr.getRefTableName());
                result.put(fieldName, tmp);
            }
        }
        return Result.ok(result);
    }

    @PostMapping(value = "/save")
    public Result<?> save(@RequestBody JSONObject jsonObject){
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");
        JSONObject rowData = jsonObject.getJSONObject("rowData");
        String pkField = jsonObject.getString("pkField");
        String pkValue = rowData.getString(pkField);
        String dataState = rowData.getString("dataState");

        QueryWrapper wrapper = new QueryWrapper();
        wrapper.eq("ds_code", dsCode);
        wrapper.eq("db_name", dbName);
        wrapper.eq("table_name", tableName);
        wrapper.eq("pk_field_name", pkField);
        wrapper.eq("pk_field_value", pkValue);
        stageDataService.remove(wrapper);
        StageData sd = new StageData();
        sd.setDsCode(dsCode);
        sd.setDbName(dbName);
        sd.setTableName(tableName);
        sd.setPkFieldName(pkField);
        sd.setPkFieldValue(pkValue);
        if("0".equals(dataState)){
            dataState = "2";
        }
        rowData.remove("dataState");
        sd.setDataJsonStr(rowData.toJSONString());
        sd.setDataState(dataState);
        stageDataService.save(sd);
        return Result.ok();
    }

    //local_mysql.pmkb-test.aaaaaaa:dpkid.1
    //local_mysql.pmkb-test.a51:dpkid.1
    @PostMapping(value = "/updateReferenceField")
    public Result<?> updateReferenceField(@RequestBody JSONObject jsonObject) {
        String source = jsonObject.getString("sourceNode");
        String target = jsonObject.getString("targetNode");
        String field = jsonObject.getString("field");
        String[] sourceArr = source.split(":");
        String[] sourceTableArr = sourceArr[0].split("\\.");
        String[] sourceFieldArr = sourceArr[1].split("\\.");
        String[] targetArr = target.split(":");
        String[] targetTableArr = targetArr[0].split("\\.");
        String[] targetFieldArr = targetArr[1].split("\\.");
        QueryWrapper<ReferenceRelation> relationQueryWrapper = new QueryWrapper<>();
        relationQueryWrapper.eq("ds_code", sourceTableArr[0]);
        relationQueryWrapper.eq("db_name", sourceTableArr[1]);
        relationQueryWrapper.eq("table_name", sourceTableArr[2]);
        relationQueryWrapper.eq("field_name", field);
        relationQueryWrapper.eq("ref_table_name", targetTableArr[2]);
        relationQueryWrapper.eq("ref_field_name", targetFieldArr[0]);
        ReferenceRelation referenceRelation = referenceRelationService.getOne(relationQueryWrapper);
        String updateFieldName = referenceRelation.getFieldName();

        QueryWrapper<StageData> stageDataQueryWrapper = new QueryWrapper<>();
        stageDataQueryWrapper.eq("ds_code", sourceTableArr[0]);
        stageDataQueryWrapper.eq("db_name", sourceTableArr[1]);
        stageDataQueryWrapper.eq("table_name", sourceTableArr[2]);
        stageDataQueryWrapper.eq("pk_field_name", sourceFieldArr[0]);
        stageDataQueryWrapper.eq("pk_field_value", sourceFieldArr[1]);
        StageData stageData = stageDataService.getOne(stageDataQueryWrapper);
        JSONObject json = JSONObject.parseObject(stageData.getDataJsonStr());
        json.put(updateFieldName, targetTableArr[2] + ":" + targetFieldArr[1]);
        stageData.setDataJsonStr(json.toJSONString());
        stageDataService.updateById(stageData);
        Map result = new HashMap();
        result.put("field", updateFieldName);
        return Result.ok(result);
    }

    @PostMapping(value = "/delReferenceField")
    public Result<?> delReferenceField(@RequestBody JSONObject jsonObject) {
        String source = jsonObject.getString("sourceNode");
        String target = jsonObject.getString("targetNode");
        String field = jsonObject.getString("field");
        String[] sourceArr = source.split(":");
        String[] sourceTableArr = sourceArr[0].split("\\.");
        String[] sourceFieldArr = sourceArr[1].split("\\.");
        String[] targetArr = target.split(":");
        String[] targetTableArr = targetArr[0].split("\\.");
        String[] targetFieldArr = targetArr[1].split("\\.");
        QueryWrapper<ReferenceRelation> relationQueryWrapper = new QueryWrapper<>();
        relationQueryWrapper.eq("ds_code", sourceTableArr[0]);
        relationQueryWrapper.eq("db_name", sourceTableArr[1]);
        relationQueryWrapper.eq("table_name", sourceTableArr[2]);
        relationQueryWrapper.eq("field_name", field);
        relationQueryWrapper.eq("ref_table_name", targetTableArr[2]);
        relationQueryWrapper.eq("ref_field_name", targetFieldArr[0]);
        ReferenceRelation referenceRelation = referenceRelationService.getOne(relationQueryWrapper);
        String updateFieldName = referenceRelation.getFieldName();

        QueryWrapper<StageData> stageDataQueryWrapper = new QueryWrapper<>();
        stageDataQueryWrapper.eq("ds_code", sourceTableArr[0]);
        stageDataQueryWrapper.eq("db_name", sourceTableArr[1]);
        stageDataQueryWrapper.eq("table_name", sourceTableArr[2]);
        stageDataQueryWrapper.eq("pk_field_name", sourceFieldArr[0]);
        stageDataQueryWrapper.eq("pk_field_value", sourceFieldArr[1]);
        StageData stageData = stageDataService.getOne(stageDataQueryWrapper);
        JSONObject json = JSONObject.parseObject(stageData.getDataJsonStr());
        json.remove(updateFieldName);
        stageData.setDataJsonStr(json.toJSONString());
        stageDataService.updateById(stageData);
        Map result = new HashMap();
        result.put("field", updateFieldName);
        return Result.ok(result);
    }

    //local_mysql.pmkb-test.aaaaaaa:dpkid.1
    //a51:1
    @PostMapping(value = "/openReferenceField")
    public Result<?> openReferenceField(@RequestBody JSONObject jsonObject) {
        String id = jsonObject.getString("id");
        String field = jsonObject.getString("field");
        String value = jsonObject.getString("value");
        String[] tmp = id.split(":");
        String[] tableInfo = tmp[0].split("\\.");
        String[] sourceField = tmp[1].split("\\.");
        String dsCode = tableInfo[0];
        String dbName = tableInfo[1];
        String tableName = sourceField[0];
        String pkFieldValue = sourceField[1];
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ds_code", dsCode);
        qw.eq("db_name", dbName);
        String[] target = value.split(":");
        qw.eq("table_name", target[0]);
        qw.eq("pk_field_value", target[1]);
        StageData stageData = stageDataService.getOne(qw);
        JSONObject record = JSONObject.parseObject(stageData.getDataJsonStr());
        record.put("dataState", stageData.getDataState());
        Map result = new HashMap();
        result.put("record", record);
        return Result.ok(result);
    }

    @PostMapping(value = "/reference-relation-by-field")
    public Result<?> referenceRelationByField(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");
        String fildName = jsonObject.getString("fildName");

        List<ReferenceRelation> list = referenceRelationService.listByParam(dsCode,dbName,tableName);

        Map<String, List<String>> result = new HashMap<>();
        for (ReferenceRelation rr:list){
            String fieldName = rr.getFieldName();
            if(result.get(fieldName)!=null){
                result.get(fieldName).add(rr.getDsCode() + "." + rr.getDbName() + "." + rr.getRefTableName());
            }else{
                List<String> tmp = new ArrayList<>();
                tmp.add(rr.getDsCode() + "." + rr.getDbName() + "." + rr.getRefTableName());
                result.put(fieldName, tmp);
            }
        }
        return Result.ok(result);
    }

    @PostMapping(value = "/reference-relation-by-pk")
    public Result<?> referenceRelationByPk(@RequestBody JSONObject jsonObject) {
        String dsCode = jsonObject.getString("dsCode");
        String dbName = jsonObject.getString("dbCode");
        String pk = jsonObject.getString("pk");
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ds_code", dsCode);
        qw.eq("db_name", dbName);
        qw.like("data_json_str", pk);
        List<StageData> list = stageDataService.list(qw);
        List<String> nodeIds = list.stream().map(sd -> {
            StringBuilder sb = new StringBuilder();
            sb.append(sd.getDsCode());
            sb.append(".");
            sb.append(sd.getDbName());
            sb.append(".");
            sb.append(sd.getTableName());
            sb.append(":");
            sb.append(sd.getPkFieldName());
            sb.append(".");
            sb.append(sd.getPkFieldValue());
            return sb.toString();
        }).collect(Collectors.toList());

        QueryWrapper qwr = new QueryWrapper();
        qwr.eq("ds_code", dsCode);
        qwr.eq("db_name", dbName);
        String[] split = pk.split(":");
        qwr.eq("table_name", split[0]);
        qwr.eq("pk_field_value", split[1]);
        StageData one = stageDataService.getOne(qwr);
        JSONObject json = JSONObject.parseObject(one.getDataJsonStr());
        Collection<Object> values = json.values();
        List<Object> rightTables = values.stream().filter(obj -> obj != null && String.valueOf(obj).indexOf(":") > -1).collect(Collectors.toList());
        List<String> rightNodeIds = rightTables.stream().map(r -> {
            String pkNameValue = r.toString();
            String[] split1 = pkNameValue.split(":");
            StringBuilder sb = new StringBuilder();
            sb.append(dsCode);
            sb.append(".");
            sb.append(dbName);
            sb.append(".");
            sb.append(split1[0]);
            sb.append(":");
            sb.append("dpkid");
            sb.append(".");
            sb.append(split1[1]);
            return sb.toString();
        }).collect(Collectors.toList());

        Map result = new HashMap();
        result.put("nodeIds", nodeIds);
        result.put("rightNodeIds", rightNodeIds);
        return Result.ok(result);
    }

    @PostMapping(value = "/del")
    public Result<?> del(@RequestBody JSONObject jsonObject){
        String dsCode = jsonObject.getString("dsCode");
        String dbCode = jsonObject.getString("dbCode");
        String tableName = jsonObject.getString("tableName");
        String pkField = jsonObject.getString("pkField");
        JSONArray ids = jsonObject.getJSONArray("ids");
        List<Integer> idsList = ids.toJavaList(Integer.class);
        UpdateWrapper uw = new UpdateWrapper();
        uw.eq("ds_code", dsCode);
        uw.eq("db_name", dbCode);
        uw.eq("table_name", tableName);
        uw.eq("pk_field_name", pkField);
        uw.in("pk_field_value", idsList);
        stageDataService.remove(uw);
        return Result.ok();
    }

    @PostMapping(value = "/sync")
    public Result<?> sync(@RequestBody JSONObject jsonObject){
        JSONArray ids = jsonObject.getJSONArray("ids");
        List<String> idsList = ids.toJavaList(String.class);
        Map<String, StageData> dataMap = new HashMap<>();
        for(String id:idsList){
            searchReference(id, dataMap);
        }
        for (StageData sd : dataMap.values()) {
            syncTableAndData(sd.getDsCode(),sd.getDbName(),sd.getTableName(),sd);
        }
        return Result.ok();
    }

    private void searchReference(String id,Map<String, StageData> dataMap){
        String[] split = id.split(":");
        String[] tmp = split[0].split("\\.");
        QueryWrapper qw = new QueryWrapper();
        qw.eq("ds_code", tmp[0]);
        qw.eq("db_name", tmp[1]);
        qw.eq("table_name", tmp[2]);
        String[] tmp2 = split[1].split("\\.");
        qw.eq("pk_field_value", tmp2.length>1?tmp2[1]:tmp2[0]);
        qw.ne("data_state", "0");
        StageData one = stageDataService.getOne(qw);
        if(one!=null){
            id = one.getDsCode() + "." + one.getDbName() + "." + one.getTableName() + ":" + one.getPkFieldValue();
            if(!dataMap.containsKey(id)){
                dataMap.put(id, one);
            }
            JSONObject jsonObject = JSONObject.parseObject(one.getDataJsonStr());
            List<Object> collect = jsonObject.values().stream().filter(v -> v != null && v.toString().indexOf(":") > -1).collect(Collectors.toList());
            for (Object obj:collect){
                String nid = one.getDsCode() + "." + one.getDbName() + "." + obj.toString();
                searchReference(nid, dataMap);
            }
        }
    }

    private void syncTableAndData(String dsCode,String dbName,String tableName,StageData sd){
        UpdateWrapper queryWrapper = new UpdateWrapper();
        queryWrapper.eq("ds_code", dsCode);
        queryWrapper.eq("db_name", dbName);
        queryWrapper.eq("table_name", tableName);
        MetadataTables mdTable = tablesService.getOne(queryWrapper);
        if(!"0".equals(mdTable.getDataState())){
            List<MetadataFields> fieldsList = fieldsService.list(queryWrapper);
            if ("1".equals(mdTable.getDataState())) {
                String sql = MetadataUtil.genCreateSql(mdTable, fieldsList);
                DynamicDBUtil.update(mdTable.getDsCode(), sql);
                mdTable.setDataState("0");
                tablesService.updateById(mdTable);
                for (MetadataFields f : fieldsList) {
                    f.setDataState("0");
                    fieldsService.updateById(f);
                }
            }
            else if ("2".equals(mdTable.getDataState())) {
                List<MetadataFields> oldList = fieldsService.list(queryWrapper);
                List<MetadataFields> addList = fieldsList.stream().filter(f -> f.getDataState().equals("1")).collect(Collectors.toList());
                List<MetadataFields> editList = fieldsList.stream().filter(f -> f.getDataState().equals("2")).collect(Collectors.toList());
                List<MetadataFields> editListOld = new ArrayList<>();
                List<MetadataFields> delList = new ArrayList<>();
                oldList.forEach(om -> {
                    final boolean[] del = {true};
                    fieldsList.forEach(nm -> {
                        if (om.getId().equals(nm.getId())) {
                            del[0] = false;
                        }
                    });
                    if (del[0]) {
                        delList.add(om);
                    }
                });
                for (MetadataFields em : editList) {
                    for (MetadataFields om : oldList) {
                        if (em.getId().equals(om.getId())) {
                            editListOld.add(om);
                            break;
                        }
                    }
                }
                String alterTableSql = "ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` COMMENT = '" + mdTable.getTableComment() + "'";
                DynamicDBUtil.update(mdTable.getDsCode(), alterTableSql);
                for (MetadataFields mf : addList) {
                    String sql = "ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` ADD COLUMN `" + mf.getFieldName() + "` " + mf.getFieldTypeFull() + " COMMENT '" + mf.getFieldComment() + "'";
                    DynamicDBUtil.update(mdTable.getDsCode(), sql);
                }
                for (MetadataFields mf : delList) {
                    String sql = "ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` DROP COLUMN `" + mf.getFieldName() + "` ";
                    DynamicDBUtil.update(mdTable.getDsCode(), sql);
                }

                for (int i = 0; i < editList.size(); i++) {
                    MetadataFields emf = editList.get(i);
                    MetadataFields omf = editListOld.get(i);
                    StringBuilder sql = new StringBuilder();
                    sql.append("ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` CHANGE COLUMN `" + omf.getFieldName() + "` `" + emf.getFieldName() + "` " + emf.getFieldTypeFull());
                    if (emf.getFieldTypeFull().startsWith("varchar")) {
                        sql.append(" CHARACTER SET utf8 COLLATE utf8_general_ci ");
                    }
                    if ("0".equals(emf.getIsPk()) && "0".equals(omf.getIsPk())) {
                        sql.append(" NULL COMMENT '" + emf.getFieldComment() + "'");
                    } else if ("1".equals(omf.getIsPk()) && "0".equals(emf.getIsPk())) {
                        sql.append(" NULL COMMENT '" + emf.getFieldComment() + "',");
                        sql.append(" DROP PRIMARY KEY");
                    } else if ("0".equals(omf.getIsPk()) && "1".equals(emf.getIsPk())) {
                        sql.append(" NOT NULL COMMENT '" + emf.getFieldComment() + "',");
                        sql.append(" ADD PRIMARY KEY (`" + emf.getFieldName() + "`)");
                        if (emf.getFieldTypeFull().startsWith("varchar")) {
                            sql.append(" USING BTREE ");
                        }
                    } else if ("1".equals(omf.getIsPk()) && "1".equals(emf.getIsPk())) {
                        sql.append(" NOT NULL COMMENT '" + emf.getFieldComment() + "',");
                        sql.append(" DROP PRIMARY KEY,");
                        sql.append(" ADD PRIMARY KEY (`" + emf.getFieldName() + "`)");
                        if (emf.getFieldTypeFull().startsWith("varchar")) {
                            sql.append(" USING BTREE ");
                        }
                    }
                    DynamicDBUtil.update(mdTable.getDsCode(), sql.toString());
                }
                mdTable.setDataState("0");
                tablesService.update(mdTable, queryWrapper);
                fieldsList.forEach(mf -> {
                    mf.setDataState("0");
                });
                fieldsService.saveBatch(fieldsList);

            }
        }
        List<String> insertSql = MetadataUtil.genInsertSql(Arrays.asList(sd));
        DynamicDBUtil.update(mdTable.getDsCode(), insertSql.get(0));
        sd.setDataState("0");
        stageDataService.updateById(sd);
    }
}
