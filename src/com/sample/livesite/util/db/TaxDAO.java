package com.sample.livesite.util.db;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sample.livesite.util.AppConfig;

/**
 * DAO
 */
public class TaxDAO extends baseDAO{

    private static final transient Log LOGGER = LogFactory.getLog(TaxDAO.class);

    public TaxDAO() {
    	super(AppConfig.getString("DB_NAME_LSDS"));
    }
    
	public String GetTaxIdbyName(String tax, String name) {
    	
		ResultSet rs = null;
		
		if(name.equals("*")) {
			return name;
		}
		
		String sql = "";
    	sql += "select a.node_id ";
    	sql += "from iwtax_node a ";
    	sql += "inner join iwtax_node_type b ";
    	sql += "on a.node_type_id=b.nodetype_id ";
    	sql += "where nodetype_name=? ";
    	sql += "and node_name=?;";

    	List<String> params = new ArrayList<String>();

    	params.add(tax);
    	params.add(name);
    	
    	rs = this.executeQuery(sql, params);

    	String id = "";
    	try {
    		if(rs.next()) {
    			id = rs.getString("node_id");
    		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("[TaxDAO] Error : " + e.getMessage() + "\n" + e.getStackTrace());
		}
		return id;
		
	}
    
}
