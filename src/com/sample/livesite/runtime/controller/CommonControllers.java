package com.sample.livesite.runtime.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Node;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;
import com.interwoven.livesite.runtime.model.page.RuntimePage;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Iterator;
import java.util.Enumeration;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


/**
 * Common controllers
 */
public class CommonControllers {
	/** logger */
	private static final transient Log LOGGER = LogFactory
			.getLog(CommonControllers.class);

	/** Used in testing between local, session and application scope */
	protected String mClassVariable = "initial";

	/**
	 * Controller that adds page scope data from wording Dcr.
	 *
	 * @param context
	 *            ReqauestContext
	 * @return null
	 */
	public ForwardAction setPageScopeData(RequestContext context) {

        HttpServletRequest request = context.getRequest();
        HttpServletResponse response = context.getResponse();
        
		//言語別Wording情報の読込
		String dcrPath = (String)context.getParameterString("worddcr");
		if(dcrPath == null || dcrPath.length() == 0) {
			LOGGER.info("[CommonControllers.setPageScopeData] dcrPath is null then return ");
			return null;
		}
		
		Document langData = com.interwoven.livesite.external.ExternalUtils.readXmlFile(context, dcrPath);
		LOGGER.debug("[CommonControllers.setPageScopeData] dcrPath : " + dcrPath);

		List wordList = langData.getRootElement().selectNodes("/WordingInfo/container");
		if (wordList.size() > 0) {
			Iterator it = wordList.iterator();
			while (it.hasNext()) {
				Node node = (Node) it.next();
				Node nName = node.selectSingleNode("name");
				Node nVal  = node.selectSingleNode("value");
				String name = nName.getText();	
				String key = name.split(" ")[0];
				context.getPageScopeData().put(key, nVal.getText());
				LOGGER.debug("[CommonControllers.setPageScopeData] Added:" + key +"-"+ nVal.getText());
			}
		}
		
		return null;
	}

	/**
	 * Controller that injects Analytics Script to page from analytics Dcr. 
	 *
	 * @param context
	 *            ReqauestContext
	 * @return null
	 */
	public ForwardAction injectAnalyticsScriptOnPage(RequestContext context){
		
		String dcrPath = (String)context.getParameterString("analyticsdcr");
		if(dcrPath == null || dcrPath.length() == 0) {
			LOGGER.info("[CommonControllers.injectAnalyticsScriptOnPage] dcrPath is null then return ");
			return null;
		}

		Document analyticsData = com.interwoven.livesite.external.ExternalUtils.readXmlFile(context, dcrPath);
		LOGGER.debug("[CommonControllers.injectAnalyticsScriptOnPage] dcrPath : " + dcrPath);
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Inside injectAnalyticsScriptOnPage");
		}
		if(context.isRuntime()){
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Setting page analytics only for runtime pages");
			}
			try{
				String endHeadSectionAnalytics = analyticsData.getRootElement().selectSingleNode("//endHeadSection").getText();
				String endBodySectionAnalytics = analyticsData.getRootElement().selectSingleNode("//endBodySection").getText();
				
				LOGGER.debug("[CommonControllers.injectAnalyticsScriptOnPage] endHeadSection : " + endHeadSectionAnalytics);
				LOGGER.debug("[CommonControllers.injectAnalyticsScriptOnPage] endBodySection : " + endBodySectionAnalytics);
				
				if(endHeadSectionAnalytics != ""){					
					setHeadBottomInjection(context, endHeadSectionAnalytics);
				}else{
					// TODO - To redirect to error page??
				}
				
				if(endBodySectionAnalytics != ""){					
					setBodyBottomInjection(context, endBodySectionAnalytics);
				}else{
					// TODO - To redirect to error page??
				}
						
			}catch(Exception analex){
				if(LOGGER.isErrorEnabled()){
					LOGGER.warn("[CommonControllers.injectAnalyticsScriptOnPage] Exception while injecting analytics "+analex.getMessage());
				}
			}
		}
		return null;
	}
	
	public void setBodyBottomInjection(RequestContext context, String endBodySectionAnalytics) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Inside setBodyBottomInjection");
		}		
		StringBuffer bodySB = new StringBuffer("");
		bodySB.append(endBodySectionAnalytics);
		bodySB.append("");
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change this to info later or remove it completely");
			LOGGER.debug("Final body analytics section is "+bodySB.toString());
		}
		context.getPageScopeData().put(RuntimePage.PAGESCOPE_BODY_INJECTION_BOTTOM, bodySB.toString());		
	}
	

	public void setHeadBottomInjection(RequestContext context, String endHeadSectionAnalytics) {		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Inside setHeadBottomInjection");
		}
		
		HashMap<String, String> globalJSMap = new HashMap<String, String>();
		globalJSMap.put("PROJECT_ID", context.getSite().getAnalyticsUrl());
		
		String userId = "";
	    BaseUserSession session = ((BaseUserSession) context.getSession());
        UserProfile profile = session.getUserProfile();
	    if(profile != null && session.isLoggedIn()) {
	    	userId = profile.getUserName();
	    }

		//Setting global javascript variables
		LOGGER.debug("userId=" + userId);		
		globalJSMap.put("USER_ID", userId);		
		Boolean userLoggedInState = userId == "" ? Boolean.FALSE : Boolean.TRUE;		
		globalJSMap.put("USER_LOGGED_IN_STATE", userLoggedInState.toString());
		
		globalJSMap.put("PAGE_DESCTIPTION", context.getPage().getBaseDescription());
		globalJSMap.put("PAGE_TITLE", context.getPageTitle());
		globalJSMap.put("PAGE_NAME", context.getPageName()+".page");
		
		StringBuffer headSB = new StringBuffer("");
		
		//Replace to variables
		headSB.append(endHeadSectionAnalytics.replaceAll("@SESSIONVARIABLES", convertGlobalJSMapToSB(globalJSMap).toString()));
		
		headSB.append("");		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Change this to info later or remove it completely");
			LOGGER.debug("Final head analytics section is "+headSB.toString());
		}
		context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION,headSB.toString());	
	}
	
	public static StringBuffer convertGlobalJSMapToSB(Map<String, String> globalJSMap) {
		StringBuffer globalJSSB = new StringBuffer();
	    Iterator<Entry<String, String>> it = globalJSMap.entrySet().iterator();
	    while (it.hasNext()) {
	        Map.Entry<String, String> pair = (Map.Entry<String, String>)it.next();
	        globalJSSB.append("").append(pair.getKey()).append("=").append("\"").append(pair.getValue()).append("\";");
	        globalJSSB.append(System.getProperty("line.separator"));
	        it.remove();
	    }
	    return globalJSSB;
	}
}
