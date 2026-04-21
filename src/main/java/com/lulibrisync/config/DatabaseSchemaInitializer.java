package com.lulibrisync.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

@Component
public class DatabaseSchemaInitializer implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(DatabaseSchemaInitializer.class);

    private final DataSource dataSource;

    public DatabaseSchemaInitializer(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            ensurePreferredPickupDateColumn(statement);
        }
    }

    private void ensurePreferredPickupDateColumn(Statement statement) throws Exception {
        try (ResultSet tables = statement.executeQuery("SHOW TABLES LIKE 'reservations'")) {
            if (!tables.next()) {
                return;
            }
        }

        try (ResultSet columns = statement.executeQuery("SHOW COLUMNS FROM reservations LIKE 'preferred_pickup_date'")) {
            if (columns.next()) {
                return;
            }
        }

        statement.executeUpdate("ALTER TABLE reservations ADD COLUMN preferred_pickup_date DATE NULL AFTER expires_at");
        logger.info("Added reservations.preferred_pickup_date column for scheduled pickup support.");
    }
}
