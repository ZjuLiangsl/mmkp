package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.poi.ss.formula.functions.T;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.exception.JeecgBootException;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.ReflectHelper;
import org.jeecg.common.util.oConvertUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Spring JDBC 实时数据库访问
 *
 * @author chenguobin
 * @version 1.0
 * @date 2014-09-05
 */
@Slf4j
public class DynamicDBUtil {

    /**
     * 获取数据源【最底层方法，不要随便调用】
     *
     * @param dbSource
     * @return
     */
    private static DruidDataSource getJdbcDataSource(final DynamicDataSourceModel dbSource) {
        DruidDataSource dataSource = new DruidDataSource();

        String driverClassName = dbSource.getDbDriver();
        String url = dbSource.getDbUrl();
        String dbUser = dbSource.getDbUsername();
        String dbPassword = dbSource.getDbPassword();
        dataSource.setDriverClassName(driverClassName);
        dataSource.setUrl(url);
        //dataSource.setValidationQuery("SELECT 1 FROM DUAL");
        dataSource.setTestWhileIdle(true);
        dataSource.setTestOnBorrow(false);
        dataSource.setTestOnReturn(false);
        dataSource.setBreakAfterAcquireFailure(true);
        dataSource.setConnectionErrorRetryAttempts(0);
        dataSource.setUsername(dbUser);
        dataSource.setMaxWait(30000);
        dataSource.setPassword(dbPassword);

        log.info("******************************************");
        log.info("*                                        *");
        log.info("*====【"+dbSource.getCode()+"】=====Druid连接池已启用 ====*");
        log.info("*                                        *");
        log.info("******************************************");
        return dataSource;
    }

    /**
     * 通过 dbKey ,获取数据源
     *
     * @param dbKey
     * @return
     */
    public static DruidDataSource getDbSourceByDbKey(final String dbKey) {
        //获取多数据源配置
        DynamicDataSourceModel dbSource = DataSourceCachePool.getCacheDynamicDataSourceModel(dbKey);
        //先判断缓存中是否存在数据库链接
        DruidDataSource cacheDbSource = DataSourceCachePool.getCacheBasicDataSource(dbKey);
        if (cacheDbSource != null && !cacheDbSource.isClosed()) {
            log.debug("--------getDbSourceBydbKey------------------从缓存中获取DB连接-------------------");
            return cacheDbSource;
        } else {
            DruidDataSource dataSource = getJdbcDataSource(dbSource);
            if(dataSource!=null && dataSource.isEnable()){
                DataSourceCachePool.putCacheBasicDataSource(dbKey, dataSource);
            }else{
                throw new JeecgBootException("动态数据源连接失败，dbKey："+dbKey);
            }
            log.info("--------getDbSourceBydbKey------------------创建DB数据库连接-------------------");
            return dataSource;
        }
    }

    /**
     * 关闭数据库连接池
     *
     * @param dbKey
     * @return
     */
    public static void closeDbKey(final String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        try {
            if (dataSource != null && !dataSource.isClosed()) {
                dataSource.getConnection().commit();
                dataSource.getConnection().close();
                dataSource.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private static JdbcTemplate getJdbcTemplate(String dbKey) {
        DruidDataSource dataSource = getDbSourceByDbKey(dbKey);
        return new JdbcTemplate(dataSource);
    }

    /**
     * Executes the SQL statement in this <code>PreparedStatement</code> object,
     * which must be an SQL Data Manipulation Language (DML) statement, such as <code>INSERT</code>, <code>UPDATE</code> or
     * <code>DELETE</code>; or an SQL statement that returns nothing,
     * such as a DDL statement.
     */
    public static int update(final String dbKey, String sql, Object... param) {
        int effectCount;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        if (ArrayUtils.isEmpty(param)) {
            effectCount = jdbcTemplate.update(sql);
        } else {
            effectCount = jdbcTemplate.update(sql, param);
        }
        return effectCount;
    }


    public static Object findOne(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        list = findList(dbKey, sql, param);
        if (oConvertUtils.listIsEmpty(list)) {
            log.error("Except one, but not find actually");
            return null;
        }
        if (list.size() > 1) {
            log.error("Except one, but more than one actually");
        }
        return list.get(0);
    }

    /**
     * 直接sql查询 根据clazz返回单个实例
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句
     * @param clazz 返回实例的Class
     * @param param
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <T> Object findOne(final String dbKey, String sql, Class<T> clazz, Object... param) {
        Map<String, Object> map = (Map<String, Object>) findOne(dbKey, sql, param);
        return ReflectHelper.setAll(clazz, map);
    }


    public static List<Map<String, Object>> findList(final String dbKey, String sql, Object... param) {
        List<Map<String, Object>> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql);
        } else {
            list = jdbcTemplate.queryForList(sql, param);
        }
        return list;
    }

    //此方法只能返回单列，不能返回实体类
    public static <T> List<T> findList(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<T> list;
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);

        if (ArrayUtils.isEmpty(param)) {
            list = jdbcTemplate.queryForList(sql, clazz);
        } else {
            list = jdbcTemplate.queryForList(sql, clazz, param);
        }
        return list;
    }

    /**
     * 直接sql查询 返回实体类列表
     *
     * @param dbKey 数据源标识
     * @param sql   执行sql语句，sql支持 minidao 语法逻辑
     * @param clazz 返回实体类列表的class
     * @param param sql拼接注入中需要的数据
     * @return
     */
    public static <T> List<T> findListEntities(final String dbKey, String sql, Class<T> clazz, Object... param) {
        List<Map<String, Object>> queryList = findList(dbKey, sql, param);
        return ReflectHelper.transList2Entrys(queryList, clazz);
    }


    public static Page page(final String dbKey, String sql, Page page,Class<T> clazz){
        JdbcTemplate jdbcTemplate = getJdbcTemplate(dbKey);
        String countSql="select count(*) ct from ("+sql+") ct_";
        long pages = 0;   //总页数
        Map<String,Object> ctMap = findList(dbKey, countSql).get(0);//查询总行数
        Long rows = (Long) ctMap.get("ct");
        //判断页数,如果是页大小的整数倍就为rows/pageRow如果不是整数倍就为rows/pageRow+1
        if (rows % page.getSize() == 0) {
            pages = rows / page.getSize();
        } else {
            pages = rows / page.getSize() + 1;
        }
        //查询第page页的数据sql语句
        if(page.getCurrent()<=1){
            sql+=" limit 0,"+page.getSize();
        }else{
            sql+=" limit "+((page.getCurrent()-1)*page.getSize())+","+page.getSize();
        }
        //查询第page页数据
        List list=null;
        if(clazz!=null){
            list=findListEntities(dbKey,sql, clazz);
        }else{
            list=findList(dbKey,sql);
        }

        //返回分页格式数据
        page.setTotal(rows);
        page.setRecords(list);
        IPage result = new Page();
        return page;
    }
}
