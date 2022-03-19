package org.jeecg.common.api.dto;
import lombok.Data;
import org.jeecg.common.system.vo.LoginUser;
import java.io.Serializable;
import java.util.Date;


@Data
public class LogDTO implements Serializable {

    private static final long serialVersionUID = 8482720462943906924L;

    /**  */
    private String logContent;

    /**    (0:    ;1:    ;2:    )  */
    private Integer logType;

    /**    (1:  ;2:  ;3:  ;) */
    private Integer operateType;

    /**     */
    private LoginUser loginUser;

    private String id;
    private String createBy;
    private Date createTime;
    private Long costTime;
    private String ip;

    /**     */
    private String requestParam;

    /**    */
    private String requestType;

    /**    */
    private String requestUrl;

    /**     */
    private String method;

    /**       */
    private String username;

    /**       */
    private String userid;

    public LogDTO(){

    }

    public LogDTO(String logContent, Integer logType, Integer operatetype){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
    }

    public LogDTO(String logContent, Integer logType, Integer operatetype, LoginUser loginUser){
        this.logContent = logContent;
        this.logType = logType;
        this.operateType = operatetype;
        this.loginUser = loginUser;
    }
}
