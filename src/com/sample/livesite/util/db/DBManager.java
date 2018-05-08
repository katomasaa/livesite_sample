package com.sample.livesite.util.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sample.livesite.util.AppConfig;

public class DBManager {
    private static String driverName = "com.mysql.jdbc.Driver";
    private static String url = AppConfig.getString("DB_URL");
    private static String user = AppConfig.getString("DB_USER");
    private static String pass = AppConfig.getString("DB_PASS");

    private static final transient Log LOGGER = LogFactory.getLog(DBManager.class);
    
    public static Connection getConnection(String dbname) {
        Connection con = null;
        try {
            Class.forName(driverName);
            con = DriverManager.getConnection(url + dbname,user,pass);
        } catch (ClassNotFoundException e) {
        	LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());
            e.printStackTrace();
        } catch (SQLException e) {
        	LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());
            e.printStackTrace();
        }
        return con;
    }
}