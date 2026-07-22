package com.freightflow.helpers;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Limpa as tabelas do banco de dados entre testes de integração.
 * Usa um único TRUNCATE ... CASCADE em uma conexão física única para evitar
 * que exceções intermediárias deixem a sessão em estado abortado.
 */
@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    // Tabelas de domínio explícitas para garantir limpeza após novas migrations.
    private static final String[] TABLES = {
        "commercial_quotation_items",
        "commercial_quotations",
        "commercial_rfq_containers",
        "commercial_rfq_cargo_items",
        "commercial_rfqs",
        "documents",
        "alerts",
        "events",
        "shipments",
        "webhook_subscriptions",
        "api_keys",
        "users",
        "customers",
        "voyages",
        "vessels",
        "ports",
        "tenants"
    };

    public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Trunca todas as tabelas de domínio.
     * Chamado pelo @BeforeEach do AbstractIntegrationTest.
     */
    public void clean() {
        String truncateSql = "TRUNCATE TABLE "
                + String.join(", ", TABLES)
                + " RESTART IDENTITY CASCADE";

        jdbcTemplate.execute((Connection connection) -> {
            boolean originalAutoCommit = connection.getAutoCommit();

            try {
                if (originalAutoCommit) {
                    connection.setAutoCommit(false);
                }

                try (Statement statement = connection.createStatement()) {
                    statement.execute(truncateSql);
                }

                connection.commit();
                return null;
            } catch (SQLException ex) {
                rollbackQuietly(connection, ex);
                throw new DataAccessException("Failed to clean integration-test database", ex) {
                };
            } finally {
                restoreAutoCommit(connection, originalAutoCommit);
            }
        });
    }

    private void rollbackQuietly(Connection connection, SQLException originalException) throws SQLException {
        try {
            connection.rollback();
        } catch (SQLException rollbackException) {
            originalException.addSuppressed(rollbackException);
            throw originalException;
        }
    }

    private void restoreAutoCommit(Connection connection, boolean originalAutoCommit) {
        try {
            if (connection.getAutoCommit() != originalAutoCommit) {
                connection.setAutoCommit(originalAutoCommit);
            }
        } catch (SQLException ex) {
            throw new DataAccessException("Failed to restore JDBC auto-commit after test database cleanup", ex) {
            };
        }
    }
}
