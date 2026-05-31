package jjfw;

import jjfw.common.Config;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Sequential migration runner.
 *
 * Scans src/main/java/database/ for *.sql files sorted alphabetically,
 * skips files already recorded in schema_migrations, then applies the
 * remaining files one by one inside individual transactions.
 *
 * Connection credentials
 * ──────────────────────
 * Migrations require DDL privileges (CREATE TABLE, ALTER TABLE, GRANT, …).
 * The app role (db_user) is intentionally limited to DML only.
 * Add optional keys to your config file to supply admin credentials:
 *
 *   "db_migrate_user": "postgres"
 *   "db_migrate_pass": "yourpassword"
 *
 * If those keys are absent, the tool falls back to db_user / db_pass —
 * useful in CI/CD where a single role owns both DDL and DML.
 */
public class DatabaseMigrator {

    private static final String MIGRATIONS_DIR = "src/main/java/database";

    private static Connection openConnection() throws SQLException {
        String host = Config.get("db_host");
        int    port = Config.getNum("db_port");
        String name = Config.get("db_name");
        String user = Config.getOrDefault("db_migrate_user", Config.get("db_user"));
        String pass = Config.getOrDefault("db_migrate_pass", Config.get("db_pass"));

        String url = "jdbc:postgresql://" + host + ":" + port + "/" + name;
        return DriverManager.getConnection(url, user, pass);
    }

    /** Run all pending migrations. Called by Migrate.main(). */
    public static void createTables() throws SQLException, IOException {
        try (Connection conn = openConnection()) {
            ensureMigrationsTable(conn);

            List<Path> files = discoverMigrations();
            System.out.println("Found " + files.size() + " migration file(s).");

            for (Path file : files) {
                String filename = file.getFileName().toString();
                if (isApplied(conn, filename)) {
                    System.out.println("  [skip] " + filename);
                    continue;
                }

                System.out.println("  [apply] " + filename + " …");
                applyMigration(conn, file, filename);
                System.out.println("  [done]  " + filename);
            }
        }
    }

    /** Drop all tables and truncate migration history (for dev resets). */
    public static void purgeTables() throws SQLException, IOException {
        String purgeFile = "jjfw/backend/jjdb/src/main/java/queries/purge.sql";
        try (Connection conn = openConnection()) {
            String sql = Files.readString(Paths.get(purgeFile));
            conn.createStatement().execute(sql);
            System.out.println("Purge complete.");
        }
    }

    // ── private helpers ────────────────────────────────────────────

    private static void ensureMigrationsTable(Connection conn) throws SQLException {
        conn.createStatement().execute(
            "CREATE TABLE IF NOT EXISTS schema_migrations (" +
            "  filename   VARCHAR(255) PRIMARY KEY," +
            "  applied_at TIMESTAMPTZ NOT NULL DEFAULT now()" +
            ")"
        );
    }

    private static List<Path> discoverMigrations() throws IOException {
        Path dir = Paths.get(MIGRATIONS_DIR);
        if (!Files.isDirectory(dir)) {
            throw new IOException("Migration directory not found: " + dir.toAbsolutePath());
        }
        try (Stream<Path> stream = Files.list(dir)) {
            return stream
                .filter(p -> p.toString().endsWith(".sql"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .collect(Collectors.toList());
        }
    }

    private static boolean isApplied(Connection conn, String filename) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM schema_migrations WHERE filename = ?")) {
            ps.setString(1, filename);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private static void applyMigration(Connection conn, Path file, String filename)
            throws SQLException, IOException {
        String sql = Files.readString(file);
        boolean autoCommit = conn.getAutoCommit();
        conn.setAutoCommit(false);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO schema_migrations (filename) VALUES (?)")) {
                ps.setString(1, filename);
                ps.execute();
            }
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new SQLException("Migration '" + filename + "' failed: " + e.getMessage(), e);
        } finally {
            conn.setAutoCommit(autoCommit);
        }
    }
}

