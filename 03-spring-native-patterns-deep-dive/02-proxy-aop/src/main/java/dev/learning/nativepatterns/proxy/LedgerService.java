package dev.learning.nativepatterns.proxy;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LedgerService {
    private final JdbcTemplate jdbc;

    public LedgerService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Transactional
    public void recordThenFail(String reference) {
        jdbc.update("insert into ledger_entry(reference) values (?)", reference);
        throw new IllegalStateException("simulated failure");
    }

    public int countEntries() {
        return jdbc.queryForObject("select count(*) from ledger_entry", Integer.class);
    }

    public void deleteAll() {
        jdbc.update("delete from ledger_entry");
    }
}
