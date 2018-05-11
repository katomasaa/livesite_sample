package com.sample.livesite.util.mongo;

import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.MongoClient;
import com.mongodb.MongoClientSettings;
import com.mongodb.Block;
import com.mongodb.ConnectionString;
import com.mongodb.ServerAddress;
import com.mongodb.MongoCredential;
import com.mongodb.MongoClientOptions;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

import com.sample.livesite.util.AppConfig;
import com.sample.livesite.util.db.DBManager;
import com.sample.livesite.util.db.TaxDAO;
import com.sample.livesite.util.db.UsersBean;
import com.sample.livesite.util.db.baseDAO;

public class NewsDAO extends baseMongoDAO{

	private static final transient Log LOGGER = LogFactory.getLog(NewsDAO.class);

	private String dbname;
	private String collname;
	
	public NewsDAO() {		
		super();
		
		dbname = "custom";
		collname = "ot_news";
	}

	public List<NewsBean> findAll() {
		
		MongoCursor cur = super.find(dbname, collname);
		List<NewsBean> result = null;
		
		try {
			result = this.setBean(cur);
		}catch(Exception e){
    		//log
    		LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());
		}

		return result;
	}
	
	public List<NewsBean> findByKey(String key) {
		
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("key", key);

		MongoCursor cur = super.find(dbname, collname, params);
		List<NewsBean> result = null;
		
		try {
			result = this.setBean(cur);
		}catch(Exception e){
    		//log
    		LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());
		}

		return result;
	}
	
	private List<NewsBean> setBean(MongoCursor cur){

		List<NewsBean> result = new ArrayList<NewsBean>();
		
		while (cur.hasNext()) {
			Document doc = (Document)cur.next();
		    LOGGER.debug("[NewsDAO] key :" + (String)doc.getString("key"));
		    LOGGER.debug("[NewsDAO] url :" + (String)doc.getString("url"));
		    
		    String key = (String)doc.getString("key");
		    String url = (String)doc.getString("url");
		    String title = (String)doc.getString("title");
		    
		    List<String> tmp = (ArrayList)doc.get("content");
		    String content = tmp.get(0);
	
		    result.add(new NewsBean(key, url, title, content));
		}
		
		return result;
	}
}
