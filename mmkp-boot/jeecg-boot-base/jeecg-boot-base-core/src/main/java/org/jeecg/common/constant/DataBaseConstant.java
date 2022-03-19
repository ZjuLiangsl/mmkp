package org.jeecg.common.constant;
/**
 *         
 */
public interface DataBaseConstant {
	//*********     ****************************************
	public static final String DB_TYPE_MYSQL = "MYSQL";
	public static final String DB_TYPE_ORACLE = "ORACLE";
	public static final String DB_TYPE_DM = "DM";//     
	public static final String DB_TYPE_POSTGRESQL = "POSTGRESQL";
	public static final String DB_TYPE_SQLSERVER = "SQLSERVER";
	public static final String DB_TYPE_MARIADB = "MARIADB";

//	//      ，   database_type   
//	public static final String DB_TYPE_MYSQL_NUM = "1";
//	public static final String DB_TYPE_MYSQL7_NUM = "6";
//	public static final String DB_TYPE_ORACLE_NUM = "2";
//	public static final String DB_TYPE_SQLSERVER_NUM = "3";
//	public static final String DB_TYPE_POSTGRESQL_NUM = "4";
//	public static final String DB_TYPE_MARIADB_NUM = "5";

	//*********       ****************************************
	/**
	 *   -      
	 */
	public static final String SYS_ORG_CODE = "sysOrgCode";
	/**
	 *   -      
	 */
	public static final String SYS_ORG_CODE_TABLE = "sys_org_code";
	/**
	 *   -      
	 */
	public static final String SYS_MULTI_ORG_CODE = "sysMultiOrgCode";
	/**
	 *   -      
	 */
	public static final String SYS_MULTI_ORG_CODE_TABLE = "sys_multi_org_code";
	/**
	 *   -      （        ）
	 */
	public static final String SYS_USER_CODE = "sysUserCode";
	/**
	 *   -      （        ）
	 */
	public static final String SYS_USER_CODE_TABLE = "sys_user_code";
	
	/**
	 *         
	 */
	public static final String SYS_USER_NAME = "sysUserName";
	/**
	 *         
	 */
	public static final String SYS_USER_NAME_TABLE = "sys_user_name";
	/**
	 *     "yyyy-MM-dd"
	 */
	public static final String SYS_DATE = "sysDate";
	/**
	 *     "yyyy-MM-dd"
	 */
	public static final String SYS_DATE_TABLE = "sys_date";
	/**
	 *     "yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME = "sysTime";
	/**
	 *     "yyyy-MM-dd HH:mm"
	 */
	public static final String SYS_TIME_TABLE = "sys_time";
	/**
	 *   -      
	 */
	public static final String SYS_BASE_PATH = "sys_base_path";
	//*********       ****************************************
	
	
	//*********        ****************************************
	/**
	 *        
	 */
	public static final String CREATE_BY_TABLE = "create_by";
	/**
	 *        
	 */
	public static final String CREATE_BY = "createBy";
	/**
	 *       
	 */
	public static final String CREATE_TIME_TABLE = "create_time";
	/**
	 *       
	 */
	public static final String CREATE_TIME = "createTime";
	/**
	 *         
	 */
	public static final String UPDATE_BY_TABLE = "update_by";
	/**
	 *         
	 */
	public static final String UPDATE_BY = "updateBy";
	/**
	 *       
	 */
	public static final String UPDATE_TIME = "updateTime";
	/**
	 *       
	 */
	public static final String UPDATE_TIME_TABLE = "update_time";
	
	/**
	 *       
	 */
	public static final String BPM_STATUS = "bpmStatus";
	/**
	 *       
	 */
	public static final String BPM_STATUS_TABLE = "bpm_status";
	//*********        ****************************************


	/**
	 *   ID      
	 */
	String TENANT_ID = "tenantId";
	/**
	 *   ID       
	 */
	String TENANT_ID_TABLE = "tenant_id";
}
