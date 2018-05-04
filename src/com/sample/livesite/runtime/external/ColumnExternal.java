package com.sample.livesite.runtime.external;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;
import com.interwoven.livesite.file.FileDal;

public class ColumnExternal {

	  /** logger */
	  private static final transient Log LOGGER = LogFactory.getLog(ColumnExternal.class);

	  public Document getColumnDetail(RequestContext context)
	  {
		  	BaseUserSession session = ((BaseUserSession) context.getSession());
		  	
			String msg = "";
			String id = context.getRequest().getParameter("id");

			// Create an empty document
			Document doc = Dom4jUtils.newDocument();
			Element root = doc.addElement("ExRoot");
			Element ecol = root.addElement("column");
			
			Document model = context.getThisComponentModel().getDocument();
			String dcrPath = "";
								
			org.dom4j.Node dcrNode = model.getRootElement().selectSingleNode("/Properties/Data/Datum[@ID='D01']/DCR");
			if(dcrNode != null){
				dcrPath = dcrNode.getText();
			}
			msg += "dcrpath:"+dcrPath + "\n";

			Document dcrData = com.interwoven.livesite.external.ExternalUtils.readXmlFile(context, dcrPath);
			List columns;
			
			if(id != null && id.length() > 0) {
				columns = dcrData.getRootElement().selectNodes("/Root/container/id[text()="+id+"]/parent::node()");
			} else {
				columns = dcrData.getRootElement().selectNodes("/Root/container[1]");
			}
				
			if (columns.size() > 0) {
				Iterator it = columns.iterator();
				while (it.hasNext()) {
					Node node = (Node) it.next();
					
					LOGGER.info("getColumnDetail " + node.getName());
					id = node.selectSingleNode("id").getText();

//					for(Node child : (List<Node>)node.selectNodes("./*")) {
//						LOGGER.info("getColumnDetail " + child.getName() +":"+child.getText());
//						String nodeName = child.getName();
//						ecol.addElement(nodeName).addCDATA(node.selectSingleNode(nodeName).getText());
//					}
				}
			}
							
			root.addElement("message").addCDATA(msg);
			
			String login = "0";
			if(session.isLoggedIn()) {
				login = "1";
			}
			
			root.addElement("isLogin").addCDATA(login);
			root.addElement("currentId").addCDATA(id);
			root.addElement("currentPage").addCDATA(context.getPageName());
			
//		    LivesiteSiteMap smap = new LivesiteSiteMap();
//		    
//		    Document sdoc = (Document)smap.getBreadCrumb(context).clone();
//		    root.addElement("breadcrumb").add(sdoc.getRootElement());
//		    
//		    LOGGER.debug(root.asXML());
			
			return doc;
	    }

	  public Document getSearchColumnId(RequestContext context)
	  {
		  	BaseUserSession session = ((BaseUserSession) context.getSession());
		  	
			String msg = "";

			// Create an empty document
			Document doc = Dom4jUtils.newDocument();
			Element root = doc.addElement("ExRoot");
			
			String key = (String)context.getRequest().getAttribute("searchkey");
			List<String> result = (List<String>)context.getRequest().getAttribute("searchresult"); 
			if(result != null) {
				   root.addElement("searchkey").addCDATA(key);
					Element sres = root.addElement("searchresult");
					
					if(result.size() > 0) {
						for(Object id : result) {
							LOGGER.debug("getColumnDetail ADD" + id.toString());
							sres.addElement("id").addCDATA(id.toString());						
						}
					}else {
						
						sres.addElement("noresult").addCDATA("検索結果：0件");						
					}
			 }
			 
			 LOGGER.debug(doc.asXML());
			return doc;
	    }
	  
}
