package me.cqp.JRbot.Utils.misc;

import java.io.InputStream;
import java.sql.*;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

import static me.cqp.Jrbot.CQ;

public class dbutils {

    private static final Properties properties = new Properties();
    private static DataSource dataSource;

    //加载DBCP配置文件
    static {
        try {
            InputStream is = dbutils.class.getClassLoader().getResourceAsStream("db.properties");
            properties.load(is);
            dataSource = BasicDataSourceFactory.createDataSource(properties);
            System.out.println("Properties and dataSource loaded");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Properties getProperties() {
        return properties;
    }

    public static DataSource getDataSource() {
        return dataSource;
    }

    //从连接池中获取一个连接
    public static Connection getConnection() {
        Connection connection = null;
        try {
            connection = dataSource.getConnection();
            connection.setAutoCommit(false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public static void CloseConnection(Connection conn, PreparedStatement pst, Statement st, ResultSet rs) {
        try {
            // 关闭存储查询结果的ResultSet对象
            if (rs != null) {
                rs.close();
            }
            if (pst != null) {
                pst.close();
            }
            //关闭Statement对象
            if (st != null) {
                st.close();
            }
            //关闭连接
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }

        } catch (SQLException sqlex) {
            sqlex.printStackTrace();
        }

    }

    public static boolean testConnection() {
        CQ.logInfo("Database::ConnectionTest", "Testing database connection.....");
        try (Connection con = getConnection()) {
            CQ.logInfo("Database::Autocommit", String.valueOf(con.getAutoCommit()));
            CQ.logInfoSuccess("Database::ConnectionTest", "Database connection get.");
        } catch (SQLException sqlex) {
            CQ.logFatal("Database::ConnectionTest", "Failed to get database connection.Reason:" + sqlex.toString());
            return false;
        }
        return true;
    }

    public static boolean initBotDb() {
        // select @@basedir as basePath from dual
        // "basePath + /bin/mysqldump.exe" -u{username} -p{password} --opt --lock-tables=false jrbot > jrbot.sql
        return false;
    }


}
