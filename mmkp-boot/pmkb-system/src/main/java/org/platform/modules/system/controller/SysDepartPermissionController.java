package org.platform.modules.system.controller;

import java.util.*;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.jeecg.common.api.vo.Result;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.system.query.QueryGenerator;
import org.jeecg.common.aspect.annotation.AutoLog;
import org.jeecg.common.util.oConvertUtils;
import org.platform.modules.system.entity.SysDepartPermission;
import org.platform.modules.system.entity.SysDepartRolePermission;
import org.platform.modules.system.entity.SysPermission;
import org.platform.modules.system.entity.SysPermissionDataRule;
import org.platform.modules.system.model.TreeModel;
import org.platform.modules.system.service.ISysDepartPermissionService;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.system.base.controller.JeecgController;
import org.platform.modules.system.service.ISysDepartRolePermissionService;
import org.platform.modules.system.service.ISysPermissionDataRuleService;
import org.platform.modules.system.service.ISysPermissionService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

 /**
 * @Author: jeecg-boot
 * @Date:   2020-02-11
 * @Version: V1.0
 */
@Slf4j
@RestController
@RequestMapping("/sys/sysDepartPermission")
public class SysDepartPermissionController extends JeecgController<SysDepartPermission, ISysDepartPermissionService> {
	@Autowired
	private ISysDepartPermissionService sysDepartPermissionService;

	 @Autowired
	 private ISysPermissionDataRuleService sysPermissionDataRuleService;

	 @Autowired
	 private ISysPermissionService sysPermissionService;

	 @Autowired
	 private ISysDepartRolePermissionService sysDepartRolePermissionService;

	/**
	 *
	 * @param sysDepartPermission
	 * @param pageNo
	 * @param pageSize
	 * @param req
	 * @return
	 */
	@GetMapping(value = "/list")
	public Result<?> queryPageList(SysDepartPermission sysDepartPermission,
								   @RequestParam(name="pageNo", defaultValue="1") Integer pageNo,
								   @RequestParam(name="pageSize", defaultValue="10") Integer pageSize,
								   HttpServletRequest req) {
		QueryWrapper<SysDepartPermission> queryWrapper = QueryGenerator.initQueryWrapper(sysDepartPermission, req.getParameterMap());
		Page<SysDepartPermission> page = new Page<SysDepartPermission>(pageNo, pageSize);
		IPage<SysDepartPermission> pageList = sysDepartPermissionService.page(page, queryWrapper);
		return Result.ok(pageList);
	}
	
	/**
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@PostMapping(value = "/add")
	public Result<?> add(@RequestBody SysDepartPermission sysDepartPermission) {
		sysDepartPermissionService.save(sysDepartPermission);
		return Result.ok("success！");
	}
	
	/**
	 *
	 * @param sysDepartPermission
	 * @return
	 */
	@PutMapping(value = "/edit")
	public Result<?> edit(@RequestBody SysDepartPermission sysDepartPermission) {
		sysDepartPermissionService.updateById(sysDepartPermission);
		return Result.ok("success!");
	}
	
	/**
	 *
	 * @param id
	 * @return
	 */
	@DeleteMapping(value = "/delete")
	public Result<?> delete(@RequestParam(name="id",required=true) String id) {
		sysDepartPermissionService.removeById(id);
		return Result.ok("success!");
	}
	
	/**
	 *
	 * @param ids
	 * @return
	 */
	@DeleteMapping(value = "/deleteBatch")
	public Result<?> deleteBatch(@RequestParam(name="ids",required=true) String ids) {
		this.sysDepartPermissionService.removeByIds(Arrays.asList(ids.split(",")));
		return Result.ok("success！");
	}
	
	/**
	 *
	 * @param id
	 * @return
	 */
	@GetMapping(value = "/queryById")
	public Result<?> queryById(@RequestParam(name="id",required=true) String id) {
		SysDepartPermission sysDepartPermission = sysDepartPermissionService.getById(id);
		return Result.ok(sysDepartPermission);
	}

	/**
	*
	* @param request
	* @param sysDepartPermission
	*/
	@RequestMapping(value = "/exportXls")
	public ModelAndView exportXls(HttpServletRequest request, SysDepartPermission sysDepartPermission) {
	  return super.exportXls(request, sysDepartPermission, SysDepartPermission.class, "Department Permission table");
	}

	/**
	*
	* @param request
	* @param response
	* @return
	*/
	@RequestMapping(value = "/importExcel", method = RequestMethod.POST)
	public Result<?> importExcel(HttpServletRequest request, HttpServletResponse response) {
	  return super.importExcel(request, response, SysDepartPermission.class);
	}

