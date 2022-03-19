package org.platform.modules.system.service;

import com.baomidou.mybatisplus.extension.service.IService;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictQuery;
import org.platform.modules.system.entity.SysDict;
import org.platform.modules.system.entity.SysDictItem;
import org.platform.modules.system.model.TreeSelectModel;

import java.util.List;
import java.util.Map;

/**
 * <p>
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
public interface ISysDictService extends IService<SysDict> {

    public List<DictModel> queryDictItemsByCode(String code);

	/**
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(String code);

	/**
	 *
	 * @param dictCodeList
	 */
	Map<String, List<DictModel>> queryDictItemsByCodeList(List<String> dictCodeList);

    public Map<String,List<DictModel>> queryAllDictItems();

    @Deprecated
    List<DictModel> queryTableDictItemsByCode(String table, String text, String code);

    @Deprecated
	public List<DictModel> queryTableDictItemsByCodeAndFilter(String table, String text, String code, String filterSql);

    public String queryDictTextByKey(String code, String key);

	/**
	 * @return
	 */
	Map<String, List<DictModel>> queryManyDictByKeys(List<String> dictCodeList, List<String> keys);

    @Deprecated
	String queryTableDictTextByKey(String table, String text, String code, String key);

	/**
	 *
	 * @param table
	 * @param text
	 * @param code
	 * @param keys
	 * @return
	 */
	List<DictModel> queryTableDictTextByKeys(String table, String text, String code, List<String> keys);

	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys);
	@Deprecated
	List<String> queryTableDictByKeys(String table, String text, String code, String keys,boolean delNotExist);

    /**
     *
     * @param sysDict
     * @return
     */
    boolean deleteByDictId(SysDict sysDict);

    /**
     */
    public Integer saveMain(SysDict sysDict, List<SysDictItem> sysDictItemList);

    /**
	 * @return
	 */
	public List<DictModel> queryAllDepartBackDictModel();

	/**
	 * @return
	 */
	public List<DictModel> queryAllUserBackDictModel();

	/**
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryTableDictItems(String table, String text, String code,String keyword);

	/**
	 * @param table
	 * @param text
	 * @param code
	 * @param keyword
	 * @return
	 */
	public List<DictModel> queryLittleTableDictItems(String table, String text, String code, String condition, String keyword, int pageSize);

	/**
	 * @param table
	 * @param text
	 * @param code
	 * @param condition
	 * @param keyword
	 * @return
	 */
	public List<DictModel> queryAllTableDictItems(String table, String text, String code, String condition, String keyword);
	/**
	 * @param table
	 * @param text
	 * @param code
	 * @param pidField
	 * @param pid
	 * @param hasChildField
	 * @return
	 */
	@Deprecated
	List<TreeSelectModel> queryTreeList(Map<String, String> query,String table, String text, String code, String pidField,String pid,String hasChildField);

	/**
	 * @param id
	 */
	public void deleteOneDictPhysically(String id);

	/**
	 * @param delFlag
	 * @param id
	 */
	public void updateDictDelFlag(int delFlag,String id);

	/**
	 * @return
	 */
	public List<SysDict> queryDeleteList();

	/**
	 * @param query
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	@Deprecated
	public List<DictModel> queryDictTablePageList(DictQuery query,int pageSize, int pageNo);

    /**
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     *
     * @return
     */
    List<DictModel> loadDict(String dictCode, String keyword, Integer pageSize);

}
