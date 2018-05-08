package com.sample.livesite.util.mongo;

import java.io.Serializable;
import java.util.Date;

public class NewsBean implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String key;
	private String url;
	private String title;
	private String content;
    
    public NewsBean() {
    	key = "";
    	url = "";
    	title = "";
    	content = "";
    }

	public NewsBean(String key, String url, String title, String content) {
		super();
		this.key = key;
		this.url = url;
		this.title = title;
		this.content = content;
	}

	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}
    
}