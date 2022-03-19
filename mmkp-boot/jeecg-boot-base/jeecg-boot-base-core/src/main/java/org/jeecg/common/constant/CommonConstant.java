package org.jeecg.common.constant;

public interface CommonConstant {

	/**
	 *
	 */
	public static final Integer STATUS_NORMAL = 0;

	/**
	 *
	 */
	public static final Integer STATUS_DISABLE = -1;

	/**
	 *
	 */
	public static final Integer DEL_FLAG_1 = 1;

	/**
	 *
	 */
	public static final Integer DEL_FLAG_0 = 0;

	/**
	 *       ：
	 */
	public static final int LOG_TYPE_1 = 1;
	
	/**
	 *       ：
	 */
	public static final int LOG_TYPE_2 = 2;

	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_1 = 1;
	
	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_2 = 2;
	
	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_3 = 3;
	
	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_4 = 4;
	
	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_5 = 5;
	
	/**
	 *       ：
	 */
	public static final int OPERATE_TYPE_6 = 6;
	
	
	/** {@code 500 Server Error} (HTTP/1.0 - RFC 1945) */
    public static final Integer SC_INTERNAL_SERVER_ERROR_500 = 500;
    /** {@code 200 OK} (HTTP/1.0 - RFC 1945) */
    public static final Integer SC_OK_200 = 200;
    
    /**          510*/
    public static final Integer SC_JEECG_NO_AUTHZ=510;

    /**     Shiro    KEY   */
    public static String PREFIX_USER_SHIRO_CACHE  = "shiro:cache:org.jeecg.config.shiro.ShiroRealm.authorizationCache:";
    /**     Token    KEY   */
    public static final String PREFIX_USER_TOKEN  = "prefix_user_token_";
    /** Token    ：3600      */
    public static final int  TOKEN_EXPIRE_TIME  = 3600;
    

    /**
     *  0：
     */
    public static final Integer MENU_TYPE_0  = 0;
   /**
    *  1：
    */
    public static final Integer MENU_TYPE_1  = 1;
    /**
     *  2：
     */
    public static final Integer MENU_TYPE_2  = 2;
    
    /**      （USER:    ，ALL:    ）*/
    public static final String MSG_TYPE_UESR  = "USER";
    public static final String MSG_TYPE_ALL  = "ALL";
    
    /**    （0   ，1   ，2   ）*/
    public static final String NO_SEND  = "0";
    public static final String HAS_SEND  = "1";
    public static final String HAS_CANCLE  = "2";
    
    /**    （0  ，1  ）*/
    public static final String HAS_READ_FLAG  = "1";
    public static final String NO_READ_FLAG  = "0";
    
    /**   （L ，M ，H ）*/
    public static final String PRIORITY_L  = "L";
    public static final String PRIORITY_M  = "M";
    public static final String PRIORITY_H  = "H";
    
    /**
     *         0 .    、1.    、2.
     */
    public static final String SMS_TPL_TYPE_0  = "0";
    public static final String SMS_TPL_TYPE_1  = "1";
    public static final String SMS_TPL_TYPE_2  = "2";
    
    /**
     *   (0  1  )
     */
    public static final String STATUS_0 = "0";
    public static final String STATUS_1 = "1";
    
    /**
     *        1  0
     */
    public static final Integer ACT_SYNC_1 = 1;
    public static final Integer ACT_SYNC_0 = 0;

    /**
     *     1:    2:    
     */
    public static final String MSG_CATEGORY_1 = "1";
    public static final String MSG_CATEGORY_2 = "2";
    
    /**
     *             1 0 
     */
    public static final Integer RULE_FLAG_0 = 0;
    public static final Integer RULE_FLAG_1 = 1;

    /**
     *          1  (  ) 2  
     */
    public static final Integer USER_UNFREEZE = 1;
    public static final Integer USER_FREEZE = 2;
    
    /**        */
    public static final String DICT_TEXT_SUFFIX = "_dictText";

    /**
     *          
     */
    public static final Integer DESIGN_FORM_TYPE_MAIN = 1;

    /**
     *           
     */
    public static final Integer DESIGN_FORM_TYPE_SUB = 2;

    /**
     *      URL    
     */
    public static final Integer DESIGN_FORM_URL_STATUS_PASSED = 1;

    /**
     *      URL     
     */
    public static final Integer DESIGN_FORM_URL_STATUS_NOT_PASSED = 2;

    /**
     *         Flag
     */
    public static final String DESIGN_FORM_URL_TYPE_ADD = "add";
    /**
     *         Flag
     */
    public static final String DESIGN_FORM_URL_TYPE_EDIT = "edit";
    /**
     *         Flag
     */
    public static final String DESIGN_FORM_URL_TYPE_DETAIL = "detail";
    /**
     *           Flag
     */
    public static final String DESIGN_FORM_URL_TYPE_REUSE = "reuse";
    /**
     *         Flag （   ）
     */
    public static final String DESIGN_FORM_URL_TYPE_VIEW = "view";

