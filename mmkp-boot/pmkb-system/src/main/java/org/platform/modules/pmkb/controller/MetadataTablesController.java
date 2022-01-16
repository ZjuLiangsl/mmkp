package org.platform.modules.pmkb.controller;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.util.dynamic.db.DataSourceCachePool;
import org.jeecg.common.util.dynamic.db.DynamicDBUtil;
import org.platform.modules.pmkb.entity.*;
import org.platform.modules.pmkb.service.*;
import org.platform.modules.pmkb.util.MetadataUtil;
import org.platform.modules.pmkb.vo.FieldNodeVO;
import org.platform.modules.pmkb.vo.MetadataFieldsVO;
import org.platform.modules.pmkb.vo.MetadataTablesVO;
import org.platform.modules.pmkb.vo.TableNodeVO;
import org.platform.modules.system.entity.SysDataSource;
import org.platform.modules.system.entity.SysUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@RestController
@RequestMapping("/md/tables")
public class MetadataTablesController {
	@Autowired
	private IMetadataTablesService tablesService;
	@Autowired
	private IMetadataFieldsService fieldsService;
	@Autowired
	private IMetadataTablesExtService tablesExtService;
	@Autowired
	private IMetadataFieldsExtService fieldsExtService;
	@Autowired
	IMetadataService metadataService;
	@Autowired
	private IStageDataService stageDataService;
	@Autowired
	private IReferenceRelationService referenceRelationService;


	@RequestMapping(value = "/getDbDict", method = RequestMethod.GET)
	public Result<List<DictModel>> getDictItems(HttpServletRequest request) {
		Result<List<DictModel>> result = new Result<List<DictModel>>();
		try {
			QueryWrapper queryWrapper = new QueryWrapper();
			queryWrapper.select("db_name");
			queryWrapper.groupBy("db_name");
			List<MetadataTables> list = tablesService.list(queryWrapper);
			List<DictModel> ls = list.stream().map(mt -> {
				DictModel dm = new DictModel();
				dm.setText(mt.getDbName());
				dm.setValue(mt.getDbName());
				return dm;
			}).collect(Collectors.toList());
//			if (ls==null || ls.size()==0) {
//				result.error500("未采集元数据！");
//				return result;
//			}
			result.setSuccess(true);
			result.setResult(ls);
			log.debug(result.toString());
		} catch (Exception e) {
			log.error(e.getMessage(), e);
			result.error500("操作失败");
			return result;
		}
		return result;
	}

