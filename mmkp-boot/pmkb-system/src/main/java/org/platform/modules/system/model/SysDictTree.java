package org.platform.modules.system.model;

import java.io.Serializable;
import java.util.Date;

import org.platform.modules.system.entity.SysDict;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.Accessors;

/**
 * <p>
 * </p>
 *
 * @Author zhangweijian
 * @since 2018-12-28
 */
@Data
@EqualsAndHashCode(callSuper = false)
@Accessors(chain = true)
public class SysDictTree implements Serializable {

    private static final long serialVersionUID = 1L;

    private String key;
	
	private String title;
	
    /**
     * id
     */
    @TableId(type = IdType.ASSIGN_ID)
    private String id;
    /**
     */
    private Integer type;
    
    /**
     * 字典名称
     */
    private String dictName;

    /**
     * 字典编码
     */
    private String dictCode;

    /**
     * 描述
     */
    private String description;

    /**
     * 删除状态
     */
    private Integer delFlag;

    /**
     * 创建人
     */
    private String createBy;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 更新人
     */
    private String updateBy;

    /**
     * 更新时间
     */
    private Date updateTime;
    
    public SysDictTree(SysDict node) {
    	this.id = node.getId();
		this.key = node.getId();
		this.title = node.getDictName();
		this.dictCode = node.getDictCode();
		this.description = node.getDescription();
		this.delFlag = node.getDelFlag();
		this.type = node.getType();
	}
    
}
