package org.platform.modules.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.platform.modules.system.entity.SysDict;
import org.platform.modules.system.entity.SysDictItem;
import org.platform.modules.system.mapper.SysDictItemMapper;
import org.platform.modules.system.mapper.SysDictMapper;
import org.platform.modules.system.model.TreeSelectModel;
import org.platform.modules.system.service.ISysDictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * <p>
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Service
@Slf4j
public class SysDictServiceImpl extends ServiceImpl<SysDictMapper, SysDict> implements ISysDictService {

    @Autowired
    private SysDictMapper sysDictMapper;
    @Autowired
    private SysDictItemMapper sysDictItemMapper;

	/**
	 * @param code
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryDictItemsByCode(String code) {
		return sysDictMapper.queryDictItemsByCode(code);
	}

	@Override
	@Cacheable(value = CacheConstant.SYS_ENABLE_DICT_CACHE,key = "#code", unless = "#result == null ")
	public List<DictModel> queryEnableDictItemsByCode(String code) {
		return sysDictMapper.queryEnableDictItemsByCode(code);
	}

	@Override
	public Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList) {
		List<DictModelMany> list = sysDictMapper.queryDictItemsByCodeList(dictCodeList);
		Map<String, List<DictModel>> dictMap = new HashMap<>();
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			dict.setDictCode(null);
			dictItemList.add(new DictModel(dict.getValue(), dict.getText()));
		}
		return dictMap;
	}

	@Override
	public Map<String, List<DictModel>> queryAllDictItems() {
		Map<String, List<DictModel>> res = new HashMap<String, List<DictModel>>();
		List<SysDict> ls = sysDictMapper.selectList(null);
		LambdaQueryWrapper<SysDictItem> queryWrapper = new LambdaQueryWrapper<SysDictItem>();
		queryWrapper.eq(SysDictItem::getStatus, 1);
		queryWrapper.orderByAsc(SysDictItem::getSortOrder);
		List<SysDictItem> sysDictItemList = sysDictItemMapper.selectList(queryWrapper);

		for (SysDict d : ls) {
			List<DictModel> dictModelList = sysDictItemList.stream().filter(s -> d.getId().equals(s.getDictId())).map(item -> {
				DictModel dictModel = new DictModel();
				dictModel.setText(item.getItemText());
				dictModel.setValue(item.getItemValue());
				return dictModel;
			}).collect(Collectors.toList());
			res.put(d.getDictCode(), dictModelList);
		}
		return res;
	}

	/**
	 * @param code
	 * @param key
	 * @return
	 */

	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_CACHE,key = "#code+':'+#key", unless = "#result == null ")
	public String queryDictTextByKey(String code, String key) {
		return sysDictMapper.queryDictTextByKey(code, key);
	}

	@Override
	public Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys) {
		List<DictModelMany> list = sysDictMapper.queryManyDictByKeys(dictCodeList, keys);
		Map<String, List<DictModel>> dictMap = new HashMap<>();
		for (DictModelMany dict : list) {
			List<DictModel> dictItemList = dictMap.computeIfAbsent(dict.getDictCode(), i -> new ArrayList<>());
			dictItemList.add(new DictModel(dict.getValue(), dict.getText()));
		}
		return dictMap;
	}

	/**
	 * @param table
	 * @param text
	 * @param code
	 * @return
	 */
	@Override
	//@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE)
	public List<DictModel> queryTableDictItemsByCode(String table, String text, String code) {
		return sysDictMapper.queryTableDictItemsByCode(table,text,code);
	}

	@Override
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql) {
		return sysDictMapper.queryTableDictItemsByCodeAndFilter(table,text,code,filterSql);
	}
	
	/**
	 * @param table
	 * @param text
	 * @param code
	 * @param key
	 * @return
	 */
	@Override
	@Cacheable(value = CacheConstant.SYS_DICT_TABLE_CACHE, unless = "#result == null ")
	public String queryTableDictTextByKey(String table,String text,String code, String key) {
		return sysDictMapper.queryTableDictTextByKey(table,text,code,key);
	}

	@Override
	public List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> keys) {
		return sysDictMapper.queryTableDictTextByKeys(table, text, code, keys);
	}

	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String keys) {
		return this.queryTableDictByKeys(table, text, code, keys, true);
	}

	/**
	 * @param table
	 * @param text
	 * @param code
	 * @return
	 */
	@Override
	public List<String> queryTableDictByKeys(String table, String text, String code, String keys, boolean delNotExist) {
		if(oConvertUtils.isEmpty(keys)){
			return null;
		}
		String[] keyArray = keys.split(",");
		List<DictModel> dicts = sysDictMapper.queryTableDictByKeys(table, text, code, keyArray);
		List<String> texts = new ArrayList<>(dicts.size());

		for (String key : keyArray) {
			List<DictModel> res = dicts.stream().filter(i -> key.equals(i.getValue())).collect(Collectors.toList());
			if (res.size() > 0) {
				texts.add(res.get(0).getText());
			} else if (!delNotExist) {
				texts.add(key);
			}
		}

		return texts;
	}

    /**
     */
    @Override
    public boolean deleteByDictId(SysDict sysDict) {
        sysDict.setDelFlag(CommonConstant.DEL_FLAG_1);
        return  this.updateById(sysDict);
    }

    @Override
    @Transactional
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList) {
		int insert=0;
    	try{
			 insert = sysDictMapper.insert(sysDict);
			if (sysDictItemList != null) {
				for (SysDictItem entity : sysDictItemList) {
					entity.setDictId(sysDict.getId());
					entity.setStatus(1);
					sysDictItemMapper.insert(entity);
				}
			}
		}catch(Exception e){
			return insert;
		}
		return insert;
    }

	@Override
	public List<DictModel> queryAllDepartBackDictModel() {
		return baseMapper.queryAllDepartBackDictModel();
	}

	@Override
	public List<DictModel> queryAllUserBackDictModel() {
		return baseMapper.queryAllUserBackDictModel();
	}
	
	@Override
	public List<DictModel> queryTableDictItems(String table, String text, String code, String keyword) {
		return baseMapper.queryTableDictItems(table, text, code, "%"+keyword+"%");
	}

	@Override
	public List<DictModel> queryLittleTableDictItems(String table, String text, String code, String condition, String keyword, int pageSize) {
    	Page<DictModel> page = new Page<DictModel>(1, pageSize);
		page.setSearchCount(false);
		String filterSql = getFilterSql(text, code, condition, keyword);
		IPage<DictModel> pageList = baseMapper.queryTableDictWithFilter(page, table, text, code, filterSql);
		return pageList.getRecords();
	}

	/**
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	private String getFilterSql(String text, String code, String condition, String keyword){
		String keywordSql = null, filterSql = "", sql_where = " where ";
		if(oConvertUtils.isNotEmpty(keyword)){
			if (keyword.contains(",")) {
				String inKeywords = "\"" + keyword.replaceAll(",", "\",\"") + "\"";
				keywordSql = "(" + text + " in (" + inKeywords + ") or " + code + " in (" + inKeywords + "))";
			} else {
				keywordSql = "("+text + " like '%"+keyword+"%' or "+ code + " like '%"+keyword+"%')";
			}
		}
		if(oConvertUtils.isNotEmpty(condition) && oConvertUtils.isNotEmpty(keywordSql)){
			filterSql+= sql_where + condition + " and " + keywordSql;
		}else if(oConvertUtils.isNotEmpty(condition)){
			filterSql+= sql_where + condition;
		}else if(oConvertUtils.isNotEmpty(keywordSql)){
			filterSql+= sql_where + keywordSql;
		}
		return filterSql;
	}
	@Override
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword) {
		String filterSql = getFilterSql(text, code, condition, keyword);
		List<DictModel> ls = baseMapper.queryAllTableDictItems(table, text, code, filterSql);
    	return ls;
	}

	@Override
	public List<TreeSelectModel> queryTreeList(Map<String, String> query,String table, String text, String code, String pidField,String pid,String hasChildField) {
		return baseMapper.queryTreeList(query,table, text, code, pidField, pid,hasChildField);
	}

	@Override
	public void deleteOneDictPhysically(String id) {
		this.baseMapper.deleteOneById(id);
		this.sysDictItemMapper.delete(new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId,id));
	}

	@Override
	public void updateDictDelFlag(int delFlag, String id) {
		baseMapper.updateDictDelFlag(delFlag,id);
	}

	@Override
	public List<SysDict> queryDeleteList() {
		return baseMapper.queryDeleteList();
	}

	@Override
	public List<DictModel> queryDictTablePageList(DictQuery query, int pageSize, int pageNo) {
		Page page = new Page(pageNo,pageSize,false);
		Page<DictModel> pageList = baseMapper.queryDictTablePageList(page, query);
		return pageList.getRecords();
	}

	@Override
	public List<DictModel> getDictItems(String dictCode) {
		List<DictModel> ls;
		if (dictCode.contains(",")) {
			String[] params = dictCode.split(",");
			if (params.length < 3) {
				return null;
			}
			final String[] sqlInjCheck = {params[0], params[1], params[2]};
			SqlInjectionUtil.filterContent(sqlInjCheck);
			if (params.length == 4) {
				SqlInjectionUtil.specialFilterContent(params[3]);
				ls = this.queryTableDictItemsByCodeAndFilter(params[0], params[1], params[2], params[3]);
			} else if (params.length == 3) {
				ls = this.queryTableDictItemsByCode(params[0], params[1], params[2]);
			} else {
				return null;
			}
		} else {
			ls = this.queryDictItemsByCode(dictCode);
		}
		return ls;
	}

	@Override
	public List<DictModel> loadDict(String dictCode, String keyword, Integer pageSize) {
		if (dictCode.contains(",")) {
			String[] params = dictCode.split(",");
			String condition = null;
			if (params.length != 3 && params.length != 4) {
				return null;
			} else if (params.length == 4) {
				condition = params[3];
			}
			List<DictModel> ls;
			if (pageSize != null) {
				ls = this.queryLittleTableDictItems(params[0], params[1], params[2], condition, keyword, pageSize);
			} else {
				ls = this.queryAllTableDictItems(params[0], params[1], params[2], condition, keyword);
			}
			return ls;
		} else {
			return null;
		}
	}

}
