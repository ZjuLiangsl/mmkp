package org.jeecg.common.modules.redis.listener;

import org.jeecg.common.base.BaseMap;

/**
 */
public interface JeecgRedisListerer {

    void onMessage(BaseMap message);

}
