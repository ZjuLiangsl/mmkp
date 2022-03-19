package org.jeecg.modules.base.service;

import org.jeecg.common.api.dto.LogDTO;
import org.jeecg.common.system.vo.LoginUser;

/**
 * common  
 */
public interface BaseCommonService {

    /**
     *     
     * @param logDTO
     */
    void addLog(LogDTO logDTO);

    /**
     *     
     * @param LogContent
     * @param logType
     * @param operateType
     * @param user
     */
    void addLog(String LogContent, Integer logType, Integer operateType, LoginUser user);

    /**
     *     
     * @param LogContent
     * @param logType
     * @param operateType
     */
    void addLog(String LogContent, Integer logType, Integer operateType);

}
