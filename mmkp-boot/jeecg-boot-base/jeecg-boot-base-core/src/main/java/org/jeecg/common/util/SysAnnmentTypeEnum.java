package org.jeecg.common.util;

/**
 *
 */
public enum SysAnnmentTypeEnum {
    /**
     *
     */
    EMAIL("email", "component", "modules/eoa/email/modals/EoaEmailInForm"),
    /**
     *
     */
    BPM("bpm", "url", "/bpm/task/MyTaskList");

    /**
     *     (email:   bpm:  )
     */
    private String type;
    /**
     *        ：component   ：url
     */
    private String openType;
    /**
     *   /
     */
    private String openPage;

    SysAnnmentTypeEnum(String type, String openType, String openPage) {
        this.type = type;
        this.openType = openType;
        this.openPage = openPage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getOpenType() {
        return openType;
    }

    public void setOpenType(String openType) {
        this.openType = openType;
    }

    public String getOpenPage() {
        return openPage;
    }

    public void setOpenPage(String openPage) {
        this.openPage = openPage;
    }

    public static SysAnnmentTypeEnum getByType(String type) {
        if (oConvertUtils.isEmpty(type)) {
            return null;
        }
        for (SysAnnmentTypeEnum val : values()) {
            if (val.getType().equals(type)) {
                return val;
            }
        }
        return null;
    }
}
