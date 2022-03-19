package org.jeecg.common.aspect.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 *     :
 *       ： dangzhenghui
 *       ： 2019 03 17 -  9:37:16
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Dict {
    /**
     *     :    code
     *       ： dangzhenghui
     *       ： 2019 03 17 -  9:37:16
     *
     * @return     ： String
     */
    String dicCode();

    /**
     *     :    Text
     *       ： dangzhenghui
     *       ： 2019 03 17 -  9:37:16
     *
     * @return     ： String
     */
    String dicText() default "";

    /**
     *     :
     *       ： dangzhenghui
     *       ： 2019 03 17 -  9:37:16
     *
     * @return     ： String
     */
    String dictTable() default "";
}
