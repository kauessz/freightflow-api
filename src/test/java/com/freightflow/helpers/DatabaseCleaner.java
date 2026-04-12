package com.freightflow.helpers;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Limpa as tabelas do banco de dados entre testes de integração.
 * A ordem respeita as foreign keys (filhos antes dos pais).
 * Usa SET session_replication_role = replica para desabilitar FKs
 * temporariamente, permitindo TRUNCATE em qualquer ordem.
 */
@Component
public class DatabaseCleaner {

    private final JdbcTemplate jdbcTemplate;

    // Ordem inversa de dependência — filhos antes dos pais
    private static final String[] TABLES = {
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
    @Transactional
    public void clean() {
        // Desabilita constraint checks temporariamente (PostgreSQL-only)
        jdbcTemplate.execute("SET session_replication_role = replica");
        try {
            for (String table : TABLES) {
                jdbcTemplate.execute("TRUNCATE TABLE " + table + " CASCADE");
            }
        } finally {
            jdbcTemplate.execute("SET session_replication_role = DEFAULT");
        }
    }
}
