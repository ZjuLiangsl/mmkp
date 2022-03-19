package org.platform.modules.system.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import org.jeecg.common.aspect.annotation.Dict;
import org.jeecgframework.poi.excel.annotation.Excel;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * <p>
 * <p>
 * 
 * @Author Steve
 * @Since  2019-01-22
 */
@Data
@TableName("sys_depart")
public class SysDepart implements Serializable {
    private static final long serialVersionUID = 1L;
    
	/**ID*/
	@TableId(type = IdType.ASSIGN_ID)
	private String id;
	/**parentId*/
	private String parentId;
	/**departName*/
	@Excel(name="departName",width=15)
	private String departName;
	/**departNameEn*/
	@Excel(name="departNameEn",width=15)
	private String departNameEn;
	/**departNameAbbr*/
	private String departNameAbbr;
	/**departOrder*/
	@Excel(name="departOrder",width=15)
	private Integer departOrder;
	/**description*/
	@Excel(name="description",width=15)
	private String description;
	/**orgCategory*/
	@Excel(name="orgCategory",width=15,dicCode="org_category")
	private String orgCategory;
	/**orgType*/
	private String orgType;
	/**orgCode*/
	@Excel(name="orgCode",width=15)
	private String orgCode;
	/**mobile*/
	@Excel(name="mobile",width=15)
	private String mobile;
	/**fax*/
	@Excel(name="fax",width=15)
	private String fax;
	/**address*/
	@Excel(name="address",width=15)
	private String address;
	/**memo*/
	@Excel(name="memo",width=15)
	private String memo;
	/**depart_status*/
	@Dict(dicCode = "depart_status")
	private String status;
	/**del_flag*/
	@Dict(dicCode = "del_flag")
	private String delFlag;
	/**qywxIdentifier*/
	private String qywxIdentifier;
	/**createBy*/
	private String createBy;
	/**createTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date createTime;
	/**updateBy*/
	private String updateBy;
	/**updateTime*/
	@JsonFormat(timezone = "GMT+8",pattern = "yyyy-MM-dd HH:mm:ss")
    @DateTimeFormat(pattern="yyyy-MM-dd HH:mm:ss")
	private Date updateTime;
	
	/**
	 */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
			return true;
		}
        if (o == null || getClass() != o.getClass()) {
			return false;
		}
        if (!super.equals(o)) {
			return false;
		}
        SysDepart depart = (SysDepart) o;
        return Objects.equals(id, depart.id) &&
                Objects.equals(parentId, depart.parentId) &&
                Objects.equals(departName, depart.departName) &&
                Objects.equals(departNameEn, depart.departNameEn) &&
                Objects.equals(departNameAbbr, depart.departNameAbbr) &&
                Objects.equals(departOrder, depart.departOrder) &&
                Objects.equals(description, depart.description) &&
                Objects.equals(orgCategory, depart.orgCategory) &&
                Objects.equals(orgType, depart.orgType) &&
                Objects.equals(orgCode, depart.orgCode) &&
                Objects.equals(mobile, depart.mobile) &&
                Objects.equals(fax, depart.fax) &&
                Objects.equals(address, depart.address) &&
                Objects.equals(memo, depart.memo) &&
                Objects.equals(status, depart.status) &&
                Objects.equals(delFlag, depart.delFlag) &&
                Objects.equals(createBy, depart.createBy) &&
                Objects.equals(createTime, depart.createTime) &&
                Objects.equals(updateBy, depart.updateBy) &&
                Objects.equals(updateTime, depart.updateTime);
    }

    /**
     */
    @Override
    public int hashCode() {

        return Objects.hash(super.hashCode(), id, parentId, departName, 
        		departNameEn, departNameAbbr, departOrder, description,orgCategory, 
        		orgType, orgCode, mobile, fax, address, memo, status, 
        		delFlag, createBy, createTime, updateBy, updateTime);
    }
}
