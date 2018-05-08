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
import java.util.List;

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

	public NewsDAO() {
		super();
	}

	public List<NewsBean> findAll(String dbname, String collname) {
		
		MongoCursor cur = super.find(dbname, collname);
		List<NewsBean> result = new ArrayList<NewsBean>();
		
		try {
			while (cur.hasNext()) {
				Document doc = (Document)cur.next();
			    LOGGER.debug("[NewsDAO] id :" + (String)doc.getString("title"));
			    
			    String key = (String)doc.getString("key");
			    String url = (String)doc.getString("url");
			    String title = (String)doc.getString("title");
			    String content = (String)doc.getString("content");
	
			    result.add(new NewsBean(key, url, title, content));
			}
		}catch(Exception e){
    		//log
    		LOGGER.error("Error : " + e.getMessage() + "\n" + e.getStackTrace());
		}

		return result;
	}
	
}
