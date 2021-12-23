package com.asl.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @author Zubayer Ahamed
 *
 * @since Dec 8, 2020
 */
@Profile("!api")
@Configuration
@EnableScheduling
@ConditionalOnProperty(name = "scheduling.enabled" ,matchIfMissing = true)
public class SchedulingConfiguration {

}
