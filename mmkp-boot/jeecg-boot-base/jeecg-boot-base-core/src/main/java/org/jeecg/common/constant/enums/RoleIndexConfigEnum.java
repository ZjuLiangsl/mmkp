package org.jeecg.common.constant.enums;

/**
 *
 *
 */
public enum RoleIndexConfigEnum {
    /**
     *
     */
    ADMIN("admin1", "dashboard/Analysis2"),
    /**
     *
     */
    TEST("test",  "dashboard/Analysis"),
    /**
     * hr
     */
    HR("hr", "dashboard/Analysis1");

    /**
     *
     */
    String roleCode;
    /**
     *   index
     */
    String componentUrl;

    /**
     *
     *
     * @param roleCode
     * @param componentUrl       （         ）
     */
    RoleIndexConfigEnum(String roleCode, String componentUrl) {
        this.roleCode = roleCode;
        this.componentUrl = componentUrl;
    }
    /**
     *   code   
     * @param roleCode     
     * @return
     */
    public static RoleIndexConfigEnum getEnumByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e;
            }
        }
        return null;
    }
    /**
     *   code index
     * @param roleCode     
     * @return
     */
    public static String getIndexByCode(String roleCode) {
        for (RoleIndexConfigEnum e : RoleIndexConfigEnum.values()) {
            if (e.roleCode.equals(roleCode)) {
                return e.componentUrl;
            }
        }
        return null;
    }

    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    public String getComponentUrl() {
        return componentUrl;
    }

    public void setComponentUrl(String componentUrl) {
        this.componentUrl = componentUrl;
    }
}
