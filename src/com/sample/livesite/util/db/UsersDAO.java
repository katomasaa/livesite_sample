package com.sample.livesite.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * DAO
 */
public class UsersDAO extends baseDAO{

    private ResultSet rs = null;

    private static final transient Log LOGGER = LogFactory.getLog(UsersDAO.class);

    public UsersDAO() {
    	super();
    }
    
    public List<UsersBean> select (){
    	String sql = "select * from users;";
    	List<String> params = new ArrayList<String>();
    	
    	rs = this.executeQuery(sql, params);
    	    	
    	return this.toBean(rs);
    }

    public List<UsersBean> select(String email) {
    	String sql = "select * from users where email = ?;";
    	List<String> params = new ArrayList<String>();

    	params.add(email);
    	rs = this.executeQuery(sql, params);

    	return this.toBean(rs);
    }

    public List<UsersBean> select(String email, String password) {
    	String sql = "select * from users where email = ? and password = ?;";

    	List<String> params = new ArrayList<String>();

    	params.add(email);
    	params.add(password);
    	rs = this.executeQuery(sql, params);

    	return this.toBean(rs);
    }

    public int insert(UsersBean bean) {
    	String sql = "insert users(email,password,first_name,last_name) values(?, ?, ?, ?);";

    	List<String> params = new ArrayList<String>();

    	params.add(bean.getEmail());
    	params.add(bean.getPassword());
    	params.add(bean.getFirstName());
    	params.add(bean.getLastName());
    	
    	return this.executeUpdate(sql, params);
    }
    
    public int update(UsersBean bean) {
    	String sql = "update users set password=?, first_name=?, last_name=? where email=?;";

    	List<String> params = new ArrayList<String>();

    	params.add(bean.getPassword());
    	params.add(bean.getFirstName());
    	params.add(bean.getLastName());
    	params.add(bean.getEmail());
    	
    	return this.executeUpdate(sql, params);

    }

    
    private List<UsersBean> toBean(ResultSet rs){

    	UsersBean bean;
    	List<UsersBean> ret = new ArrayList<UsersBean>();
    	
    	try {
	        while (rs.next()) {
	            bean = new UsersBean();
	
	            bean.setEmail(rs.getString("email")); 
	            bean.setPassword(rs.getString("password")); 
	            bean.setFirstName(rs.getString("first_name")); 
	            bean.setLastName(rs.getString("last_name")); 
	            bean.setBirthDate(rs.getDate("birth_date"));
	            bean.setSegment(rs.getString("segment"));
	            
	            ret.add(bean);
	        }
        
    	}catch(SQLException e){
    		//log
    		LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());

    	}finally {
    		this.close();
    	}
    	
        return ret;

    }
    
}
