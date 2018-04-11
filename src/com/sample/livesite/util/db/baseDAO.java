package com.sample.livesite.util.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO
 */
public class baseDAO {

    private Connection con = null;
    private ResultSet rs = null;
    private PreparedStatement ps = null;

    private static final transient Log LOGGER = LogFactory.getLog(baseDAO.class);

    private String dbname = "tsdb_custom";
    
    public baseDAO() {
    	
    }
    
    public baseDAO(String dbname) {
    	this.dbname = dbname;
    }

    protected int executeUpdate(String sql, List<String> params) {

    	int cnt = 0;
    	
        try {
            // データベースと接続（本来はユーザやパスワードも別管理にしておくのが理想）
            con = DBManager.getConnection(dbname);

            // SQL文を生成
            ps = con.prepareStatement(sql);

            // 生成したSQL文の「？」の部分にIDとパスワードをセット
            int i = 0;
            for(String param : params){
            	i++;
            	ps.setString(i, param);
            	
				LOGGER.debug("[baseDAO] parameter : " +  Integer.toString(i) + " - " + param);
            }
            
            // SQLを実行
        	LOGGER.debug("[baseDAO] execute updateSql : " + sql);
            cnt = ps.executeUpdate();
            
        } catch (SQLException sqle) {
        	LOGGER.error("[baseDAO] Error : " + sqle.getMessage() + "\n" + sqle.getStackTrace());
            sqle.printStackTrace();
            this.close();
        }
        return cnt;
    }

    protected ResultSet executeQuery(String sql, List<String> params) {
    	
        try {
            // データベースと接続（本来はユーザやパスワードも別管理にしておくのが理想）
            con = DBManager.getConnection(dbname);

            // SQL文を生成
            ps = con.prepareStatement(sql);

            // 生成したSQL文の「？」の部分にIDとパスワードをセット
            int i = 0;
            for(String param : params){
            	i++;
            	ps.setString(i, param);
            	
            	LOGGER.debug("[baseDAO] parameter : " +  Integer.toString(i) + " - " + param);
            }
            
            // SQLを実行
        	LOGGER.debug("[baseDAO] execute sql : " + sql);
        	
            rs = ps.executeQuery();
            
        } catch (SQLException sqle) {
        	LOGGER.error("[baseDAO] Error : " + sqle.getMessage() + "\n" + sqle.getStackTrace());
            sqle.printStackTrace();
            this.close();
        }
        return rs;
    }
  
    protected void close() {
        try {
            // データベースとの接続を解除する
            if (con != null) {
                con.close();
            }
            if (ps != null) {
                ps.close();
            }
            if (rs != null) {
                rs.close();
            }
        } catch (SQLException sqle) {
            // データベースとの接続解除に失敗した場合
            sqle.printStackTrace();
        }
    }
    
}