	/**
	*/
	@GetMapping(value = "/datarule/{permissionId}/{departId}")
	public Result<?> loadDatarule(@PathVariable("permissionId") String permissionId,@PathVariable("departId") String departId) {
		List<SysPermissionDataRule> list = sysPermissionDataRuleService.getPermRuleListByPermId(permissionId);
		if(list==null || list.size()==0) {
			return Result.error("Permission configuration information was not found");
		}else {
			Map<String,Object> map = new HashMap<>();
			map.put("datarule", list);
			LambdaQueryWrapper<SysDepartPermission> query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, permissionId)
				 .eq(SysDepartPermission::getDepartId,departId);
			SysDepartPermission sysDepartPermission = sysDepartPermissionService.getOne(query);
			if(sysDepartPermission==null) {
			}else {
				String drChecked = sysDepartPermission.getDataRuleIds();
				if(oConvertUtils.isNotEmpty(drChecked)) {
					map.put("drChecked", drChecked.endsWith(",")?drChecked.substring(0, drChecked.length()-1):drChecked);
				}
			}
			return Result.ok(map);
		}
	}

	/**
	*/
	@PostMapping(value = "/datarule")
	public Result<?> saveDatarule(@RequestBody JSONObject jsonObject) {
		try {
			String permissionId = jsonObject.getString("permissionId");
			String departId = jsonObject.getString("departId");
			String dataRuleIds = jsonObject.getString("dataRuleIds");
			LambdaQueryWrapper<SysDepartPermission> query = new LambdaQueryWrapper<SysDepartPermission>()
				 .eq(SysDepartPermission::getPermissionId, permissionId)
				 .eq(SysDepartPermission::getDepartId,departId);
			SysDepartPermission sysDepartPermission = sysDepartPermissionService.getOne(query);
			if(sysDepartPermission==null) {
				return Result.error("Please save the department menu permission first!");
			}else {
				sysDepartPermission.setDataRuleIds(dataRuleIds);
			 	this.sysDepartPermissionService.updateById(sysDepartPermission);
			}
		} catch (Exception e) {
		 	return Result.error("failure");
		}
		return Result.ok("success!");
	}

	 /**
	  *
	  * @return
	  */
	 @RequestMapping(value = "/queryDeptRolePermission", method = RequestMethod.GET)
	 public Result<List<String>> queryDeptRolePermission(@RequestParam(name = "roleId", required = true) String roleId) {
		 Result<List<String>> result = new Result<>();
		 try {
			 List<SysDepartRolePermission> list = sysDepartRolePermissionService.list(new QueryWrapper<SysDepartRolePermission>().lambda().eq(SysDepartRolePermission::getRoleId, roleId));
			 result.setResult(list.stream().map(SysDepartRolePermission -> String.valueOf(SysDepartRolePermission.getPermissionId())).collect(Collectors.toList()));
			 result.setSuccess(true);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 /**
	  *
	  * @return
	  */
	 @RequestMapping(value = "/saveDeptRolePermission", method = RequestMethod.POST)
	 public Result<String> saveDeptRolePermission(@RequestBody JSONObject json) {
		 long start = System.currentTimeMillis();
		 Result<String> result = new Result<>();
		 try {
			 String roleId = json.getString("roleId");
			 String permissionIds = json.getString("permissionIds");
			 String lastPermissionIds = json.getString("lastpermissionIds");
			 this.sysDepartRolePermissionService.saveDeptRolePermission(roleId, permissionIds, lastPermissionIds);
			 result.success("success！");
		 } catch (Exception e) {
			 result.error500("failure！");
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 /**
	  * @param request
	  * @return
	  */
	 @RequestMapping(value = "/queryTreeListForDeptRole", method = RequestMethod.GET)
	 public Result<Map<String,Object>> queryTreeListForDeptRole(@RequestParam(name="departId",required=true) String departId,HttpServletRequest request) {
		 Result<Map<String,Object>> result = new Result<>();
		 List<String> ids = new ArrayList<>();
		 try {
			 LambdaQueryWrapper<SysPermission> query = new LambdaQueryWrapper<SysPermission>();
			 query.eq(SysPermission::getDelFlag, CommonConstant.DEL_FLAG_0);
			 query.orderByAsc(SysPermission::getSortNo);
			 query.inSql(SysPermission::getId,"select permission_id  from sys_depart_permission where depart_id='"+departId+"'");
			 List<SysPermission> list = sysPermissionService.list(query);
			 for(SysPermission sysPer : list) {
				 ids.add(sysPer.getId());
			 }
			 List<TreeModel> treeList = new ArrayList<>();
			 getTreeModelList(treeList, list, null);
			 Map<String,Object> resMap = new HashMap<String,Object>();
			 resMap.put("treeList", treeList);
			 resMap.put("ids", ids);
			 result.setResult(resMap);
			 result.setSuccess(true);
		 } catch (Exception e) {
			 log.error(e.getMessage(), e);
		 }
		 return result;
	 }

	 private void getTreeModelList(List<TreeModel> treeList, List<SysPermission> metaList, TreeModel temp) {
		 for (SysPermission permission : metaList) {
			 String tempPid = permission.getParentId();
			 TreeModel tree = new TreeModel(permission.getId(), tempPid, permission.getName(),permission.getRuleFlag(), permission.isLeaf());
			 if(temp==null && oConvertUtils.isEmpty(tempPid)) {
				 treeList.add(tree);
				 if(!tree.getIsLeaf()) {
					 getTreeModelList(treeList, metaList, tree);
				 }
			 }else if(temp!=null && tempPid!=null && tempPid.equals(temp.getKey())){
				 temp.getChildren().add(tree);
				 if(!tree.getIsLeaf()) {
					 getTreeModelList(treeList, metaList, tree);
				 }
			 }

		 }
	 }

}
