package org.platform.modules.system.service;


import java.util.List;

import com.baomidou.mybatisplus.core.metadata.IPage;
import org.platform.modules.system.entity.SysUser;
import org.platform.modules.system.entity.SysUserDepart;
import org.platform.modules.system.model.DepartIdModel;


import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * </p>
 * @Author ZhiLin
 *
 */
public interface ISysUserDepartService extends IService<SysUserDepart> {
	

	/**
	 * @param userId
	 * @return
	 */
	List<DepartIdModel> queryDepartIdsOfUser(String userId);
	

	/**
	 * @param depId
	 * @return
	 */
	List<SysUser> queryUserByDepId(String depId);
  	/**
	 */
	List<SysUser> queryUserByDepCode(String depCode,String realname);

	/**
	 * @param departId
	 * @param username
	 * @param pageSize
	 * @param pageNo
	 * @return
	 */
	IPage<SysUser> queryDepartUserPageList(String departId, String username, String realname, int pageSize, int pageNo);

}
