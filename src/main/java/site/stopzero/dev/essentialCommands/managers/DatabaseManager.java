package site.stopzero.dev.essentialCommands.managers;

import org.bukkit.configuration.ConfigurationSection;
import site.stopzero.dev.essentialCommands.EssentialCommands;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class DatabaseManager {

    private final EssentialCommands plugin;
    private Connection connection;
    public final String storageType;

    public DatabaseManager(EssentialCommands plugin) {
        this.plugin = plugin;
        this.storageType = plugin.getConfig()
                .getString("database.storage-type", "sqlite").toLowerCase();
    }

    public Connection getConnection() throws SQLException {
        if (connection != null && !connection.isClosed()) {
            return connection;
        }

        if (storageType.equals("mysql")) {
            ConfigurationSection mysqlConfig
                    = plugin.getConfig().getConfigurationSection("database.mysql");

            if (mysqlConfig == null) {
                throw new SQLException("MySQL 설정이 config.yml에 존재하지 않습니다.");
            }

            String host = mysqlConfig.getString("host");
            int port = mysqlConfig.getInt("port");
            String db = mysqlConfig.getString("database");
            String user = mysqlConfig.getString("username");
            String pass = mysqlConfig.getString("password");
            boolean useSSL = mysqlConfig.getBoolean("useSSL", false);

            String jdbcUrl = "jdbc:mysql://" + host + ":" + port + "/" + db + "?useSSL=" + useSSL;

            try {
                Class.forName("com.mysql.cj.jdbc.Driver");

                connection = DriverManager.getConnection(jdbcUrl, user, pass);
                plugin.getLogger().info("MySQL 데이터베이스에 성공적으로 연결되었습니다.");

            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "MySQL JDBC 드라이버를 찾을 수 없습니다! 플러그인에 포함되었는지 확인하세요.");

                throw new SQLException("MySQL 드라이버 로드 실패", e);
            }

        } else {
            File dataFolder = new File(plugin.getDataFolder(), "database.db");

            if (!dataFolder.exists()) {
                try {
                    dataFolder.createNewFile();
                } catch (IOException e) {
                    plugin.getLogger().log(Level.SEVERE,
                            "데이터베이스 파일을 생성할 수 없습니다!", e);
                }
            }

            try {
                Class.forName("org.sqlite.JDBC");

                connection = DriverManager.getConnection("jdbc:sqlite:" + dataFolder);
                plugin.getLogger().info("SQLite 데이터베이스에 성공적으로 연결되었습니다.");

            } catch (ClassNotFoundException e) {
                plugin.getLogger().log(Level.SEVERE,
                        "SQLite JDBC 드라이버를 찾을 수 없습니다! 플러그인에 포함되었는지 확인하세요.");
                throw new SQLException("SQLite 드라이버 로드 실패", e);
            }
        }
        return connection;
    }

    public void initializeDatabase() {
        String sql = "CREATE TABLE IF NOT EXISTS mutes (" +
                "uuid VARCHAR(36) PRIMARY KEY NOT NULL," +
                "source VARCHAR(255) NOT NULL," +
                "reason TEXT NOT NULL," +
                "expirationTime BIGINT NOT NULL);";

        try (Connection conn = getConnection(); Statement statement = conn.createStatement()) {
            statement.execute(sql);
            plugin.getLogger().info("Mutes 테이블이 성공적으로 초기화 되었습니다.");

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "데이터베이스 초기화에 실패했습니다!", e);
        }
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "데이터베이스 연결 해제에 실패했습니다!", e);
        }
    }


}