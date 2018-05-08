package com.sample.livesite.util.api;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;

import com.sample.livesite.util.db.TaxDAO;
import com.sample.livesite.util.AppConfig;

public class LscsClient extends RESTClient{

	private static final transient Log LOGGER = LogFactory.getLog(RESTClient.class);
	
	private String project = "";
	
	private final String lscsPath = AppConfig.getString("LSCS_PATH");
	private final String lscsPathForMeta = lscsPath + "$";
	
	public LscsClient(String project) {
		super("http", "localhost", "1876");
		this.project = project;
	}
	
	public LscsClient(String protocol, String host, String port, String project) {
		super(protocol, host, port);
		this.project = project;
	}	

	public Document GetDocsByMeta(Map<String, String> metas) {		
	    return this.GetDocsByMeta(metas, 0 ,100);		
	}
	
	public Document GetDocsByMeta(Map<String, String> metas, int start, int max) {
		return this.GetDocOrMeta(metas, start, max, lscsPath, false);
	}

	public Document GetMetaByMeta(Map<String, String> metas) {
	    return this.GetMetaByMeta(metas, 0 ,100);		
	}
	
	public Document GetMetaByMeta(Map<String, String> metas, int start, int max) {
		return this.GetDocOrMeta(metas, start, max, lscsPathForMeta, false);
	}

	public String GetDocsByMetaJson(Map<String, String> metas) {		
	    return this.GetDocsByMetaJson(metas, 0 ,100);		
	}
	
	public String GetDocsByMetaJson(Map<String, String> metas, int start, int max) {
		return this.GetDocOrMetaString(metas, start, max, lscsPath, true);
	}

	public String GetMetaByMetaJson(Map<String, String> metas) {
	    return this.GetMetaByMetaJson(metas, 0 ,100);		
	}
	
	public String GetMetaByMetaJson(Map<String, String> metas, int start, int max) {
		return this.GetDocOrMetaString(metas, start, max, lscsPathForMeta, true);
	}
	
	private Document GetDocOrMeta(Map<String, String> metas, int start, int max, String usePath, boolean json){
		
		String ret = this.GetDocOrMetaString(metas, start, max, usePath, json);
	    return this.strToDoc(ret);				
	}
	
	private String GetDocOrMetaString(Map<String, String> metas, int start, int max, String usePath, boolean json){
		
	    String path = usePath;
	    String metaParams = "";
	    
    	for(String meta : metas.keySet()) {
    		if(metaParams.length() > 0 ) {
    			metaParams += " AND ";
    		}
    		LOGGER.debug("[LscsUtil] meta : " + meta + " - " + metas.get(meta));
    		metaParams += meta + ":" + metas.get(meta);
    	}		
	    	    
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("q", metaParams);
	    params.put("project", project);
	    params.put("start", String.valueOf(start));
	    params.put("max", String.valueOf(max));
	    
	    if(json) {
	    	params.put("format", "json");
	    }
	    
	    String ret = this.get(path, params);
	    
	    return ret;				
	}
	
	public InputStream GetDocsByPath(String filePath) {
		
	    if(filePath.startsWith("/")) {
	    	filePath = filePath.substring(1);
	    }
	    String path = lscsPath + "/path/" + filePath;
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("project", project);
	    
	    return this.getInputStream(path, params);
	}
	
	public InputStream GetDocsById(String fileId) {
		
	    String path = lscsPath + "/id/" + fileId;
	    Map<String, String> params = new HashMap<String, String>();
	    params.put("project", project);
	    
	    return this.getInputStream(path, params);
	}

	public String GetTaxIdbyName(String tax, String name) {
		
		TaxDAO dao = new TaxDAO();
	    return dao.GetTaxIdbyName(tax, name);	
	}

	private Document strToDoc(String strDoc) {
		Document doc = null;
		
		try {
        	doc =  DocumentHelper.parseText(strDoc);
		}catch (DocumentException e) {
			LOGGER.debug("[LscsUtil] ERROR! : " + e.getMessage() + "\n" + e.getStackTrace());
		}
		return doc;
		
	}
	
}
