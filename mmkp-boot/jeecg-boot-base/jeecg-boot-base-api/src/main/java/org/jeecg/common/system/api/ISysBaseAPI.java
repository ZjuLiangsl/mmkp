package org.jeecg.common.system.api;

import com.alibaba.fastjson.JSONObject;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.system.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @Author  scott
 * @Date 2019-4-20
 * @Version V1.0
 */
public interface ISysBaseAPI extends CommonAPI {

    /**
     * @param id
     * @return
     */
    LoginUser getUserById(String id);

    /**
     * @param username
     * @return
     */
    List<String> getRolesByUsername(String username);

    /**
     * @param username
     */
    List<String> getDepartIdsByUsername(String username);

    /**
     * @param username
     */
    List<String> getDepartNamesByUsername(String username);



    public List<DictModel> queryAllDict();

    /**
     * @return
     */
    public List<SysCategoryModel> queryAllDSysCategory();


    /**
     * @return
     */
    public List<DictModel> queryAllDepartBackDictModel();


    /**
     * @param table
     * @param text
     * @param code
     * @param filterSql
     * @return
     */
    public List<DictModel> queryFilterTableDictInfo(String table, String text, String code, String filterSql);

    /**
     * @param table
     * @param text
     * @param code
     * @param keyArray
     * @return
     */
    @Deprecated
    public List<String> queryTableDictByKeys(String table, String text, String code, String[] keyArray);

    /**
     * @return
     */
    public List<ComboModel> queryAllUserBackCombo();

    /**
     * @return
     */
    public JSONObject queryAllUser(String userIds, Integer pageNo, Integer pageSize);

    /**
     * @return
     */
    public List<ComboModel> queryAllRole();

    /**
     * @return
     */
    public List<ComboModel> queryAllRole(String[] roleIds );

    /**
     * @param username
     * @return
     */
    public List<String> getRoleIdsByUsername(String username);

    /**
     * @param orgCode
     * @return
     */
    public String getDepartIdsByOrgCode(String orgCode);

    /**
     * @return
     */
    public List<SysDepartModel> getAllSysDepart();

    /**
     * @param departId
     * @return
     */
    DictModel getParentDepartId(String departId);

    /**
     * @param deptId
     * @return
     */
    public List<String> getDeptHeadByDepId(String deptId);


    /**
     * userIds
     * @return
     */
    public List<LoginUser> queryAllUserByIds(String[] userIds);


    /**
     * userNames
     * @return
     */
    List<LoginUser> queryUserByNames(String[] userNames);


    /**
     * @param username
     * @return
     */
    Set<String> getUserRoleSet(String username);

    /**
     * @param username
     * @return
     */
    Set<String> getUserPermissionSet(String username);


    /**
     */
    SysDepartModel selectAllById(String id);

    /**
     * @param userId
     * @return
     */
    List<String> queryDeptUsersByUserId(String userId);

    /**
     * @param usernames
     * @return
     */
    List<JSONObject> queryUsersByUsernames(String usernames);

    /**
     * @param ids
     * @return
     */
    List<JSONObject> queryUsersByIds(String ids);

    /**
     * @param orgCodes
     * @return
     */
    List<JSONObject> queryDepartsByOrgcodes(String orgCodes);

    /**
     * @param ids
     * @return
     */
    List<JSONObject> queryDepartsByIds(String ids);

    /**
     * @param orgCode
     */
    List<Map> getDeptUserByOrgCode(String orgCode);

    /**
     */
    List<String> loadCategoryDictItem(String ids);

    /**
     *
     * @return
     */
    List<String> loadDictItem(String dictCode, String keys);

    /**
     *
     * @return
     */
    List<DictModel> getDictItems(String dictCode);

    /**
     * @param dictCodeList
     */
    Map<String, List<DictModel>> getManyDictItems(List<String> dictCodeList);

    /**
     *
     * @return
     */
    List<DictModel> loadDictItemByKeyword(String dictCode, String keyword, Integer pageSize);

}
