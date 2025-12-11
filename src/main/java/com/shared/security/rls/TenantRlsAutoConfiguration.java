package com.shared.security.rls;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Auto-configures tenant-aware RLS context.
 */
@Configuration
@ConditionalOnClass({DataSource.class, SecurityFilterChain.class})
@EnableConfigurationProperties(RlsProperties.class)
@ConditionalOnProperty(prefix = "shared-lib.rls", name = "enabled", havingValue = "true")
public class TenantRlsAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(TenantRlsAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public TenantFilter tenantFilter(RlsProperties properties) {
        log.info("TenantRlsAutoConfiguration: registering TenantFilter");
        return new TenantFilter(properties.getHeaderName(), properties.getConfigKey());
    }

    @Bean
    @ConditionalOnMissingBean(name = "tenantAwareDataSourceWrapper")
    public BeanPostProcessor tenantAwareDataSourceWrapper(RlsProperties properties) {
        return new TenantAwareDataSourceWrapper(properties);
    }

    private static class TenantAwareDataSourceWrapper implements BeanPostProcessor {

        private final RlsProperties properties;

        TenantAwareDataSourceWrapper(RlsProperties properties) {
            this.properties = properties;
        }

        @Override
        public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
            if (bean instanceof DataSource && "dataSource".equals(beanName) && !(bean instanceof TenantAwareDataSource)) {
                log.info("TenantRlsAutoConfiguration: wrapping primary DataSource with TenantAwareDataSource");
                return new TenantAwareDataSource((DataSource) bean, properties.getConfigKey());
            }
            return bean;
        }
    }
}
