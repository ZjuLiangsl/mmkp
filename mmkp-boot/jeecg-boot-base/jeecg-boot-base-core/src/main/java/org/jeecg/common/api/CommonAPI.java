package org.jeecg.common.api;

import org.jeecg.common.system.vo.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface CommonAPI {

    /**
     * @param username
     * @return
     */
    Set<String> queryUserRoles(String username);


    /**
     * @param username
     * @return
     */
    Set<String> queryUserAuths(String username);

    /**
     *
     * @param dbSourceId
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceById(String dbSourceId);

    /**
     *
     * @param dbSourceCode
     * @return
     */
    DynamicDataSourceModel getDynamicDbSourceByCode(String dbSourceCode);

    /**
     * @param username
     * @return
     */
    public LoginUser getUserByName(String username);


    /**
     * @param table
     * @param text
     * @param code
     * @param key
     * @return
     */
    String translateDictFromTable(String table, String text, String code, String key);

    /**
     * @param code
     * @param key
     * @return
     */
    String translateDict(String code, String key);

    /**
     * @return
     */
    List<SysPermissionDataRuleModel> queryPermissionDataRule(String component, String requestPath, String username);


    /**
     * @param username
     * @return
     */
    SysUserCacheInfo getCacheUser(String username);

    /**
     * @param code
     * @return
     */
    public List<DictModel> queryDictItemsByCode(String code);

    /**
     * @param code
     * @return
     */
    public List<DictModel> queryEnableDictItemsByCode(String code);

    /**
     * @param table
     * @param text
     * @param code
     * @return
     */
    List<DictModel> queryTableDictItemsByCode(String table, String text, String code);

    /**
     * @param dictCodes user_status,sex
     * @param keys  1,2,0
     * @return
     */
    Map<String, List<DictModel>> translateManyDict(String dictCodes, String keys);

    /**
     * 15
     * @param table
     * @param text
     * @param code
     * @param keys
     * @return
     */
    List<DictModel> translateDictFromTableByKeys(String table, String text, String code, String keys);

}