    /**
     * online     （ ：Y,  ：N）
     */
    public static final String ONLINE_PARAM_VAL_IS_TURE = "Y";
    public static final String ONLINE_PARAM_VAL_IS_FALSE = "N";

    /**
     *       （  ：local，Minio：minio，   ：alioss）
     */
    public static final String UPLOAD_TYPE_LOCAL = "local";
    public static final String UPLOAD_TYPE_MINIO = "minio";
    public static final String UPLOAD_TYPE_OSS = "alioss";

    /**
     *           
     */
    public static final String UPLOAD_CUSTOM_BUCKET = "eoafile";
    /**
     *          
     */
    public static final String UPLOAD_CUSTOM_PATH = "eoafile";
    /**
     *          
     */
    public static final Integer UPLOAD_EFFECTIVE_DAYS = 1;

    /**
     *      （1:      2:  ）
     */
    public static final Integer USER_IDENTITY_1 = 1;
    public static final Integer USER_IDENTITY_2 = 2;

    /** sys_user   username       */
    public static final String SQL_INDEX_UNIQ_SYS_USER_USERNAME = "uniq_sys_user_username";
    /** sys_user   work_no       */
    public static final String SQL_INDEX_UNIQ_SYS_USER_WORK_NO = "uniq_sys_user_work_no";
    /** sys_user   phone       */
    public static final String SQL_INDEX_UNIQ_SYS_USER_PHONE = "uniq_sys_user_phone";
    /** sys_user   email       */
    public static final String SQL_INDEX_UNIQ_SYS_USER_EMAIL = "uniq_sys_user_email";
    /** sys_quartz_job   job_class_name       */
    public static final String SQL_INDEX_UNIQ_JOB_CLASS_NAME = "uniq_job_class_name";
    /** sys_position   code       */
    public static final String SQL_INDEX_UNIQ_CODE = "uniq_code";
    /** sys_role   code       */
    public static final String SQL_INDEX_UNIQ_SYS_ROLE_CODE = "uniq_sys_role_role_code";
    /** sys_depart   code       */
    public static final String SQL_INDEX_UNIQ_DEPART_ORG_CODE = "uniq_depart_org_code";
    /**
     *             
     */
    public static final String IM_DEFAULT_GROUP = "1";
    /**
     *              
     */
    public static final String IM_UPLOAD_CUSTOM_PATH = "imfile";
    /**
     *          
     */
    public static final String IM_STATUS_ONLINE = "online";

    /**
     *      SOCKET    
     */
    public static final String IM_SOCKET_TYPE = "chatMessage";

    /**
     *                 1  0 
     */
    public static final String IM_DEFAULT_ADD_FRIEND = "1";

    /**
     *              
     */
    public static final String IM_PREFIX_USER_FRIEND_CACHE = "sys:cache:im:im_prefix_user_friend_";

    /**
     *          （1：    2：   ）
     */
    public static final String SIGN_PATCH_BIZ_STATUS_1 = "1";
    public static final String SIGN_PATCH_BIZ_STATUS_2 = "2";

    /**
     *            
     */
    public static final String UPLOAD_CUSTOM_PATH_OFFICIAL = "officialdoc";
     /**
     *            
     */
    public static final String DOWNLOAD_CUSTOM_PATH_OFFICIAL = "officaldown";

    /**
     * WPS     (1 code   2 text（WPS          ）)
     */
    public static final String WPS_TYPE_1="1";
    public static final String WPS_TYPE_2="2";


    public final static String X_ACCESS_TOKEN = "X-Access-Token";
    public final static String X_SIGN = "X-Sign";
    public final static String X_TIMESTAMP = "X-TIMESTAMP";

    /**
     *        
     */
    public final static String TENANT_ID = "tenant-id";

    /**
     *                 
     */
    public final static String CLOUD_SERVER_KEY = "spring.cloud.nacos.discovery.server-addr";

    /**
     *           /                       
     */
    public final static String THIRD_LOGIN_CODE = "third_login_code";

    /**
     *    APP    ：   -->    APP
     */
    String THIRD_SYNC_TO_APP = "SYNC_TO_APP";
    /**
     *    APP    ：   APP -->   
     */
    String THIRD_SYNC_TO_LOCAL = "SYNC_TO_LOCAL";

    /**         ：0=    */
    String ANNOUNCEMENT_SEND_STATUS_0 = "0";
    /**         ：1=    */
    String ANNOUNCEMENT_SEND_STATUS_1 = "1";
    /**         ：2=    */
    String ANNOUNCEMENT_SEND_STATUS_2 = "2";

}
