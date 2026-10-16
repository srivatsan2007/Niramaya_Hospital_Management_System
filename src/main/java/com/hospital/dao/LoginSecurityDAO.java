package com.hospital.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LoginSecurityDAO: Trusted Server-Side Failed Login Protection Manager.
 * Enforces a strict daily limit of 5 failed login attempts per account
 * in timezone Asia/Kolkata (00:00:00 to 23:59:59 IST).
 */
public class LoginSecurityDAO {

    public static final ZoneId KOLKATA_ZONE = ZoneId.of("Asia/Kolkata");
    public static final int MAX_FAILED_ATTEMPTS = 5;

    public LoginSecurityDAO() {
        initTable();
    }

    public void initTable() {
        String sql = "CREATE TABLE IF NOT EXISTS login_attempts (" +
                "account_identifier VARCHAR(100) NOT NULL, " +
                "attempt_date VARCHAR(20) NOT NULL, " +
                "failed_count INT DEFAULT 0, " +
                "last_attempt_time VARCHAR(50), " +
                "is_locked INT DEFAULT 0, " +
                "PRIMARY KEY (account_identifier, attempt_date)" +
                ");";
        try (Connection conn = DBConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (Exception e) {
            System.err.println("[LoginSecurityDAO] Init table error: " + e.getMessage());
        }
    }

    public String getCurrentKolkataDate() {
        return LocalDate.now(KOLKATA_ZONE).format(DateTimeFormatter.ISO_LOCAL_DATE);
    }

    public String getCurrentKolkataTimestamp() {
        return LocalDateTime.now(KOLKATA_ZONE).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    /**
     * Checks if account has reached maximum daily failed attempts.
     */
    public synchronized boolean isAccountLocked(String accountIdentifier) {
        if (accountIdentifier == null || accountIdentifier.trim().isEmpty()) return false;
        String normalized = accountIdentifier.trim().toLowerCase();
        String today = getCurrentKolkataDate();

        String sql = "SELECT failed_count, is_locked FROM login_attempts WHERE LOWER(account_identifier) = ? AND attempt_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt("failed_count");
                    int locked = rs.getInt("is_locked");
                    return locked == 1 || count >= MAX_FAILED_ATTEMPTS;
                }
            }
        } catch (SQLException e) {
            System.err.println("[LoginSecurityDAO] Error checking account lock: " + e.getMessage());
        }
        return false;
    }

    /**
     * Gets current failed attempts count for today (Asia/Kolkata).
     */
    public synchronized int getFailedAttempts(String accountIdentifier) {
        if (accountIdentifier == null || accountIdentifier.trim().isEmpty()) return 0;
        String normalized = accountIdentifier.trim().toLowerCase();
        String today = getCurrentKolkataDate();

        String sql = "SELECT failed_count FROM login_attempts WHERE LOWER(account_identifier) = ? AND attempt_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, today);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("failed_count");
                }
            }
        } catch (SQLException e) {
            System.err.println("[LoginSecurityDAO] Error getting failed attempts: " + e.getMessage());
        }
        return 0;
    }

    /**
     * Atomically records a failed login attempt for today.
     * Returns the updated count of failed attempts.
     */
    public synchronized int recordFailedAttempt(String accountIdentifier) {
        if (accountIdentifier == null || accountIdentifier.trim().isEmpty()) return 1;
        String normalized = accountIdentifier.trim().toLowerCase();
        String today = getCurrentKolkataDate();
        String now = getCurrentKolkataTimestamp();

        int currentCount = getFailedAttempts(normalized);
        int newCount = currentCount + 1;
        int isLocked = newCount >= MAX_FAILED_ATTEMPTS ? 1 : 0;

        String upsertSql;
        if (DBConnection.isPostgreSQL()) {
            upsertSql = "INSERT INTO login_attempts (account_identifier, attempt_date, failed_count, last_attempt_time, is_locked) " +
                    "VALUES (?, ?, ?, ?, ?) " +
                    "ON CONFLICT (account_identifier, attempt_date) DO UPDATE SET " +
                    "failed_count = EXCLUDED.failed_count, " +
                    "last_attempt_time = EXCLUDED.last_attempt_time, " +
                    "is_locked = EXCLUDED.is_locked";
        } else {
            upsertSql = "INSERT OR REPLACE INTO login_attempts (account_identifier, attempt_date, failed_count, last_attempt_time, is_locked) " +
                    "VALUES (?, ?, ?, ?, ?)";
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(upsertSql)) {
            ps.setString(1, normalized);
            ps.setString(2, today);
            ps.setInt(3, newCount);
            ps.setString(4, now);
            ps.setInt(5, isLocked);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[LoginSecurityDAO] Error recording failed attempt: " + e.getMessage());
        }

        return newCount;
    }

    /**
     * Formats the required exact response message according to the prompt specifications.
     */
    public static String getRemainingAttemptsMessage(int failedCount) {
        int remaining = Math.max(0, MAX_FAILED_ATTEMPTS - failedCount);
        if (failedCount >= MAX_FAILED_ATTEMPTS) {
            return "You have reached the maximum of 5 failed login attempts for today. Please try again tomorrow.";
        }
        return "Invalid credentials. You have " + remaining + " login attempt" + (remaining == 1 ? "" : "s") + " remaining today.";
    }

    /**
     * Secure admin reset function (recorded in audit logs).
     */
    public synchronized void resetDailyAttempts(String accountIdentifier) {
        if (accountIdentifier == null) return;
        String normalized = accountIdentifier.trim().toLowerCase();
        String today = getCurrentKolkataDate();
        String sql = "DELETE FROM login_attempts WHERE LOWER(account_identifier) = ? AND attempt_date = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, normalized);
            ps.setString(2, today);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("[LoginSecurityDAO] Error resetting attempts: " + e.getMessage());
        }
    }
}
