package org.platform.modules.system.service;

import org.platform.modules.system.entity.SysDepartRoleUser;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * @Author: jeecg-boot
 * @Date:   2020-02-13
 * @Version: V1.0
 */
public interface ISysDepartRoleUserService extends IService<SysDepartRoleUser> {

    void deptRoleUserAdd(String userId,String newRoleId,String oldRoleId);

    /**
     * @param userIds
     * @param depId
     */
    void removeDeptRoleUser(List<String> userIds,String depId);
}
