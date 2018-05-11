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
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.bson.Document;

import com.sample.livesite.util.AppConfig;
import com.sample.livesite.util.db.DBManager;
import com.sample.livesite.util.db.baseDAO;

public class baseMongoDAO {

	private static final transient Log LOGGER = LogFactory.getLog(baseMongoDAO.class);
	
	MongoClient mongoClient;
	MongoCredential credential;

	Block<Document> printBlock = new Block<Document>() {
	       @Override
	       public void apply(final Document document) {
	           System.out.println(document.toJson());
	       }
	};
	
	public baseMongoDAO() {
	    String host = AppConfig.getString("MONGO_HOST");
	    String port = AppConfig.getString("MONGO_PORT");
	    
	    String user = AppConfig.getString("MONGO_USER"); // the user name
	    String database = AppConfig.getString("MONGO_DB"); // the name of the database in which the user is defined
	    char[] password = AppConfig.getString("MONGO_PASS").toCharArray(); // the password as a character array

	    credential = MongoCredential.createCredential(user, database, password);
	    mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                .applyToClusterSettings(builder ->builder.hosts(Arrays.asList(new ServerAddress(host, Integer.parseInt(port)))))
                .credential(credential)
                .build());
    }

    protected MongoCursor find(String dbname, String collname) {
    	return this.find(dbname, collname, null);
    }
 

    protected MongoCursor find(String dbname, String collname, Map<String, String> key) {
    	
    	MongoCursor<Document> cur = null;
    	
        try {
        	
        	MongoDatabase db = mongoClient.getDatabase(dbname);
        	MongoCollection<Document> collection = db.getCollection(collname);
        	Document query = null;
        	
        	if(key != null && !key.isEmpty()) {
        		query = new Document();
        		
            	for(String tmpkey : key.keySet()) {
            		query.append(tmpkey, key.get(tmpkey));
            	}
        	}
        	
        	if(query != null && !query.isEmpty()) {
        		LOGGER.debug("[baseMongoDAO] Execute Find with query : " + query.toJson());
        		cur = collection.find(query).iterator();
        	}else {
        		LOGGER.debug("[baseMongoDAO] Execute Find without query");
        		cur = collection.find().iterator();
        	}
        	
//        	while(cur.hasNext()) {
//        		Document doc = (Document)cur.next();
//        		LOGGER.debug(doc.getString("title"));
//            }        	 
        	        	            
        } catch (Exception e) {
        	LOGGER.error("[baseMongoDAO] Error : " + e.getMessage() + "\n" + e.getStackTrace());
            e.printStackTrace();
        } finally {
        	mongoClient.close();
        }
        return cur;
    }

}
