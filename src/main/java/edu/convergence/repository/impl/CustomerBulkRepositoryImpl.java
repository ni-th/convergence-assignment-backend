package edu.convergence.repository.impl;

import edu.convergence.dto.customer.CustomerBulkDTO;
import edu.convergence.repository.CustomerBulkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
@Repository
@RequiredArgsConstructor
public class CustomerBulkRepositoryImpl implements CustomerBulkRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String UPSERT_SQL =
            "INSERT INTO customer (nic, name, date_of_birth) " +
                    "VALUES (?, ?, ?) " +
                    "ON DUPLICATE KEY UPDATE " +
                    "name = VALUES(name), " +
                    "date_of_birth = VALUES(date_of_birth)";
    @Override
    public void upsertBatch(List<CustomerBulkDTO> customers) {

        jdbcTemplate.batchUpdate(UPSERT_SQL, new BatchPreparedStatementSetter() {

            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {

                CustomerBulkDTO c = customers.get(i);

                ps.setString(1, c.getNic());
                ps.setString(2, c.getName());
                ps.setDate(3, java.sql.Date.valueOf(c.getDateOfBirth()));
            }

            @Override
            public int getBatchSize() {
                return customers.size();
            }
        });

    }
}
