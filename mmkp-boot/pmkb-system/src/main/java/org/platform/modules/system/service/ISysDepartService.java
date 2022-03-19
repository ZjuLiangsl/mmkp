package org.platform.modules.system.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.extension.service.IService;
import org.platform.modules.system.entity.SysDepart;
import org.platform.modules.system.model.DepartIdModel;
import org.platform.modules.system.model.SysDepartTreeModel;
import java.util.List;

/**
 * <p>
 * <p>
 * 
 * @Author:Steve
 * @Since：   2019-01-22
 */
public interface ISysDepartService extends IService<SysDepart>{

    /**
     * @return
     */
    List<SysDepartTreeModel> queryMyDeptTreeList(String departIds);

    /**
     * @return
     */
    List<SysDepartTreeModel> queryTreeList();

    /**
     * @return
     */
    public List<DepartIdModel> queryDepartIdTreeList();

    /**
     * @param sysDepart
     */
    void saveDepartData(SysDepart sysDepart,String username);

    /**
     * @param sysDepart
     * @return
     */
    Boolean updateDepartDataById(SysDepart sysDepart,String username);
    
    /**
     * @param id
     * @return
     */
	/* boolean removeDepartDataById(String id); */
    
    /**
     * @param keyWord
     * @return
     */
    List<SysDepartTreeModel> searhBy(String keyWord,String myDeptSearch,String departIds);
    
    /**
     * @param id
     * @return
     */
    boolean delete(String id);
    
    /**
     * @param userId
     * @return
     */
	public List<SysDepart> queryUserDeparts(String userId);

    /**
     *
     * @param username
     * @return
     */
    List<SysDepart> queryDepartsByUsername(String username);

	 /**
     * @param id
     * @return
     */
	void deleteBatchWithChildren(List<String> ids);

    /**
     * @param departId
     * @return
     */
    List<String> getSubDepIdsByDepId(String departId);

    /**
     * @return
     */
    List<String> getMySubDepIdsByDepId(String departIds);
    /**
     * @return
     */
    List<SysDepartTreeModel> queryTreeByKeyWord(String keyWord);
    /**
     * @return
     */
    List<SysDepartTreeModel> queryTreeListByPid(String parentId);

    /**
     *
     */
    JSONObject queryAllParentIdByDepartId(String departId);

    /**
     *
     */
    JSONObject queryAllParentIdByOrgCode(String orgCode);
    /**
     * @return
     */
    SysDepart queryCompByOrgCode(String orgCode);
    /**
     * @return
     */
    List<SysDepart> queryDeptByPid(String pid);
}
