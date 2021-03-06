package org.platform.modules.system.model;

import lombok.Data;
import org.platform.modules.system.entity.SysDepart;
import org.platform.modules.system.entity.SysUser;

/**
 *
 * @author sunjianlei
 */
@Data
public class SysUserSysDepartModel {

    private String id;
    private String realname;
    private String workNo;
    private String post;
    private String telephone;
    private String email;
    private String phone;
    private String departId;
    private String departName;
    private String avatar;

}
