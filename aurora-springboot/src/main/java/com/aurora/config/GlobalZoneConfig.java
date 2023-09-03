package com.aurora.config;

import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.util.TimeZone;

import static com.aurora.enums.ZoneEnum.SHANGHAI;

/**
 * 全局时区配置类
 */
@Configuration
public class GlobalZoneConfig {

    @PostConstruct
    public void setGlobalZone() {
        TimeZone.setDefault(TimeZone.getTimeZone(SHANGHAI.getZone()));
    }

}
