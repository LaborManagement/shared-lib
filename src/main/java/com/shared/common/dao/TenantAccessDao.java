package com.shared.common.dao;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * DAO to fetch tenant access tuples for the current user using
 * auth.user_accessible_tenants().
 */
@Component
public class TenantAccessDao {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public static class TenantAccess {
        public Integer boardId;
        public Integer employerId;
        public Integer toliId;
        public boolean canRead;
        public boolean canWrite;
    }

    /**
     * Returns all accessible tenant tuples for the current user (RLS context must
     * be set).
     */
    public List<TenantAccess> getAccessibleTenants() {
        String sql = "SELECT * FROM auth.user_accessible_tenants()";
        return jdbcTemplate.query(sql, (ResultSet rs) -> {
            List<TenantAccess> result = new ArrayList<>();
            while (rs.next()) {
                TenantAccess ta = new TenantAccess();
                ta.boardId = toInteger(rs.getObject("board_id"));
                ta.employerId = toInteger(rs.getObject("employer_id"));
                ta.toliId = toInteger(rs.getObject("toli_id"));
                ta.canRead = rs.getBoolean("can_read");
                ta.canWrite = rs.getBoolean("can_write");
                result.add(ta);
            }
            return result;
        });
    }

    // Helper to safely convert Long/Integer/null to Integer
    private Integer toInteger(Object value) {
        if (value == null)
            return null;
        if (value instanceof Integer)
            return (Integer) value;
        if (value instanceof Long)
            return ((Long) value).intValue();
        throw new IllegalArgumentException("Unexpected type for tenant id: " + value.getClass());
    }

    /**
     * Returns the first accessible tenant tuple for the current user (for
     * single-tenant ops).
     */
    public TenantAccess getFirstAccessibleTenant() {
        List<TenantAccess> all = getAccessibleTenants();
        return all.isEmpty() ? null : all.get(0);
    }
}
