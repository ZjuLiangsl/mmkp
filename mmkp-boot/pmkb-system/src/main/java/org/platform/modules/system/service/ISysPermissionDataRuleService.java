package org.platform.modules.system.service;

import java.util.List;

import org.platform.modules.system.entity.SysPermissionDataRule;

import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * </p>
 *
 * @Author huangzhilin
 * @since 2019-04-01
 */
public interface ISysPermissionDataRuleService extends IService<SysPermissionDataRule> {

	/**
	 *
	 */
	List<SysPermissionDataRule> getPermRuleListByPermId(String permissionId);

	/**
	 *
	 * @return
	 */
	List<SysPermissionDataRule> queryPermissionRule(SysPermissionDataRule permRule);
	
	
	/**
	 * @param username
	 * @return
	 */
	List<SysPermissionDataRule> queryPermissionDataRules(String username,String permissionId);
	
	/**
	 * @param sysPermissionDataRule
	 */
	public void savePermissionDataRule(SysPermissionDataRule sysPermissionDataRule);
	
	/**
	 * @param dataRuleId
	 */
	public void deletePermissionDataRule(String dataRuleId);
	
	
}
