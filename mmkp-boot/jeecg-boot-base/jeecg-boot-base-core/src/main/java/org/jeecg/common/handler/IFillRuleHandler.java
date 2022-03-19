package org.jeecg.common.handler;

import com.alibaba.fastjson.JSONObject;

/**
 *       
 *
 * @author Yan_ 
 *           ，            
 */
public interface IFillRuleHandler {

    /**
     * @param params         
     * @param formData        
     * @return
     */
    public Object execute(JSONObject params, JSONObject formData);

}

