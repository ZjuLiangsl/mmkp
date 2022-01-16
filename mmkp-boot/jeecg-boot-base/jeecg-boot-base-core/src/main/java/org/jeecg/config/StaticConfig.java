package org.jeecg.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 设置静态参数初始化
 */
@Component
@Data
public class StaticConfig {

    /**
     * 签名密钥串
     */
    @Value(value = "${jeecg.signatureSecret}")
    private String signatureSecret;

}