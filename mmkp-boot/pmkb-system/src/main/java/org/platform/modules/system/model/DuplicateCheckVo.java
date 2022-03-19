package org.platform.modules.system.model;

import java.io.Serializable;
import java.util.List;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @Title: DuplicateCheckVo
 * @Date 2019-03-25
 * @Version V1.0
 */
@Data
@ApiModel(value="DuplicateCheckVo",description="DuplicateCheckVo")
public class DuplicateCheckVo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 */
	@ApiModelProperty(value="tableName",name="tableName",example="sys_log")
	private String tableName;
	
	/**
	 */
	@ApiModelProperty(value="fieldName",name="fieldName",example="id")
	private String fieldName;
	
	/**
	 * fieldVal
	 */
	@ApiModelProperty(value="fieldVal",name="fieldVal",example="1000")
	private String fieldVal;
	
	/**
	 * dataId
	*/
	@ApiModelProperty(value="dataId",name="dataId",example="2000")
	private String dataId;

	private List<JSONObject> multipleConditions;
}