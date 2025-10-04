package jjfw;

import jjfw.common.Config;
import org.postgresql.jdbc2.optional.PoolingDataSource;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseMigrator {

    private static Connection connection = null;
    private static PoolingDataSource pool = null;

    public static Connection getConnectionFromPool() throws SQLException {
        if (pool == null) {
            pool = new PoolingDataSource();
            pool.setServerName(Config.get("db_host"));
            pool.setDatabaseName(Config.get("db_name"));
            pool.setUser(Config.get("db_user"));
            pool.setPassword(Config.get("db_pass"));
            pool.setMaxConnections(10);
        }
        try {
            return pool.getConnection();
        } catch (SQLException e) {
            throw e;
        }
    }

    public static void init() throws SQLException, IOException, InterruptedException {
        getConnectionFromPool().createStatement().execute(sqlToString(
                "src/main/java/database/0.0.1.sql"));
    }

    public static String sqlToString(String path) {
        String query = "";
        try {
            BufferedReader in = new BufferedReader(new FileReader(path));
            String str;
            StringBuffer sb = new StringBuffer();
            while ((str = in.readLine()) != null) {
                sb.append(str + "\n ");
            }
            in.close();
            query = sb.toString();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return query;
    }

}
