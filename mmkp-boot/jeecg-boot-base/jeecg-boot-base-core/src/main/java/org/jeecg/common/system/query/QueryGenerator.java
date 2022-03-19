package org.jeecg.common.system.query;

import java.beans.PropertyDescriptor;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.apache.commons.beanutils.PropertyUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.common.constant.DataBaseConstant;
import org.jeecg.common.system.util.JeecgDataAutorUtils;
import org.jeecg.common.system.util.JwtUtil;
import org.jeecg.common.system.vo.SysPermissionDataRuleModel;
import org.jeecg.common.util.CommonUtils;
import org.jeecg.common.util.DateUtils;
import org.jeecg.common.util.SqlInjectionUtil;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.util.NumberUtils;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class QueryGenerator {
	public static final String SQL_RULES_COLUMN = "SQL_RULES_COLUMN";

	private static final String BEGIN = "_begin";
	private static final String END = "_end";
	/**
	 *       ，
	 */
	private static final String MULTI = "_MultiString";
	private static final String STAR = "*";
	private static final String COMMA = ",";
	/**
	 *                 【  】
	 */
	public static final String QUERY_COMMA_ESCAPE = "++";
	private static final String NOT_EQUAL = "!";
	/**         ，       */
	private static final String QUERY_SEPARATE_KEYWORD = " ";
	/**            */
	private static final String SUPER_QUERY_PARAMS = "superQueryParams";
	/**                  */
	private static final String SUPER_QUERY_MATCH_TYPE = "superQueryMatchType";
	/**     */
	public static final String SQL_SQ = "'";
	/**   */
	private static final String ORDER_COLUMN = "column";
	/**    */
	private static final String ORDER_TYPE = "order";
	private static final String ORDER_TYPE_ASC = "ASC";

	/**mysql              （_、\）*/
	public static final String LIKE_MYSQL_SPECIAL_STRS = "_,%";
	
	/**      */
	private static final ThreadLocal<SimpleDateFormat> local = new ThreadLocal<SimpleDateFormat>();
	private static SimpleDateFormat getTime(){
		SimpleDateFormat time = local.get();
		if(time == null){
			time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			local.set(time);
		}
		return time;
	}
	
	/**
	 *          QueryWrapper
	 * @param searchObj
	 * @param parameterMap request.getParameterMap()
	 * @return QueryWrapper
	 */
	public static <T> QueryWrapper<T> initQueryWrapper(T searchObj,Map<String, String[]> parameterMap){
		long start = System.currentTimeMillis();
		QueryWrapper<T> queryWrapper = new QueryWrapper<T>();
		installMplus(queryWrapper, searchObj, parameterMap);
		log.debug("---            ,  :"+(System.currentTimeMillis()-start)+"  ----");
		return queryWrapper;
	}

	/**
	 *   Mybatis Plus
	 * <p>               :
	 * <br>1.  QueryWrapper   LambdaQueryWrapper;
	 * <br>2.   QueryWrapper          
	 * <br>    : QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>(jeecgDemo);
	 * <br>    :QueryWrapper<JeecgDemo> queryWrapper = new QueryWrapper<JeecgDemo>();
	 * <br>3.               {@link #initQueryWrapper}      
	 */
	public static void installMplus(QueryWrapper<?> queryWrapper,Object searchObj,Map<String, String[]> parameterMap) {
		
		/*
		 *   :                                        orgCode    #{sys_org_code}
		         SQL  orgCode in #{sys_org_code} 
		                    : orgCode    #{sys_org_code}        SQL: orgCode = '#{sys_org_code}'
		*/
		
		//                            
		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(searchObj);
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		
		//       SQL   
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
			}
		}
		
		String name, type, column;
		// update-begin--Author:taoyan  Date:20200923 for：issues/1671         @TableField(exist = false),  DB  -------
		//                                     TableField             
		Map<String,String> fieldColumnMap = new HashMap<String,String>();
		for (int i = 0; i < origDescriptors.length; i++) {
			//aliasName = origDescriptors[i].getName();  mybatis                   
			name = origDescriptors[i].getName();
			type = origDescriptors[i].getPropertyType().toString();
			try {
				if (judgedIsUselessField(name)|| !PropertyUtils.isReadable(searchObj, name)) {
					continue;
				}

				Object value = PropertyUtils.getSimpleProperty(searchObj, name);
				column = getTableFieldName(searchObj.getClass(), name);
				if(column==null){
					//column null                @TableField(exist = false)         
					continue;
				}
				fieldColumnMap.put(name,column);
				//      
				if(ruleMap.containsKey(name)) {
					addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
				}
				//    
				doIntervalQuery(queryWrapper, parameterMap, type, name, column);
				//                       
				//TODO                                
				if (null != value && value.toString().startsWith(COMMA) && value.toString().endsWith(COMMA)) {
					String multiLikeval = value.toString().replace(",,", COMMA);
					String[] vals = multiLikeval.substring(1, multiLikeval.length()).split(COMMA);
					final String field = oConvertUtils.camelToUnderline(column);
					if(vals.length>1) {
						queryWrapper.and(j -> {
							j = j.like(field,vals[0]);
							for (int k=1;k<vals.length;k++) {
								j = j.or().like(field,vals[k]);
							}
							//return j;
						});
					}else {
						queryWrapper.and(j -> j.like(field,vals[0]));
					}
				}else {
					//                       
					QueryRuleEnum rule = convert2Rule(value);
					value = replaceValue(rule,value);
					// add -begin                 
					//if( (rule==null || QueryRuleEnum.EQ.equals(rule)) && "class java.lang.String".equals(type)) {
						//             ，    
						//rule = QueryRuleEnum.LIKE;
					//}
					// add -end                 
					addEasyQuery(queryWrapper, column, rule, value);
				}
				
			} catch (Exception e) {
				log.error(e.getMessage(), e);
			}
		}
		//         
		doMultiFieldsOrder(queryWrapper, parameterMap);
				
		//    
		doSuperQuery(queryWrapper, parameterMap, fieldColumnMap);
		// update-end--Author:taoyan  Date:20200923 for：issues/1671         @TableField(exist = false),  DB  -------
		
	}


	/**
	 *     
	 * @param queryWrapper query  
	 * @param parameterMap   map
	 * @param type             
	 * @param filedName        
	 * @param columnName      
	 */
	private static void doIntervalQuery(QueryWrapper<?> queryWrapper, Map<String, String[]> parameterMap, String type, String filedName, String columnName) throws ParseException {
		//            
		String endValue = null,beginValue = null;
		if (parameterMap != null && parameterMap.containsKey(filedName + BEGIN)) {
			beginValue = parameterMap.get(filedName + BEGIN)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, beginValue, QueryRuleEnum.GE);

		}
		if (parameterMap != null && parameterMap.containsKey(filedName + END)) {
			endValue = parameterMap.get(filedName + END)[0].trim();
			addQueryByRule(queryWrapper, columnName, type, endValue, QueryRuleEnum.LE);
		}
		//    
		if (parameterMap != null && parameterMap.containsKey(filedName + MULTI)) {
			endValue = parameterMap.get(filedName + MULTI)[0].trim();
			addQueryByRule(queryWrapper, columnName.replace(MULTI,""), type, endValue, QueryRuleEnum.IN);
		}
	}
	
	//      TODO       
	public static void doMultiFieldsOrder(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap) {
		String column=null,order=null;
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_COLUMN)) {
			column = parameterMap.get(ORDER_COLUMN)[0];
		}
		if(parameterMap!=null&& parameterMap.containsKey(ORDER_TYPE)) {
			order = parameterMap.get(ORDER_TYPE)[0];
		}
        log.info("    >> :" + column + ",    :" + order);
		if (oConvertUtils.isNotEmpty(column) && oConvertUtils.isNotEmpty(order)) {
			//    ，          
			if(column.endsWith(CommonConstant.DICT_TEXT_SUFFIX)) {
				column = column.substring(0, column.lastIndexOf(CommonConstant.DICT_TEXT_SUFFIX));
			}
			//SQL  check
			SqlInjectionUtil.filterContent(column);

			//update-begin--Author:scott  Date:20210531 for：36            -------
			//       
			//       _         {....,column: 'column1,column2',order: 'desc'}    sql "column1,column2 desc"
			//     _         {....,column: 'column1,column2',order: 'desc'}    sql "column1 desc,column2 desc"
			if (order.toUpperCase().indexOf(ORDER_TYPE_ASC)>=0) {
				//queryWrapper.orderByAsc(oConvertUtils.camelToUnderline(column));
				String columnStr = oConvertUtils.camelToUnderline(column);
				String[] columnArray = columnStr.split(",");
				queryWrapper.orderByAsc(Arrays.asList(columnArray));
			} else {
				//queryWrapper.orderByDesc(oConvertUtils.camelToUnderline(column));
				String columnStr = oConvertUtils.camelToUnderline(column);
				String[] columnArray = columnStr.split(",");
				queryWrapper.orderByDesc(Arrays.asList(columnArray));
			}
			//update-end--Author:scott  Date:20210531 for：36            -------
		}
	}
	
	/**
	 *     
	 * @param queryWrapper     
	 * @param parameterMap     
	 * @param fieldColumnMap             map
	 */
	public static void doSuperQuery(QueryWrapper<?> queryWrapper,Map<String, String[]> parameterMap, Map<String,String> fieldColumnMap) {
		if(parameterMap!=null&& parameterMap.containsKey(SUPER_QUERY_PARAMS)){
			String superQueryParams = parameterMap.get(SUPER_QUERY_PARAMS)[0];
			String superQueryMatchType = parameterMap.get(SUPER_QUERY_MATCH_TYPE) != null ? parameterMap.get(SUPER_QUERY_MATCH_TYPE)[0] : MatchTypeEnum.AND.getValue();
            MatchTypeEnum matchType = MatchTypeEnum.getByValue(superQueryMatchType);
            // update-begin--Author:sunjianlei  Date:20200325 for：              ，             -------
            try {
                superQueryParams = URLDecoder.decode(superQueryParams, "UTF-8");
                List<QueryCondition> conditions = JSON.parseArray(superQueryParams, QueryCondition.class);
                if (conditions == null || conditions.size() == 0) {
                    return;
                }
                log.info("---      -->" + conditions.toString());
                queryWrapper.and(andWrapper -> {
                    for (int i = 0; i < conditions.size(); i++) {
                        QueryCondition rule = conditions.get(i);
                        if (oConvertUtils.isNotEmpty(rule.getField())
                                && oConvertUtils.isNotEmpty(rule.getRule())
                                && oConvertUtils.isNotEmpty(rule.getVal())) {

                            log.debug("SuperQuery ==> " + rule.toString());

                            //update-begin-author:taoyan date:20201228 for: 【    】 oracle         
							Object queryValue = rule.getVal();
                            if("date".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(),DateUtils.date_sdf.get());
							}else if("datetime".equals(rule.getType())){
								queryValue = DateUtils.str2Date(rule.getVal(), DateUtils.datetimeFormat.get());
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】          ，            ----
							String dbType = rule.getDbType();
							if (oConvertUtils.isNotEmpty(dbType)) {
								try {
									String valueStr = String.valueOf(queryValue);
									switch (dbType.toLowerCase().trim()) {
										case "int":
											queryValue = Integer.parseInt(valueStr);
											break;
										case "bigdecimal":
											queryValue = new BigDecimal(valueStr);
											break;
										case "short":
											queryValue = Short.parseShort(valueStr);
											break;
										case "long":
											queryValue = Long.parseLong(valueStr);
											break;
										case "float":
											queryValue = Float.parseFloat(valueStr);
											break;
										case "double":
											queryValue = Double.parseDouble(valueStr);
											break;
										case "boolean":
											queryValue = Boolean.parseBoolean(valueStr);
											break;
									}
								} catch (Exception e) {
									log.error("         ：", e);
								}
							}
							// update-begin--author:sunjianlei date:20210702 for：【/issues/I3VR8E】          ，            ----
                            addEasyQuery(andWrapper, fieldColumnMap.get(rule.getField()), QueryRuleEnum.getByValue(rule.getRule()), queryValue);
							//update-end-author:taoyan date:20201228 for: 【    】 oracle         

                            //        OR，   OR
                            if (MatchTypeEnum.OR == matchType && i < (conditions.size() - 1)) {
                                andWrapper.or();
                            }
                        }
                    }
                    //return andWrapper;
                });
            } catch (UnsupportedEncodingException e) {
                log.error("--          ：" + superQueryParams, e);
            } catch (Exception e) {
                log.error("--        ：" + e.getMessage());
                e.printStackTrace();
            }
            // update-end--Author:sunjianlei  Date:20200325 for：              ，             -------
		}
		//log.info(" superQuery getCustomSqlSegment: "+ queryWrapper.getCustomSqlSegment());
	}
	/**
	 *                  
	 *   ><= like in !
	 * @param value
	 * @return
	 */
	private static QueryRuleEnum convert2Rule(Object value) {
		//      
		// update-begin-author:taoyan date:20210629 for:           return null        null  
		if (value == null) {
			return QueryRuleEnum.EQ;
		}
		String val = (value + "").toString().trim();
		if (val.length() == 0) {
			return QueryRuleEnum.EQ;
		}
		// update-end-author:taoyan date:20210629 for:           return null        null  
		QueryRuleEnum rule =null;

		//update-begin--Author:scott  Date:20190724 for：initQueryWrapper  sql       #284-------------------
		//TODO     ，     le lt ge gt
		// step 2 .>= =<
		if (rule == null && val.length() >= 3) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(2, 3))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 2));
			}
		}
		// step 1 .> <
		if (rule == null && val.length() >= 2) {
			if(QUERY_SEPARATE_KEYWORD.equals(val.substring(1, 2))){
				rule = QueryRuleEnum.getByValue(val.substring(0, 1));
			}
		}
		//update-end--Author:scott  Date:20190724 for：initQueryWrapper  sql       #284---------------------

		// step 3 like
		if (rule == null && val.contains(STAR)) {
			if (val.startsWith(STAR) && val.endsWith(STAR)) {
				rule = QueryRuleEnum.LIKE;
			} else if (val.startsWith(STAR)) {
				rule = QueryRuleEnum.LEFT_LIKE;
			} else if(val.endsWith(STAR)){
				rule = QueryRuleEnum.RIGHT_LIKE;
			}
		}

		// step 4 in
		if (rule == null && val.contains(COMMA)) {
			//TODO in         bug                 in          
			rule = QueryRuleEnum.IN;
		}
		// step 5 != 
		if(rule == null && val.startsWith(NOT_EQUAL)){
			rule = QueryRuleEnum.NE;
		}
		// step 6 xx+xx+xx                              in      ++  【     】
		if(rule == null && val.indexOf(QUERY_COMMA_ESCAPE)>0){
			rule = QueryRuleEnum.EQ_WITH_ADD;
		}

		//update-begin--Author:taoyan  Date:20201229 for：initQueryWrapper  sql       #284---------------------
		//    ：Oracle    to_date('xxx','yyyy-MM-dd')    ，     in  ，      
		if(rule == QueryRuleEnum.IN && val.indexOf("yyyy-MM-dd")>=0 && val.indexOf("to_date")>=0){
			rule = QueryRuleEnum.EQ;
		}
		//update-end--Author:taoyan  Date:20201229 for：initQueryWrapper  sql       #284---------------------

		return rule != null ? rule : QueryRuleEnum.EQ;
	}
	
	/**
	 *         
	 * 
	 * @param rule
	 * @param value
	 * @return
	 */
	private static Object replaceValue(QueryRuleEnum rule, Object value) {
		if (rule == null) {
			return null;
		}
		if (! (value instanceof String)){
			return value;
		}
		String val = (value + "").toString().trim();
		if (rule == QueryRuleEnum.LIKE) {
			value = val.substring(1, val.length() - 1);
			//mysql              （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.LEFT_LIKE || rule == QueryRuleEnum.NE) {
			value = val.substring(1);
			//mysql              （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.RIGHT_LIKE) {
			value = val.substring(0, val.length() - 1);
			//mysql              （_、\）
			value = specialStrConvert(value.toString());
		} else if (rule == QueryRuleEnum.IN) {
			value = val.split(",");
		} else if (rule == QueryRuleEnum.EQ_WITH_ADD) {
			value = val.replaceAll("\\+\\+", COMMA);
		}else {
			//update-begin--Author:scott  Date:20190724 for：initQueryWrapper  sql       #284-------------------
			if(val.startsWith(rule.getValue())){
				//TODO          ->                  ，      （  ：>=  ）
				value = val.replaceFirst(rule.getValue(),"");
			}else if(val.startsWith(rule.getCondition()+QUERY_SEPARATE_KEYWORD)){
				value = val.replaceFirst(rule.getCondition()+QUERY_SEPARATE_KEYWORD,"").trim();
			}
			//update-end--Author:scott  Date:20190724 for：initQueryWrapper  sql       #284-------------------
		}
		return value;
	}
	
	private static void addQueryByRule(QueryWrapper<?> queryWrapper,String name,String type,String value,QueryRuleEnum rule) throws ParseException {
		if(oConvertUtils.isNotEmpty(value)) {
			Object temp;
			//         ，    
			if(value.indexOf(COMMA)!=-1){
				temp = value;
				addEasyQuery(queryWrapper, name, rule, temp);
				return;
			}

			switch (type) {
			case "class java.lang.Integer":
				temp =  Integer.parseInt(value);
				break;
			case "class java.math.BigDecimal":
				temp =  new BigDecimal(value);
				break;
			case "class java.lang.Short":
				temp =  Short.parseShort(value);
				break;
			case "class java.lang.Long":
				temp =  Long.parseLong(value);
				break;
			case "class java.lang.Float":
				temp =   Float.parseFloat(value);
				break;
			case "class java.lang.Double":
				temp =  Double.parseDouble(value);
				break;
			case "class java.util.Date":
				temp = getDateQueryByRule(value, rule);
				break;
			default:
				temp = value;
				break;
			}
			addEasyQuery(queryWrapper, name, rule, temp);
		}
	}
	
	/**
	 *         
	 * @param value
	 * @param rule
	 * @return
	 * @throws ParseException
	 */
	private static Date getDateQueryByRule(String value,QueryRuleEnum rule) throws ParseException {
		Date date = null;
		if(value.length()==10) {
			if(rule==QueryRuleEnum.GE) {
				//    
				date = getTime().parse(value + " 00:00:00");
			}else if(rule==QueryRuleEnum.LE) {
				//    
				date = getTime().parse(value + " 23:59:59");
			}
			//TODO            oracle      
		}
		if(date==null) {
			date = getTime().parse(value);
		}
		return date;
	}
	
	/**
	  *           
	 * @param queryWrapper QueryWrapper
	 * @param name             
	 * @param rule             
	 * @param value             
	 */
	private static void addEasyQuery(QueryWrapper<?> queryWrapper, String name, QueryRuleEnum rule, Object value) {
		if (value == null || rule == null || oConvertUtils.isEmpty(value)) {
			return;
		}
		name = oConvertUtils.camelToUnderline(name);
		log.info("--    -->"+name+" "+rule.getValue()+" "+value);
		switch (rule) {
		case GT:
			queryWrapper.gt(name, value);
			break;
		case GE:
			queryWrapper.ge(name, value);
			break;
		case LT:
			queryWrapper.lt(name, value);
			break;
		case LE:
			queryWrapper.le(name, value);
			break;
		case EQ:
		case EQ_WITH_ADD:
			queryWrapper.eq(name, value);
			break;
		case NE:
			queryWrapper.ne(name, value);
			break;
		case IN:
			if(value instanceof String) {
				queryWrapper.in(name, (Object[])value.toString().split(","));
			}else if(value instanceof String[]) {
				queryWrapper.in(name, (Object[]) value);
			}
			//update-begin-author:taoyan date:20200909 for:【bug】in           postgresql #1671
			else if(value.getClass().isArray()) {
				queryWrapper.in(name, (Object[])value);
			}else {
				queryWrapper.in(name, value);
			}
			//update-end-author:taoyan date:20200909 for:【bug】in           postgresql #1671
			break;
		case LIKE:
			queryWrapper.like(name, value);
			break;
		case LEFT_LIKE:
			queryWrapper.likeLeft(name, value);
			break;
		case RIGHT_LIKE:
			queryWrapper.likeRight(name, value);
			break;
		default:
			log.info("--        ---");
			break;
		}
	}
	/**
	 * 
	 * @param name
	 * @return
	 */
	private static boolean judgedIsUselessField(String name) {
		return "class".equals(name) || "ids".equals(name)
				|| "page".equals(name) || "rows".equals(name)
				|| "sort".equals(name) || "order".equals(name);
	}

	

	/**
	 *              
	 * @return
	 */
	public static Map<String, SysPermissionDataRuleModel> getRuleMap() {
		Map<String, SysPermissionDataRuleModel> ruleMap = new HashMap<String, SysPermissionDataRuleModel>();
		List<SysPermissionDataRuleModel> list =JeecgDataAutorUtils.loadDataSearchConditon();
		if(list != null&&list.size()>0){
			if(list.get(0)==null){
				return ruleMap;
			}
			for (SysPermissionDataRuleModel rule : list) {
				String column = rule.getRuleColumn();
				if(QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
					column = SQL_RULES_COLUMN+rule.getId();
				}
				ruleMap.put(column, rule);
			}
		}
		return ruleMap;
	}

	/**
	 *              
	 * @return
	 */
	public static Map<String, SysPermissionDataRuleModel> getRuleMap(List<SysPermissionDataRuleModel> list) {
		Map<String, SysPermissionDataRuleModel> ruleMap = new HashMap<String, SysPermissionDataRuleModel>();
		if(list==null){
			list =JeecgDataAutorUtils.loadDataSearchConditon();
		}
		if(list != null&&list.size()>0){
			if(list.get(0)==null){
				return ruleMap;
			}
			for (SysPermissionDataRuleModel rule : list) {
				String column = rule.getRuleColumn();
				if(QueryRuleEnum.SQL_RULES.getValue().equals(rule.getRuleConditions())) {
					column = SQL_RULES_COLUMN+rule.getId();
				}
				ruleMap.put(column, rule);
			}
		}
		return ruleMap;
	}
	
	private static void addRuleToQueryWrapper(SysPermissionDataRuleModel dataRule, String name, Class propertyType, QueryWrapper<?> queryWrapper) {
		QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
		if(rule.equals(QueryRuleEnum.IN) && ! propertyType.equals(String.class)) {
			String[] values = dataRule.getRuleValue().split(",");
			Object[] objs = new Object[values.length];
			for (int i = 0; i < values.length; i++) {
				objs[i] = NumberUtils.parseNumber(values[i], propertyType);
			}
			addEasyQuery(queryWrapper, name, rule, objs);
		}else {
			if (propertyType.equals(String.class)) {
				addEasyQuery(queryWrapper, name, rule, converRuleValue(dataRule.getRuleValue()));
			}else if (propertyType.equals(Date.class)) {
				String dateStr =converRuleValue(dataRule.getRuleValue());
				if(dateStr.length()==10){
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.date_sdf.get()));
				}else{
					addEasyQuery(queryWrapper, name, rule, DateUtils.str2Date(dateStr,DateUtils.datetimeFormat.get()));
				}
			}else {
				addEasyQuery(queryWrapper, name, rule, NumberUtils.parseNumber(dataRule.getRuleValue(), propertyType));
			}
		}
	}
	
	public static String converRuleValue(String ruleValue) {
		String value = JwtUtil.getUserSystemData(ruleValue,null);
		return value!= null ? value : ruleValue;
	}

	/**
	* @author: scott
	* @Description:         
	* @date: 2020/3/19 21:26
	* @param ruleValue: 
	* @Return: java.lang.String
	*/
	public static String trimSingleQuote(String ruleValue) {
		if (oConvertUtils.isEmpty(ruleValue)) {
			return "";
		}
		if (ruleValue.startsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(1);
		}
		if (ruleValue.endsWith(QueryGenerator.SQL_SQ)) {
			ruleValue = ruleValue.substring(0, ruleValue.length() - 1);
		}
		return ruleValue;
	}
	
	public static String getSqlRuleValue(String sqlRule){
		try {
			Set<String> varParams = getSqlRuleParams(sqlRule);
			for(String var:varParams){
				String tempValue = converRuleValue(var);
				sqlRule = sqlRule.replace("#{"+var+"}",tempValue);
			}
		} catch (Exception e) {
			log.error(e.getMessage(), e);
		}
		return sqlRule;
	}
	
	/**
	 *   sql  #{key}   key   set
	 */
	public static Set<String> getSqlRuleParams(String sql) {
		if(oConvertUtils.isEmpty(sql)){
			return null;
		}
		Set<String> varParams = new HashSet<String>();
		String regex = "\\#\\{\\w+\\}";
		
		Pattern p = Pattern.compile(regex);
		Matcher m = p.matcher(sql);
		while(m.find()){
			String var = m.group();
			varParams.add(var.substring(var.indexOf("{")+1,var.indexOf("}")));
		}
		return varParams;
	}
	
	/**
	 *        
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString) {
		return getSingleQueryConditionSql(field, alias, value, isString,null);
	}

	/**
	 *                
	 * @param field
	 * @param alias
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	public static String getSingleQueryConditionSql(String field,String alias,Object value,boolean isString, String dataBaseType) {
		if (value == null) {
			return "";
		}
		field =  alias+oConvertUtils.camelToUnderline(field);
		QueryRuleEnum rule = QueryGenerator.convert2Rule(value);
		return getSingleSqlByRule(rule, field, value, isString, dataBaseType);
	}

	/**
	 *           
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	public static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString, String dataBaseType) {
		String res = "";
		switch (rule) {
		case GT:
			res =field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case GE:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case LT:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case LE:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case EQ:
			res = field+rule.getValue()+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case EQ_WITH_ADD:
			res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case NE:
			res = field+" <> "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		case IN:
			res = field + " in "+getInConditionValue(value, isString);
			break;
		case LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		case LEFT_LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		case RIGHT_LIKE:
			res = field + " like "+getLikeConditionValue(value);
			break;
		default:
			res = field+" = "+getFieldConditionValue(value, isString, dataBaseType);
			break;
		}
		return res;
	}


	/**
	 *           
	 * @param rule
	 * @param field
	 * @param value
	 * @param isString
	 * @return
	 */
	public static String getSingleSqlByRule(QueryRuleEnum rule,String field,Object value,boolean isString) {
		return getSingleSqlByRule(rule, field, value, isString, null);
	}

	/**
	 *         
	 * @param value
	 * @param isString
	 * @param dataBaseType
	 * @return
	 */
	private static String getFieldConditionValue(Object value,boolean isString, String dataBaseType) {
		String str = value.toString().trim();
		if(str.startsWith("!")) {
			str = str.substring(1);
		}else if(str.startsWith(">=")) {
			str = str.substring(2);
		}else if(str.startsWith("<=")) {
			str = str.substring(2);
		}else if(str.startsWith(">")) {
			str = str.substring(1);
		}else if(str.startsWith("<")) {
			str = str.substring(1);
		}else if(str.indexOf(QUERY_COMMA_ESCAPE)>0) {
			str = str.replaceAll("\\+\\+", COMMA);
		}
		if(dataBaseType==null){
			dataBaseType = getDbType();
		}
		if(isString) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(dataBaseType)){
				return " N'"+str+"' ";
			}else{
				return " '"+str+"' ";
			}
		}else {
			//                 popup                 “‘admin’”     
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(dataBaseType) && str.endsWith("'") && str.startsWith("'")){
				return " N"+str;
			}
			return value.toString();
		}
	}

	private static String getInConditionValue(Object value,boolean isString) {
		//update-begin-author:taoyan date:20210628 for:         ,  sql  
		String[] temp = value.toString().split(",");
		if(temp.length==0){
			return "('')";
		}
		if(isString) {
			List<String> res = new ArrayList<>();
			for (String string : temp) {
				if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
					res.add("N'"+string+"'");
				}else{
					res.add("'"+string+"'");
				}
			}
			return "("+String.join("," ,res)+")";
		}else {
			return "("+value.toString()+")";
		}
		//update-end-author:taoyan date:20210628 for:         ,  sql  
	}
	
	private static String getLikeConditionValue(Object value) {
		String str = value.toString().trim();
		if(str.startsWith("*") && str.endsWith("*")) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+str.substring(1,str.length()-1)+"%'";
			}else{
				return "'%"+str.substring(1,str.length()-1)+"%'";
			}
		}else if(str.startsWith("*")) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'%"+str.substring(1)+"'";
			}else{
				return "'%"+str.substring(1)+"'";
			}
		}else if(str.endsWith("*")) {
			if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
				return "N'"+str.substring(0,str.length()-1)+"%'";
			}else{
				return "'"+str.substring(0,str.length()-1)+"%'";
			}
		}else {
			if(str.indexOf("%")>=0) {
				if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
					if(str.startsWith("'") && str.endsWith("'")){
						return "N"+str;
					}else{
						return "N"+"'"+str+"'";
					}
				}else{
					if(str.startsWith("'") && str.endsWith("'")){
						return str;
					}else{
						return "'"+str+"'";
					}
				}
			}else {
				if(DataBaseConstant.DB_TYPE_SQLSERVER.equals(getDbType())){
					return "N'%"+str+"%'";
				}else{
					return "'%"+str+"%'";
				}
			}
		}
	}
	
	/**
	 *                SQL   
	 * @param clazz
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static String installAuthJdbc(Class<?> clazz) {
		StringBuffer sb = new StringBuffer();
		//    
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(clazz);
		String sql_and = " and ";
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				sb.append(sql_and+getSqlRuleValue(ruleMap.get(c).getRuleValue()));
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			if(ruleMap.containsKey(name)) {
				column = getTableFieldName(clazz, name);
				if(column==null){
					continue;
				}
				SysPermissionDataRuleModel dataRule = ruleMap.get(name);
				QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
				Class propType = origDescriptors[i].getPropertyType();
				boolean isString = propType.equals(String.class);
				Object value;
				if(isString) {
					value = converRuleValue(dataRule.getRuleValue());
				}else {
					value = NumberUtils.parseNumber(dataRule.getRuleValue(),propType);
				}
				String filedSql = getSingleSqlByRule(rule, oConvertUtils.camelToUnderline(column), value,isString);
				sb.append(sql_and+filedSql);
			}
		}
		log.info("query auth sql is:"+sb.toString());
		return sb.toString();
	}
	
	/**
	  *            mp     
	 * @param queryWrapper
	 * @param clazz
	 * @return
	 */
	public static void installAuthMplus(QueryWrapper<?> queryWrapper,Class<?> clazz) {
		//    
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		PropertyDescriptor origDescriptors[] = PropertyUtils.getPropertyDescriptors(clazz);
		for (String c : ruleMap.keySet()) {
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				queryWrapper.and(i ->i.apply(getSqlRuleValue(ruleMap.get(c).getRuleValue())));
			}
		}
		String name, column;
		for (int i = 0; i < origDescriptors.length; i++) {
			name = origDescriptors[i].getName();
			if (judgedIsUselessField(name)) {
				continue;
			}
			column = getTableFieldName(clazz, name);
			if(column==null){
				continue;
			}
			if(ruleMap.containsKey(name)) {
				addRuleToQueryWrapper(ruleMap.get(name), column, origDescriptors[i].getPropertyType(), queryWrapper);
			}
		}
	}

	/**
	 *   sql      
	 * @param sql
	 * @return
	 */
	public static String convertSystemVariables(String sql){
		return getSqlRuleValue(sql);
	}

	/**
	 *             sql                    
	 * @return
	 */
	public static String getAllConfigAuth() {
		StringBuffer sb = new StringBuffer();
		//    
		Map<String,SysPermissionDataRuleModel> ruleMap = getRuleMap();
		String sql_and = " and ";
		for (String c : ruleMap.keySet()) {
			SysPermissionDataRuleModel dataRule = ruleMap.get(c);
			String ruleValue = dataRule.getRuleValue();
			if(oConvertUtils.isEmpty(ruleValue)){
				continue;
			}
			if(oConvertUtils.isNotEmpty(c) && c.startsWith(SQL_RULES_COLUMN)){
				sb.append(sql_and+getSqlRuleValue(ruleValue));
			}else{
				boolean isString  = false;
				ruleValue = ruleValue.trim();
				if(ruleValue.startsWith("'") && ruleValue.endsWith("'")){
					isString = true;
					ruleValue = ruleValue.substring(1,ruleValue.length()-1);
				}
				QueryRuleEnum rule = QueryRuleEnum.getByValue(dataRule.getRuleConditions());
				String value = converRuleValue(ruleValue);
				String filedSql = getSingleSqlByRule(rule, c, value,isString);
				sb.append(sql_and+filedSql);
			}
		}
		log.info("query auth sql is = "+sb.toString());
		return sb.toString();
	}



	/**
	 *          
	 */
	private static String getDbType(){
		return CommonUtils.getDatabaseType();
	}


	/**
	 *   class       
	 * @param clazz
	 * @return
	 */
	private static List<Field> getClassFields(Class<?> clazz) {
		List<Field> list = new ArrayList<Field>();
		Field[] fields;
		do{
			fields = clazz.getDeclaredFields();
			for(int i = 0;i<fields.length;i++){
				list.add(fields[i]);
			}
			clazz = clazz.getSuperclass();
		}while(clazz!= Object.class&&clazz!=null);
		return list;
	}

	/**
	 *       
	 * @param clazz
	 * @param name
	 * @return
	 */
	private static String getTableFieldName(Class<?> clazz, String name) {
		try {
			//        @TableField(exist = false),  DB  
			Field field = null;
			try {
				field = clazz.getDeclaredField(name);
			} catch (NoSuchFieldException e) {
				//e.printStackTrace();
			}

			//    ，        
			if (field == null) {
				List<Field> allFields = getClassFields(clazz);
				List<Field> searchFields = allFields.stream().filter(a -> a.getName().equals(name)).collect(Collectors.toList());
				if(searchFields!=null && searchFields.size()>0){
					field = searchFields.get(0);
				}
			}

			if (field != null) {
				TableField tableField = field.getAnnotation(TableField.class);
				if (tableField != null){
					if(tableField.exist() == false){
						//     TableField false          
						return null;
					}else{
						String column = tableField.value();
						//     TableField value          
						if(!"".equals(column)){
							return column;
						}
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return name;
	}

	/**
	 * mysql              （_、\）
	 *
	 * @param value:
	 * @Return: java.lang.String
	 */
	private static String specialStrConvert(String value) {
		if (DataBaseConstant.DB_TYPE_MYSQL.equals(getDbType()) || DataBaseConstant.DB_TYPE_MARIADB.equals(getDbType())) {
			String[] special_str = QueryGenerator.LIKE_MYSQL_SPECIAL_STRS.split(",");
			for (String str : special_str) {
				if (value.indexOf(str) !=-1) {
					value = value.replace(str, "\\" + str);
				}
			}
		}
		return value;
	}
}
