package com.shared.security.rls;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "shared-lib.rls")
public class RlsProperties {

    /**
     * Enable tenant-aware DataSource and filter.
     */
    private boolean enabled = false;

    /**
     * Header fallback for tenant id if not present in authentication.
     */
    private String headerName = "X-Tenant-Id";

    /**
     * Config key used in set_config.
     */
    private String configKey = "app.current_user_id";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getConfigKey() {
        return configKey;
    }

    public void setConfigKey(String configKey) {
        this.configKey = configKey;
    }
}
