package org.jeecg.common.util.dynamic.db;

import com.alibaba.druid.pool.DruidDataSource;
import org.jeecg.common.api.CommonAPI;
import org.jeecg.common.constant.CacheConstant;
import org.jeecg.common.system.vo.DynamicDataSourceModel;
import org.jeecg.common.util.RedisUtil;
import org.jeecg.common.util.SpringContextUtils;
import org.springframework.data.redis.core.RedisTemplate;
import java.util.HashMap;
import java.util.Map;


/**
 *
 */
public class DataSourceCachePool {

    private static Map<String, DruidDataSource> dbSources = new HashMap<>();
    private static RedisTemplate<String, Object> redisTemplate;

    private static RedisTemplate<String, Object> getRedisTemplate() {
        if (redisTemplate == null) {
            redisTemplate = (RedisTemplate<String, Object>) SpringContextUtils.getBean("redisTemplate");
        }
        return redisTemplate;
    }

    /**
     *
     *
     * @param dbKey
     * @return
     */
    public static DynamicDataSourceModel getCacheDynamicDataSourceModel(String dbKey) {
        String redisCacheKey = CacheConstant.SYS_DYNAMICDB_CACHE + dbKey;
        if (getRedisTemplate().hasKey(redisCacheKey)) {
            return (DynamicDataSourceModel) getRedisTemplate().opsForValue().get(redisCacheKey);
        }
        CommonAPI commonAPI = SpringContextUtils.getBean(CommonAPI.class);
        DynamicDataSourceModel dbSource = commonAPI.getDynamicDbSourceByCode(dbKey);
        if (dbSource != null) {
            getRedisTemplate().opsForValue().set(redisCacheKey, dbSource);
        }
        return dbSource;
    }

    public static DruidDataSource getCacheBasicDataSource(String dbKey) {
        return dbSources.get(dbKey);
    }

    /**
     * put
     *
     * @param dbKey
     * @param db
     */
    public static void putCacheBasicDataSource(String dbKey, DruidDataSource db) {
        dbSources.put(dbKey, db);
    }

    /**
     *
     */
    public static void cleanAllCache() {
        for(Map.Entry<String, DruidDataSource> entry : dbSources.entrySet()){
            String dbkey = entry.getKey();
            DruidDataSource druidDataSource = entry.getValue();
            if(druidDataSource!=null && druidDataSource.isEnable()){
                druidDataSource.close();
            }
            getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + dbkey);
        }
        dbSources.clear();
    }

    public static void removeCache(String dbKey) {
        DruidDataSource druidDataSource = dbSources.get(dbKey);
        if(druidDataSource!=null && druidDataSource.isEnable()){
            druidDataSource.close();
        }
        getRedisTemplate().delete(CacheConstant.SYS_DYNAMICDB_CACHE + dbKey);
        dbSources.remove(dbKey);
    }

}
