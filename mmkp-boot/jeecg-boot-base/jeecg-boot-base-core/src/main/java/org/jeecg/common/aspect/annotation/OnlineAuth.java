package org.jeecg.common.aspect.annotation;

import java.lang.annotation.*;

/**
 * online
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE,ElementType.METHOD})
@Documented
public @interface OnlineAuth {

    /**
     *      ， xxx/code
     * @return
     */
    String value();
}
