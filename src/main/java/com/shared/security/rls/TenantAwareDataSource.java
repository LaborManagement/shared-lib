package com.shared.security.rls;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.DelegatingDataSource;

/**
 * Wraps a DataSource and applies set_config on each acquired connection.
 */
public class TenantAwareDataSource extends DelegatingDataSource {

    private static final Logger log = LoggerFactory.getLogger(TenantAwareDataSource.class);

    private final String configKey;

    public TenantAwareDataSource(DataSource targetDataSource, String configKey) {
        super(targetDataSource);
        this.configKey = configKey;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        applyTenant(connection);
        return connection;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        applyTenant(connection);
        return connection;
    }

    private void applyTenant(Connection connection) throws SQLException {
        String tenantId = TenantContext.getTenantId();
        if (tenantId == null || tenantId.isBlank()) {
            log.debug("TenantAwareDataSource: no tenant id set; skipping set_config");
            return;
        }
        String sql = "SELECT set_config(?, ?, false)";
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            ps.setString(1, configKey);
            ps.setString(2, tenantId);
            ps.execute();
            log.debug("TenantAwareDataSource applied tenant id {} to key {}", tenantId, configKey);
        }
    }
}
