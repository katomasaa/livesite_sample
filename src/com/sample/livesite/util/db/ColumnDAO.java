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
public class ColumnDAO extends baseDAO{

    private static final transient Log LOGGER = LogFactory.getLog(TaxDAO.class);

    public ColumnDAO() {
    	super();
    }
    
	public List<String> GetColumnIdByFulltext(String searchtext, String flag) {
    	
		ResultSet rs = null;
		
		String sql = "";
    	sql += "select a.id";
    	sql += " from columns a ";
    	sql += " where match(a.column_contents) against(? in NATURAL LANGUAGE MODE)";

    	List<String> params = new ArrayList<String>();
    	List<String> result = new ArrayList<String>();

    	params.add(searchtext);
    	
    	rs = this.executeQuery(sql, params);

    	try {
    		while (rs.next()) {
    			LOGGER.debug("[ColumnDAO] id :" + rs.getString("id"));
    		    result.add(rs.getString("id"));
    		}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.error("[ColumnDAO] Error : " + e.getMessage() + "\n" + e.getStackTrace());
		}
		return result;
		
	}
    
}