	@RequestMapping(value = "/list", method = RequestMethod.GET)
	public Result<IPage<MetadataTablesVO>> queryPageList(MetadataTablesVO tablesVO, @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
                                                @RequestParam(name="pageSize", defaultValue="10") Integer pageSize, HttpServletRequest req) {
		Result<IPage<MetadataTablesVO>> result = new Result<>();
		Map<String, String[]> requestParams  = new HashMap<>(req.getParameterMap());
		Map<String, Object> paramsMap = new HashMap<>(requestParams.size());
		for (Map.Entry<String, String[]> entry : requestParams.entrySet()) {
			Object value = (entry.getValue() == null) ? null : entry.getValue()[0];
			String valueStr = "";
			if(value!=null){
				valueStr = String.valueOf(value);
				if(valueStr.length()>1 && valueStr.indexOf("*")==0 && valueStr.lastIndexOf("*")==valueStr.length()-1){
					valueStr = valueStr.substring(1, valueStr.length() - 1);
				}
			}
			paramsMap.put(entry.getKey(), valueStr);
		}
		paramsMap.put("dataState", "0");
		Page<MetadataTablesVO> page = new Page<>(pageNo, pageSize);
		IPage<MetadataTablesVO> pageList = tablesService.pageTables(page, paramsMap);
		result.setSuccess(true);
		result.setResult(pageList);
		log.info(pageList.toString());
		return result;
	}

	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name = "id") String id) {
		MetadataTables table = tablesService.getById(id);
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", table.getDsCode());
		wrapper.eq("db_name", table.getDbName());
		wrapper.eq("table_name", table.getTableName());
		fieldsService.remove(wrapper);
		fieldsExtService.remove(wrapper);
		tablesExtService.remove(wrapper);
		referenceRelationService.remove(wrapper);
		stageDataService.remove(wrapper);
		tablesService.removeById(id);
		return Result.ok("删除成功!");
	}

	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name = "ids") String ids) {
		List<String> idList = Arrays.asList(ids.split(","));
		idList.forEach(item->{
			MetadataTables table = tablesService.getById(item);
			QueryWrapper wrapper = new QueryWrapper();
			wrapper.eq("ds_code", table.getDsCode());
			wrapper.eq("db_name", table.getDbName());
			wrapper.eq("table_name", table.getTableName());
			fieldsService.remove(wrapper);
			fieldsExtService.remove(wrapper);
			tablesExtService.remove(wrapper);
		});
		this.tablesService.removeByIds(idList);
		return Result.ok("批量删除成功！");
	}

	@PostMapping(value = "/refresh")
	public Result<?> refresh(@RequestBody JSONObject jsonObject){
		try {
			String dsCode = jsonObject.getString("dsCode");
			String dbName = jsonObject.getString("dbName");
			String tablesName = jsonObject.getString("tableName");
			QueryWrapper queryWrapper = new QueryWrapper();
			queryWrapper.eq("ds_code", dsCode);
			queryWrapper.eq("db_name", dbName);
			queryWrapper.eq("table_name", tablesName);
			MetadataTables mdTable = tablesService.getOne(queryWrapper);
			List<MetadataTables> tablesList = new ArrayList<>();
			List<List<MetadataFields>> fieldsList = new ArrayList<>();
			if("1".equals(mdTable.getDataState())){
				findFkTable(tablesList, fieldsList, mdTable);
				for (int i = tablesList.size()-1; i > -1; i--) {
					MetadataTables t = tablesList.get(i);
					List<MetadataFields> fl = fieldsList.get(i);
					String sql = MetadataUtil.genCreateSql(t, fl);
					QueryWrapper qw = new QueryWrapper();
					qw.eq("ds_code", t.getDsCode());
					qw.eq("db_name", t.getDbName());
					qw.eq("table_name", t.getTableName());
					List<StageData> list = stageDataService.list(qw);
					List<String> insertSqlList = MetadataUtil.genInsertSql(list);
					DynamicDBUtil.update(t.getDsCode(), sql);
					for(String inrsql:insertSqlList){
						DynamicDBUtil.update(t.getDsCode(), inrsql);
					}
					t.setDataState("0");
					tablesService.updateById(t);
					for(MetadataFields f:fl){
						f.setDataState("0");
						fieldsService.updateById(f);
					}
					stageDataService.remove(qw);
				}
			}else if("2".equals(mdTable.getDataState())){

			}else{
				metadataService.extractMetadata(dsCode,Arrays.asList(dbName),Arrays.asList(tablesName));
			}

		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result.ok("ok");
	}

	private void searchReference(String id,List<String> idList){
		String[] split = id.split("\\.");
		QueryWrapper qw = new QueryWrapper();
		qw.eq("ds_code", split[0]);
		qw.eq("db_name", split[1]);
		qw.eq("table_name", split[2]);
		List<ReferenceRelation> list = referenceRelationService.list(qw);
		if(list.size()>0){
			for(ReferenceRelation rr:list){
				String rid = rr.getDsCode() + "." + rr.getDbName() + "." + rr.getRefTableName();
				if(!idList.contains(rid)){
					idList.add(rid);
					searchReference(rid,idList);
				}
			}
		}
	}
	private Map syncTable(String id){
		String[] split = id.split("\\.");
		String dsCode = split[0];
		String dbName = split[1];
		String tablesName = split[2];
		UpdateWrapper queryWrapper = new UpdateWrapper();
		queryWrapper.eq("ds_code", dsCode);
		queryWrapper.eq("db_name", dbName);
		queryWrapper.eq("table_name", tablesName);
		MetadataTables mdTable = tablesService.getOne(queryWrapper);
		List<MetadataFields> fieldsList = fieldsService.list(queryWrapper);
		if(mdTable!=null) {
			if ("1".equals(mdTable.getDataState())) {
				String sql = MetadataUtil.genCreateSql(mdTable, fieldsList);
				QueryWrapper qw = new QueryWrapper();
				qw.eq("ds_code", mdTable.getDsCode());
				qw.eq("db_name", mdTable.getDbName());
				qw.eq("table_name", mdTable.getTableName());
//				List<StageData> list = stageDataService.list(qw);
//				List<String> insertSqlList = MetadataUtil.genInsertSql(list);
//				DynamicDBUtil.update(mdTable.getDsCode(), sql);
//				for (String inrsql : insertSqlList) {
//					DynamicDBUtil.update(mdTable.getDsCode(), inrsql);
//				}
				mdTable.setDataState("0");
				tablesService.updateById(mdTable);
				for (MetadataFields f : fieldsList) {
					f.setDataState("0");
					fieldsService.updateById(f);
				}
//				queryWrapper.set("data_state", "0");
//				stageDataService.remove(queryWrapper);
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
//				List<StageData> list = stageDataService.list(queryWrapper);
//				List<String> insertSqlList = MetadataUtil.genInsertSql(list);
//				for (String inrsql : insertSqlList) {
//					DynamicDBUtil.update(mdTable.getDsCode(), inrsql);
//				}
				mdTable.setDataState("0");
				tablesService.update(mdTable, queryWrapper);
				fieldsList.forEach(mf -> {
					mf.setDataState("0");
				});
				fieldsService.saveBatch(fieldsList);

//				queryWrapper.set("data_state", "0");
//				stageDataService.remove(queryWrapper);
			}
		}

		//结构未变化仅同步数据
		List<StageData> list = stageDataService.list(queryWrapper);
		List<String> insertSqlList = MetadataUtil.genInsertSql(list);
		for(String inrsql:insertSqlList){
			DynamicDBUtil.update(mdTable.getDsCode(), inrsql);
		}
		queryWrapper.set("data_state", "0");
		stageDataService.update(queryWrapper);

		Map currentNode = getNodeDataByKey(mdTable.getDsCode() + "." + mdTable.getDbName() + "." + mdTable.getTableName());
		return currentNode;
	}

	@PostMapping(value = "/sync")
	public Result<?> sync(@RequestBody JSONObject jsonObject){
		Map result = new HashMap();
		try {
			List<String> idList = new ArrayList<>();
			String ids = jsonObject.getString("ids");
			String[] idArr = ids.split(",");
			for(String id:idArr){
				if(!idList.contains(id)){
					idList.add(id);
					searchReference(id,idList);
				}
			}

			List<Map> nodeList = new ArrayList<>();
			for (String id:idList){
				Map map = syncTable(id);
				nodeList.add(map);
			}

			result.put("ndoeList", nodeList);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result.ok(result);
	}

	private void findFkTable(List<MetadataTables> tablesList, List<List<MetadataFields>> fieldsList,MetadataTables table){
		QueryWrapper queryWrapper = new QueryWrapper();
		queryWrapper.eq("ds_code", table.getDsCode());
		queryWrapper.eq("db_name", table.getDbName());
		queryWrapper.eq("table_name", table.getTableName());
		List<MetadataFields> flist = fieldsService.list(queryWrapper);
		if(flist!=null){
			tablesList.add(table);
			fieldsList.add(flist);
			for (MetadataFields f:flist){
				if (StrUtil.isNotBlank(f.getFkDbName()) && StrUtil.isNotBlank(f.getFkTableName())) {
					QueryWrapper qw = new QueryWrapper();
					qw.eq("ds_code", table.getDsCode());
					qw.eq("db_name", f.getFkDbName());
					qw.eq("table_name", f.getFkTableName());
					MetadataTables one = tablesService.getOne(qw);
					if(one!=null){
						findFkTable(tablesList, fieldsList, one);
					}
				}
			}
		}
	}

	@GetMapping(value= "/table-fields")
	public Result<Map> views(String id){
		MetadataTables tablePo = tablesService.getById(id);
		Result<Map> result = new Result<>();
		Map map = new HashMap();
		MetadataTablesVO tableVO = tablesService.getMetadataTablesVO(tablePo);
		List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(tablePo);
		listFieldsVO.stream().forEach(field->{
			if(StrUtil.isBlank(field.getFieldCompType())) {
				if (StrUtil.isNotBlank(field.getFkTableName())) {
					field.setFieldCompType(MetadataUtil.SEARCH);
				} else {
					field.setFieldCompType(MetadataUtil.compSelector(field.getFieldType().toLowerCase()));
				}
			}
		});
		map.put("table", tableVO);
		map.put("fields", listFieldsVO);
		result.setSuccess(true);
		result.setResult(map);
		return result;
	}

	@GetMapping(value= "/table-fields-by-params")
	public Result<Map> views(String dsCode,String dbName,String tableName){
		MetadataTables tablePo = new MetadataTables();
		tablePo.setDsCode(dsCode);
		tablePo.setDbName(dbName);
		tablePo.setTableName(tableName);
		Result<Map> result = new Result<>();
		Map map = new HashMap();
		MetadataTablesVO tableVO = tablesService.getMetadataTablesVO(tablePo);
		List<MetadataFieldsVO> listFieldsVO = fieldsService.listMetadataFieldsVO(tablePo);
		listFieldsVO.stream().forEach(field->{
			if(StrUtil.isBlank(field.getFieldCompType())) {
				if (StrUtil.isNotBlank(field.getFkTableName())) {
					field.setFieldCompType(MetadataUtil.SEARCH);
				} else {
					field.setFieldCompType(MetadataUtil.compSelector(field.getFieldType().toLowerCase()));
				}
			}
		});
		map.put("table", tableVO);
		map.put("fields", listFieldsVO);
		result.setSuccess(true);
		result.setResult(map);
		return result;
	}

	@PostMapping(value = "/saveExt")
	public Result<?> saveExt(@RequestBody JSONObject jsonObject){
		try {
			JSONObject table = jsonObject.getJSONObject("table");
			JSONArray fields = jsonObject.getJSONArray("fields");

			UpdateWrapper<MetadataTablesExt> wrapper = new UpdateWrapper();
			wrapper.eq("ds_code", table.getString("dsCode"));
			wrapper.eq("db_name", table.getString("dbName"));
			wrapper.eq("table_name", table.getString("tableName"));
			List<MetadataTablesExt> list = tablesExtService.list(wrapper);
			if(list!=null && list.size()>0) {
				wrapper.set("table_remark", table.getString("tableRemark"));
				wrapper.set("is_paging", table.getString("isPaging"));
				wrapper.set("form_width", table.getInteger("formWidth"));
				tablesExtService.update(wrapper);
			}else{
				MetadataTablesExt metadataTablesExt = JSON.parseObject(table.toJSONString(), MetadataTablesExt.class);
				tablesExtService.save(metadataTablesExt);
			}
			List<MetadataFieldsExt> fieldsExt = fields.stream().map(f -> BeanUtil.mapToBean((Map<?, ?>) f, MetadataFieldsExt.class, true)).collect(Collectors.toList());
			fieldsExtService.saveOrUpdateBatch(fieldsExt);
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(e.getMessage());
		}
		return Result.ok("ok");
	}

	@PostMapping(value = "/save-remote")
	public Result<?> saveRemote(@RequestBody JSONObject jsonObject){
		JSONObject table = jsonObject.getJSONObject("table");
		JSONArray fields = jsonObject.getJSONArray("fields");
		boolean hasExt = jsonObject.getBoolean("hasExt");
		boolean isAdd = jsonObject.getBoolean("isAdd");

		MetadataTables mdTable = JSON.parseObject(table.toJSONString(), MetadataTables.class);
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", mdTable.getDsCode());
		wrapper.eq("db_name", mdTable.getDbName());
		wrapper.eq("table_name", mdTable.getTableName());

		List<MetadataFieldsVO> mdFieldVoList = fields.stream().map(f -> BeanUtil.mapToBean((Map<?, ?>) f, MetadataFieldsVO.class, true)).collect(Collectors.toList());
		List<MetadataFields> mdFieldList = new ArrayList<>();
		for (MetadataFieldsVO vo : mdFieldVoList) {
			if (StrUtil.isBlank(vo.getDsCode())) {
				vo.setDsCode(mdTable.getDsCode());
			}
			if (StrUtil.isBlank(vo.getDbName())) {
				vo.setDbName(mdTable.getDbName());
			}
			if(StrUtil.isBlank(vo.getTableName())){
				vo.setTableName(mdTable.getTableName());
			}
			MetadataFields mdField = new MetadataFields();
			BeanUtil.copyProperties(vo, mdField);
			if(vo.getId()==null || vo.getId().startsWith("field-add-")){
				mdField.setId(null);
			}
			if(StrUtil.isNotBlank(mdField.getFkTableName()) || StrUtil.isNotBlank(mdField.getFkFieldName())){
				mdField.setFkDbName(vo.getDbName());
			}
			if("varchar".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("varchar(" + mdField.getFieldLength() + ")");
			}else if("decimal".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("decimal(20,0)");
			}else if("reference".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("varchar(500)");
			}else {
				mdField.setFieldTypeFull(mdField.getFieldType());
			}
			mdFieldList.add(mdField);
		}
		if(isAdd){
			String sql = MetadataUtil.genCreateSql(mdTable, mdFieldList);
			DynamicDBUtil.update(mdTable.getDsCode(), sql);

			mdTable.setId(null);
			mdTable.setDataState("0");
			tablesService.save(mdTable);
		}
		else{
			List<MetadataFields> oldList = fieldsService.list(wrapper);
			List<MetadataFields> addList = mdFieldList.stream().filter(f -> f.getDataState().equals("1")).collect(Collectors.toList());
			List<MetadataFields> editList = mdFieldList.stream().filter(f -> f.getDataState().equals("2")).collect(Collectors.toList());
			List<MetadataFields> editListOld = new ArrayList<>();
			List<MetadataFields> delList = new ArrayList<>();
			oldList.forEach(om->{
				final boolean[] del = {true};
				mdFieldList.forEach(nm->{
					if(om.getId().equals(nm.getId())){
						del[0] = false;
					}
				});
				if(del[0]){
					delList.add(om);
				}
			});
			for (MetadataFields em:editList){
				for(MetadataFields om:oldList){
					if (em.getId().equals(om.getId())) {
						editListOld.add(om);
						break;
					}
				}
			}
			String alterTableSql = "ALTER TABLE `"+mdTable.getDbName()+"`.`"+mdTable.getTableName()+"` COMMENT = '" + mdTable.getTableComment() + "'";
			DynamicDBUtil.update(mdTable.getDsCode(), alterTableSql);
			for (MetadataFields mf:addList){
				String sql = "ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` ADD COLUMN `" + mf.getFieldName() + "` " + mf.getFieldTypeFull() + " COMMENT '" + mf.getFieldComment() + "'";
				DynamicDBUtil.update(mdTable.getDsCode(), sql);
			}
			for (MetadataFields mf:delList){
				QueryWrapper refWrapper = new QueryWrapper();
				refWrapper.eq("ds_code", mdTable.getDsCode());
				refWrapper.eq("db_name", mdTable.getDbName());
				refWrapper.eq("table_name", mdTable.getTableName());
				refWrapper.eq("field_name", mf.getFieldName());
				referenceRelationService.remove(refWrapper);

				String sql = "ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` DROP COLUMN `" + mf.getFieldName() + "` ";
				DynamicDBUtil.update(mdTable.getDsCode(), sql);
			}

			for (int i = 0; i < editList.size(); i++) {
				MetadataFields emf = editList.get(i);
				MetadataFields omf = editListOld.get(i);
				StringBuilder sql = new StringBuilder();
				sql.append("ALTER TABLE `" + mdTable.getDbName() + "`.`" + mdTable.getTableName() + "` CHANGE COLUMN `" + omf.getFieldName() + "` `" + emf.getFieldName() + "` " + emf.getFieldTypeFull());
				if(emf.getFieldTypeFull().startsWith("varchar")){
					sql.append(" CHARACTER SET utf8 COLLATE utf8_general_ci ");
				}
				if("0".equals(emf.getIsPk()) && "0".equals(omf.getIsPk())){
					sql.append(" NULL COMMENT '"+emf.getFieldComment()+"'");
				}else if("1".equals(omf.getIsPk()) && "0".equals(emf.getIsPk())){
					sql.append(" NULL COMMENT '"+emf.getFieldComment()+"',");
					sql.append(" DROP PRIMARY KEY");
				}else if("0".equals(omf.getIsPk()) && "1".equals(emf.getIsPk())){
					sql.append(" NOT NULL COMMENT '"+emf.getFieldComment()+"',");
					sql.append(" ADD PRIMARY KEY (`"+emf.getFieldName()+"`)");
					if(emf.getFieldTypeFull().startsWith("varchar")){
						sql.append(" USING BTREE ");
					}
				}else if("1".equals(omf.getIsPk()) && "1".equals(emf.getIsPk())){
					sql.append(" NOT NULL COMMENT '"+emf.getFieldComment()+"',");
					sql.append(" DROP PRIMARY KEY,");
					sql.append(" ADD PRIMARY KEY (`"+emf.getFieldName()+"`)");
					if(emf.getFieldTypeFull().startsWith("varchar")){
						sql.append(" USING BTREE ");
					}
				}

				UpdateWrapper refWrapper = new UpdateWrapper();
				refWrapper.eq("ds_code", mdTable.getDsCode());
				refWrapper.eq("db_name", mdTable.getDbName());
				refWrapper.eq("table_name", mdTable.getTableName());
				refWrapper.eq("field_name", omf.getFieldName());
				refWrapper.set("field_name", emf.getFieldName());

				referenceRelationService.update(refWrapper);
				DynamicDBUtil.update(mdTable.getDsCode(), sql.toString());
			}
			mdTable.setId(null);
			mdTable.setDataState("0");
			tablesService.update(mdTable, wrapper);
		}
		fieldsService.remove(wrapper);
		mdFieldList.forEach(mf->{
			mf.setDataState("0");
		});
		fieldsService.saveBatch(mdFieldList);

		return Result.ok("ok");
	}

	@PostMapping(value = "/save")
	public Result<?> save(@RequestBody JSONObject jsonObject){
		JSONObject table = jsonObject.getJSONObject("table");
		JSONArray fields = jsonObject.getJSONArray("fields");
		boolean hasExt = jsonObject.getBoolean("hasExt");
		boolean isAdd = jsonObject.getBoolean("isAdd");

		boolean isReferenceAdd = jsonObject.getBoolean("isReferenceAdd");
		String primaryField = jsonObject.getString("primaryField");
		String primaryNodeId = jsonObject.getString("primaryNodeId");

		MetadataTables mdTable = JSON.parseObject(table.toJSONString(), MetadataTables.class);
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", mdTable.getDsCode());
		wrapper.eq("db_name", mdTable.getDbName());
		wrapper.eq("table_name", mdTable.getTableName());

		List<MetadataFieldsVO> mdFieldVoList = fields.stream().map(f -> BeanUtil.mapToBean((Map<?, ?>) f, MetadataFieldsVO.class, true)).collect(Collectors.toList());
		List<MetadataFields> mdFieldList = new ArrayList<>();
		List<MetadataFieldsExt> mdFieldExtList = new ArrayList<>();
		List<String> edgeIds = new ArrayList<>();
		for (MetadataFieldsVO vo : mdFieldVoList) {
			if (StrUtil.isBlank(vo.getDsCode())) {
				vo.setDsCode(mdTable.getDsCode());
			}
			if (StrUtil.isBlank(vo.getDbName())) {
				vo.setDbName(mdTable.getDbName());
			}
			if(StrUtil.isBlank(vo.getTableName())){
				vo.setTableName(mdTable.getTableName());
			}
			MetadataFields mdField = new MetadataFields();
			BeanUtil.copyProperties(vo, mdField);
			if(vo.getId()==null || vo.getId().startsWith("field-add-")){
				mdField.setId(null);
			}
			if(StrUtil.isNotBlank(mdField.getFkTableName()) || StrUtil.isNotBlank(mdField.getFkFieldName())){
				mdField.setFkDbName(vo.getDbName());
			}
			if("varchar".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("varchar(" + mdField.getFieldLength() + ")");
			}else if("decimal".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("decimal(20,0)");
			}else if("reference".equals(mdField.getFieldType())){
				mdField.setFieldTypeFull("varchar(500)");
			}else {
				mdField.setFieldTypeFull(mdField.getFieldType());
			}
			mdFieldList.add(mdField);

			if(hasExt){
				MetadataFieldsExt mdFieldExt = new MetadataFieldsExt();
				BeanUtil.copyProperties(vo, mdFieldExt);
				if(vo.getId()==null || vo.getId().startsWith("field-add-")){
					mdFieldExt.setId(null);
				}
				if("varchar".equals(vo.getFieldType())){
					mdFieldExt.setFieldTypeFull("varchar(" + vo.getFieldLength() + ")");
				}else if("decimal".equals(vo.getFieldType())){
					mdFieldExt.setFieldTypeFull("decimal(20,0)");
				}else {
					mdFieldExt.setFieldTypeFull(vo.getFieldType());
				}
				mdFieldExtList.add(mdFieldExt);
			}
		}

		if(isAdd){
			mdTable.setId(null);
			mdTable.setDataState("1");
			tablesService.save(mdTable);
		}
		else{

			List<MetadataFields> oldList = fieldsService.list(wrapper);
			List<MetadataFields> editList = mdFieldList.stream().filter(f -> f.getDataState().equals("2")).collect(Collectors.toList());
			List<MetadataFields> editListOld = new ArrayList<>();
			List<MetadataFields> delList = new ArrayList<>();
			oldList.forEach(om->{
				final boolean[] del = {true};
				mdFieldList.forEach(nm->{
					if(om.getId().equals(nm.getId())){
						del[0] = false;
					}
				});
				if(del[0]){
					delList.add(om);
				}
			});
			for (MetadataFields em:editList){
				for(MetadataFields om:oldList){
					if (em.getId().equals(om.getId())) {
						editListOld.add(om);
						break;
					}
				}
			}

			for (MetadataFields mf:delList){
				QueryWrapper refWrapper = new QueryWrapper();
				refWrapper.eq("ds_code", mdTable.getDsCode());
				refWrapper.eq("db_name", mdTable.getDbName());
				refWrapper.eq("table_name", mdTable.getTableName());
				refWrapper.eq("field_name", mf.getFieldName());
				referenceRelationService.remove(refWrapper);
			}

			for (int i = 0; i < editList.size(); i++) {
				MetadataFields emf = editList.get(i);
				MetadataFields omf = editListOld.get(i);

				UpdateWrapper refWrapper = new UpdateWrapper();
				refWrapper.eq("ds_code", mdTable.getDsCode());
				refWrapper.eq("db_name", mdTable.getDbName());
				refWrapper.eq("table_name", mdTable.getTableName());
				refWrapper.eq("field_name", omf.getFieldName());
				refWrapper.set("field_name", emf.getFieldName());

				referenceRelationService.update(refWrapper);
			}

			MetadataTables oldTable = tablesService.getOne(wrapper);
			mdTable.setId(oldTable.getId());
			if(oldTable!=null && "0".equals(oldTable.getDataState())){
				mdTable.setDataState("2");
			}else{
				mdTable.setDataState("1");
			}
			tablesService.updateById(mdTable);
		}
		fieldsService.remove(wrapper);
		fieldsService.saveBatch(mdFieldList);

		if(isReferenceAdd){
			ReferenceRelation relation = new ReferenceRelation();
			String[] primaryNode = primaryNodeId.split("\\.");
			relation.setDsCode(primaryNode[0]);
			relation.setDbName(primaryNode[1]);
			relation.setTableName(primaryNode[2]);
			relation.setFieldName(primaryField);
			relation.setRefTableName(mdTable.getTableName());
			relation.setRefFieldName("dpkid");
			referenceRelationService.save(relation);
		}

		if(mdFieldExtList.size()>0){
			fieldsExtService.remove(wrapper);
			fieldsExtService.saveBatch(mdFieldExtList);
		}

		/////////////////////////
		Map result = new HashMap();
		Map currentNode = getNodeDataByKey(mdTable.getDsCode() + "." + mdTable.getDbName() + "." + mdTable.getTableName());
		result.put("node", currentNode);
		String tableNodeKey = mdTable.getDsCode() + "." + mdTable.getDbName() + "." + mdTable.getTableName();
		result.put("edgeIdStart", tableNodeKey);
		List<ReferenceRelation> list = referenceRelationService.list(wrapper);
		list.stream().forEach(rr->{
			String edgeId = tableNodeKey + "." + rr.getFieldName() + "_to_" + mdTable.getDsCode() + "." + mdTable.getDbName() + "." + rr.getRefTableName() + "." + rr.getRefFieldName();
			if(!edgeIds.contains(edgeId)){
				edgeIds.add(edgeId);
			}
		});
		result.put("edgeIds", edgeIds);
		if(isReferenceAdd){
			Map  referenceNode = getNodeDataByKey(primaryNodeId);
			result.put("refNode", referenceNode);
			JSONObject refEdge = new JSONObject();
			refEdge.put("id", primaryNodeId+"."+primaryField + "_to_" + mdTable.getDsCode() + "." + mdTable.getDbName() + "." + mdTable.getTableName()+".dpkid");
			refEdge.put("sourceNode", primaryNodeId);
			refEdge.put("targetNode", mdTable.getDsCode() + "." + mdTable.getDbName() + "." + mdTable.getTableName());
			refEdge.put("source", primaryField);
			refEdge.put("target", "dpkid");
			result.put("refEdge", refEdge);
		}
		return Result.ok(result);
	}

	@PostMapping(value = "/setReferenceTable")
	public Result<?> setReferenceTable(@RequestBody JSONObject jsonObject){
		String nodeId = jsonObject.getString("nodeId");
		String field = jsonObject.getString("field");
		String pkField = jsonObject.getString("pkField");

		String[] tmp = nodeId.split("\\.");
		QueryWrapper qw = new QueryWrapper();
		qw.eq("ds_code", tmp[0]);
		qw.eq("db_name", tmp[1]);
		qw.eq("table_name", tmp[2]);
		qw.eq("field_name", field);
		qw.eq("ref_table_name", tmp[2]);
		qw.eq("ref_field_name", pkField);
		ReferenceRelation one = referenceRelationService.getOne(qw);
		if(one!=null){
			referenceRelationService.remove(qw);
		}else{
			referenceRelationService.remove(qw);
			ReferenceRelation rr = new ReferenceRelation();
			rr.setDsCode(tmp[0]);
			rr.setDbName(tmp[1]);
			rr.setTableName(tmp[2]);
			rr.setFieldName(field);
			rr.setRefTableName(tmp[2]);
			rr.setRefFieldName(pkField);
			referenceRelationService.save(rr);
		}

		/////////////////////////
		Map result = new HashMap();
		Map currentNode = getNodeDataByKey(nodeId);
		result.put("node", currentNode);
		String tableNodeKey = nodeId;
		result.put("edgeIdStart", tableNodeKey);
		List<String> edgeIds = new ArrayList<>();
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", tmp[0]);
		wrapper.eq("db_name", tmp[1]);
		wrapper.eq("table_name", tmp[2]);
		List<ReferenceRelation> list = referenceRelationService.list(wrapper);
		list.stream().forEach(rr->{
			String edgeId = tableNodeKey + "." + rr.getFieldName() + "_to_" + tmp[0] + "." + tmp[1] + "." + rr.getRefTableName() + "." + rr.getRefFieldName();
			if(!edgeIds.contains(edgeId)){
				edgeIds.add(edgeId);
			}
		});
		result.put("edgeIds", edgeIds);
		return Result.ok(result);
	}

	@PostMapping(value = "/saveEdge")
	public Result<?> saveEdge(@RequestBody JSONObject jsonObject) {
		String id = jsonObject.getString("id");
		String source = jsonObject.getString("source");
		String sourceNode = jsonObject.getString("sourceNode");
		String target = jsonObject.getString("target");
		String targetNode = jsonObject.getString("targetNode");

		String[] sourceArr = sourceNode.split("\\.");
		String[] targetArr = targetNode.split("\\.");
		ReferenceRelation rr = new ReferenceRelation();
		rr.setDsCode(sourceArr[0]);
		rr.setDbName(sourceArr[1]);
		rr.setTableName(sourceArr[2]);
		rr.setFieldName(source);
		rr.setRefTableName(targetArr[2]);
		rr.setRefFieldName(target);
		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", sourceArr[0]);
		wrapper.eq("db_name", sourceArr[1]);
		wrapper.eq("table_name", sourceArr[2]);
		wrapper.eq("field_name", source);
		wrapper.eq("ref_table_name", targetArr[2]);
		wrapper.eq("ref_field_name", target);
		referenceRelationService.remove(wrapper);
		referenceRelationService.save(rr);

		Map result = new HashMap();
		Map  referenceNode = getNodeDataByKey(sourceNode);
		result.put("refNode", referenceNode);

		return Result.ok(result);
	}

	@PostMapping(value = "/delEdge")
	public Result<?> delEdge(@RequestBody JSONObject jsonObject) {
		String id = jsonObject.getString("id");
		String[] ids = id.split("_to_");
		String[] source = ids[0].split("\\.");
		String[] target = ids[1].split("\\.");
		String sourceNode = source[0] + "." + source[1] + "." + source[2];
		QueryWrapper<ReferenceRelation> wrapper = new QueryWrapper<>();
		wrapper.eq("ds_code", source[0]);
		wrapper.eq("db_name", source[1]);
		wrapper.eq("table_name", source[2]);
		wrapper.eq("field_name", source[3]);
		wrapper.eq("ref_table_name", target[2]);
		wrapper.eq("ref_field_name", target[3]);
		referenceRelationService.remove(wrapper);

		Map result = new HashMap();
		Map  referenceNode = getNodeDataByKey(sourceNode);
		result.put("refNode", referenceNode);

		return Result.ok(result);
	}

	@PostMapping(value = "/remove")
	public Result<?> remove(@RequestBody JSONObject jsonObject) {
		String dsCode = jsonObject.getString("dsCode");
		String dbName = jsonObject.getString("dbName");
		String tableName = jsonObject.getString("tableName");

		QueryWrapper wrapper = new QueryWrapper();
		wrapper.eq("ds_code", dsCode);
		wrapper.eq("db_name", dbName);
		wrapper.eq("table_name", tableName);
		MetadataTables one = tablesService.getOne(wrapper);
		if ("1".equals(one.getDataState())) {
//            QueryWrapper fkWrapper = new QueryWrapper();
//            fkWrapper.eq("fk_db_name", dbName);
//            fkWrapper.eq("fk_table_name", tableName);
//            List<MetadataFields> fkList = fieldsService.list(fkWrapper);
//            if(fkList!=null && fkList.size()>0){
//                List<String> collect = fkList.stream().map(f -> f.getTableName()).collect(Collectors.toList());
//                return Result.error("当前表被" + String.join(",", collect) + "外键引用,不能删除");
//            }

			tablesExtService.remove(wrapper);
			fieldsExtService.remove(wrapper);
			stageDataService.remove(wrapper);
			referenceRelationService.remove(wrapper);
			wrapper.in("data_state", "1");
			tablesService.remove(wrapper);
			fieldsService.remove(wrapper);
		}
		else if("2".equals(one.getDataState())){
			try {
				stageDataService.remove(wrapper);
				metadataService.extractMetadata(dsCode, Arrays.asList(dbName),Arrays.asList(tableName));
				referenceRelationService.remove(wrapper);
			} catch (Exception e) {
				e.printStackTrace();
				return Result.error(e.getMessage());
			}
		}
		else{
			stageDataService.remove(wrapper);
			referenceRelationService.remove(wrapper);
		}
		QueryWrapper qw = new QueryWrapper();
		qw.eq("ds_code", dsCode);
		qw.eq("db_name", dbName);
		qw.eq("ref_table_name", tableName);

		List<ReferenceRelation> list = referenceRelationService.list(qw);
		referenceRelationService.remove(qw);

		List leftNodes = new ArrayList();
		list.forEach(rr->{
			String key = rr.getDsCode() + "." + rr.getDbName() + "." + rr.getTableName();
			leftNodes.add(getNodeDataByKey(key));
		});

		Map result = new HashMap();
		result.put("leftNodes", leftNodes);

		return Result.ok(result);
	}

	private Map getNodeDataByKey(String key) {
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
		Map result = new HashMap();
		result.put("stageField", stageField);
		result.put("editField", editField);
		result.put("referenceField", referenceField);
		result.put("pkField", pkField);
		result.put("referenceMap", referenceMap);
		result.put("tableState", tableState);
		result.put("nodeData", tableNode);
		result.put("key", key);
		return result;
	}
}
