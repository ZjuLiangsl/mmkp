package org.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.jeecg.common.system.vo.DictModel;
import org.jeecg.common.system.vo.DictModelMany;
import org.jeecg.common.system.vo.DictQuery;
import org.platform.modules.system.entity.SysDict;
import org.platform.modules.system.model.DuplicateCheckVo;
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
public interface SysDictMapper extends BaseMapper<SysDict> {
	
	/**
	 * @return
	 */
	@Deprecated
	public Long duplicateCheckCountSql(DuplicateCheckVo duplicateCheckVo);
	@Deprecated
	public Long duplicateCheckCountSqlNoDataId(DuplicateCheckVo duplicateCheckVo);
	
	public List<DictModel> queryDictItemsByCode(@Param("code") String code);

	/**
	 * @param code
	 * @return
	 */
	List<DictModel> queryEnableDictItemsByCode(@Param("code") String code);


	/**
	 *
	 * @param dictCodeList
	 * @return
	 */
	public List<DictModelMany> queryDictItemsByCodeList(@Param("dictCodeList") List<String> dictCodeList);

	@Deprecated
	public List<DictModel> queryTableDictItemsByCode(@Param("table") String table,@Param("text") String text,@Param("code") String code);

	@Deprecated
	public List<DictModel> queryTableDictItemsByCodeAndFilter(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("filterSql") String filterSql);

	@Deprecated
	@Select("select ${key} as \"label\",${value} as \"value\" from ${table}")
	public List<Map<String,String>> getDictByTableNgAlain(@Param("table") String table, @Param("key") String key, @Param("value") String value);

	public String queryDictTextByKey(@Param("code") String code,@Param("key") String key);

	List<DictModelMany> queryManyDictByKeys(@Param("dictCodeList") List<String> dictCodeList, @Param("keys") List<String> keys);

	@Deprecated
	public String queryTableDictTextByKey(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("key") String key);

	@Deprecated
	List<DictModel> queryTableDictTextByKeys(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keys") List<String> keys);

	@Deprecated
	public List<DictModel> queryTableDictByKeys(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keyArray") String[] keyArray);


	public List<DictModel> queryAllDepartBackDictModel();
	

	public List<DictModel> queryAllUserBackDictModel();
	

	@Deprecated
	public List<DictModel> queryTableDictItems(@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("keyword") String keyword);



	IPage<DictModel> queryTableDictItems(Page<DictModel> page, @Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("keyword") String keyword);


	@Deprecated
	List<TreeSelectModel> queryTreeList(@Param("query") Map<String, String> query,@Param("table") String table,@Param("text") String text,@Param("code") String code,@Param("pidField") String pidField,@Param("pid") String pid,@Param("hasChildField") String hasChildField);


	@Select("delete from sys_dict where id = #{id}")
	public void deleteOneById(@Param("id") String id);


	@Select("select * from sys_dict where del_flag = 1")
	public List<SysDict> queryDeleteList();

	@Update("update sys_dict set del_flag = #{flag,jdbcType=INTEGER} where id = #{id,jdbcType=VARCHAR}")
	public void updateDictDelFlag(@Param("flag") int delFlag, @Param("id") String id);


	@Deprecated
	public Page<DictModel> queryDictTablePageList(Page page, @Param("query") DictQuery query);


	@Deprecated
	IPage<DictModel> queryTableDictWithFilter(Page<DictModel> page, @Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);

	@Deprecated
	List<DictModel> queryAllTableDictItems(@Param("table") String table, @Param("text") String text, @Param("code") String code, @Param("filterSql") String filterSql);
}
