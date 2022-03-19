package org.platform.modules.system.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Select;
import org.platform.modules.system.entity.SysDepart;
import org.platform.modules.system.model.SysDepartTreeModel;
import org.platform.modules.system.model.TreeModel;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * <p>
 * <p>
 * 
 * @Author: Steve
 * @Since：   2019-01-22
 */
public interface SysDepartMapper extends BaseMapper<SysDepart> {
	
	/**
	 */
	public List<SysDepart> queryUserDeparts(@Param("userId") String userId);

	/**
	 *
	 * @param username
	 * @return
	 */
	public List<SysDepart> queryDepartsByUsername(@Param("username") String username);

	@Select("select id from sys_depart where org_code=#{orgCode}")
	public String queryDepartIdByOrgCode(@Param("orgCode") String orgCode);

	@Select("select id,parent_id from sys_depart where id=#{departId}")
	public SysDepart getParentDepartId(@Param("departId") String departId);

	/**
	 * @param departId
	 * @return
	 */
	List<String> getSubDepIdsByDepId(@Param("departId") String departId);

	/**
	 * @param orgCodes
	 * @return
	 */
	List<String> getSubDepIdsByOrgCodes(@org.apache.ibatis.annotations.Param("orgCodes") String[] orgCodes);

    List<SysDepart> queryTreeListByPid(@Param("parentId") String parentId);
	/**
	 * @param parentId
	 * @return
	 */
	@Select("SELECT count(*) FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
    Integer queryCountByPid(@Param("parentId")String parentId);
	/**
	 * @param orgCode
	 * @return
	 */
	SysDepart queryCompByOrgCode(@Param("orgCode")String orgCode);
	/**
	 * @param parentId
	 * @return
	 */
	@Select("SELECT * FROM sys_depart where del_flag ='0' AND parent_id = #{parentId,jdbcType=VARCHAR}")
	List<SysDepart> queryDeptByPid(@Param("parentId")String parentId);
}
