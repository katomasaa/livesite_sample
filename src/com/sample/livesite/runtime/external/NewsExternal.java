package com.sample.livesite.runtime.external;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;
import com.sample.livesite.util.mongo.NewsBean;
import com.sample.livesite.util.mongo.NewsDAO;

public class  NewsExternal{
	
	  /** logger */
	  private static final transient Log LOGGER = LogFactory.getLog(NewsExternal.class);
	  
	  public Document getNewsList(RequestContext context) {
		  	BaseUserSession session = ((BaseUserSession) context.getSession());
		  	
			// Create an empty document
			Document doc = Dom4jUtils.newDocument();

			NewsDAO ndao = new NewsDAO();
			List<NewsBean> news = ndao.findAll();
			context.getRequest().setAttribute("NewsList", news);
			
			return doc;
	  }

	  public Document getNewsDetail(RequestContext context) {
		  	BaseUserSession session = ((BaseUserSession) context.getSession());
		  	
			// Create an empty document
			Document doc = Dom4jUtils.newDocument();
			
			String key = (String)context.getRequest().getAttribute("key");

			NewsDAO ndao = new NewsDAO();
			List<NewsBean> news = ndao.findByKey(key);
			context.getRequest().setAttribute("NewsList", news);

			return doc;
	  }

	  public Document searchNews(RequestContext context) {
		  	BaseUserSession session = ((BaseUserSession) context.getSession());
		  	
			// Create an empty document
			Document doc = Dom4jUtils.newDocument();

			String seachkey = (String)context.getRequest().getAttribute("searchkey");

			NewsDAO ndao = new NewsDAO();
			List<NewsBean> news = ndao.findByKey(seachkey);
			context.getRequest().setAttribute("NewsList", news);

			return doc;
	  }
}
