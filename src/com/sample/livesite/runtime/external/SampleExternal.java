/**
 * Copyright 2009 Autonomy Corp. All rights reserved. Other trademarks
 * are registered trademarks and the properties of their respective owners.
 * Product specifications and features are subject to change without notice.
 * Use of Autonomy software is under license.
 *
 * If this product is acquired under the terms of a DoD contract: Use,
 * duplication, or disclosure by the Government is subject to restrictions
 * as set forth in subparagraph (c)(1)(ii) of 252.227-7013. Civilian agency
 * contract: Use, reproduction or disclosure is subject to 52.227-19
 * (a) through (d) and restrictions set forth in the accompanying end
 * user agreement. Unpublished-rights reserved under the copyright laws
 * of the United States. Autonomy, Inc., One Market Plaza, Spear Tower,
 * Suite 1900, San Francisco, CA. 94105, US.
 */

package com.sample.livesite.runtime.external;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Attribute;
import org.dom4j.Node;
import org.dom4j.QName;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.file.FileDal;
import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.runtime.impl.BaseRequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.p13n.LoginUtils;
import com.sample.livesite.util.api.LscsClient;

/**
 * Sample externals
 *
 * @author $Author: jchang $
 * @version $Revision: #2 $
 */
public class SampleExternal
{
  /** logger */
  private static final transient Log LOGGER = LogFactory.getLog(SampleExternal.class);

  public Document execFirstExternal(RequestContext context) {
	    
	Document doc = Dom4jUtils.newDocument();
	    
	doc.addElement("name").addCDATA("hello world from Java");
	return doc;
  }

  public Document execExternal(RequestContext context) {
    
    HttpServletRequest request = context.getRequest();
    HttpServletResponse response = context.getResponse();

    Document doc = Dom4jUtils.newDocument();
    Element root = doc.addElement("ExRoot");

    String logMsg = "";
    String login = "0";
    String name = "GUEST USER";
    String name2 = "";
    UserProfile profile = null;

    try {
      BaseUserSession session = ((BaseUserSession) context.getSession());
      profile = session.getUserProfile();
      if(profile != null && session.isLoggedIn()) {
        name = profile.getUserName();
        name2 = profile.getFullName();
        
        login = "1";
        LOGGER.debug("segment : " + profile.getSegmentName());

      }else{
        name = request.getParameter("param.name");
        
        if(name.trim().length() > 0){
          name = "GUEST USER2";
        }
      }
      LOGGER.info("user is " + name);
      
    } catch (Exception e) {
      root.addElement("msg1").addCDATA("ERROR!!:" + e.getMessage());
    }
    
    root.addElement("name").addCDATA(name);
    root.addElement("name2").addCDATA(name2);
    root.addElement("islogin").addCDATA(login);
    
    LivesiteSiteMap smap = new LivesiteSiteMap();
    
    Document sdoc = (Document)smap.getSiteMap(context).clone();
    sdoc.setName("sitemap");
    
    root.addElement("sitemapdata").add(sdoc.getRootElement());
    
    Iterator itSegment = profile.getSegments().iterator();
    while(itSegment.hasNext())
    {
	    // Check if they have a redirect for this segment
	    Segment segment = (Segment)itSegment.next();
	    LOGGER.debug("Segment : " + segment.getName());
    }
    LOGGER.debug("project : " + context.getSite().getBranch());

    return doc;
  }

  public Document showForm(RequestContext context) {
	    
	    HttpServletRequest request = context.getRequest();
	    HttpServletResponse response = context.getResponse();

	    Document doc = Dom4jUtils.newDocument();
	    Element root = doc.addElement("ExRoot");

	    String fname = ""; 
	    String lname = "";
	    String usrid = "";
	    String passwd = "";
	    String message = "";
	    String inputDiv = "";
	    String login = "0";

	    BaseUserSession session = ((BaseUserSession) context.getSession());
        UserProfile profile = session.getUserProfile();
	    if(profile != null && session.isLoggedIn()) {
	    	fname = profile.getFirstName();
	    	lname = profile.getLastName();
	    	usrid = profile.getUserName();
	    	passwd = profile.getPassword();
	    	login = "1";
	    }
	    
	    if(context.getRequest().getAttribute("inputing") != null) {
	      fname = (String)context.getRequest().getAttribute("input.FirstName"); 
	      lname = (String)context.getRequest().getAttribute("input.LastName");
	      usrid = (String)context.getRequest().getAttribute("input.UserId");
	      passwd = (String)context.getRequest().getAttribute("input.Password");
	      inputDiv = (String)context.getRequest().getAttribute("inputing");
	    }
	    
	    if(context.getRequest().getAttribute("completed") != null) {
	    	inputDiv = "2";
	    	context.getRequest().removeAttribute("completed");
		}
	    
	    message = (String)context.getRequest().getAttribute("message");
	    
	    root.addElement("InputDiv").addCDATA(inputDiv);
	    
	    if(message != null) {
	      root.addElement("Message").addCDATA(message);
	      context.getRequest().removeAttribute("message");
	    }
	    
	    Map<String, String> errData = (Map<String, String>)context.getRequest().getAttribute("ValidationError");
	    if(errData != null) {
	    	
	    	Element validresult = root.addElement("ValidationResult");
	    	
	    	for(String key : errData.keySet()) {
	    		Element err = validresult.addElement("ValidationError");
	    	    err.addElement("Field").addCDATA(key);
	    	    err.addElement("Message").addCDATA((String)errData.get(key));
	    	}
	    }
	    
	    root.addElement("FirstName").addCDATA(fname);
	    root.addElement("LastName").addCDATA(lname);
	    root.addElement("Email").addCDATA(usrid);
	    root.addElement("Password").addCDATA(passwd);
	    
	    root.addElement("islogin").addCDATA(login);
	    
	    return doc;
	  }

  
  public Document getFileList(RequestContext context)
  {
	  final String meta_key = "ast_path";
	  final String meta_value = "download/*";
	  
	  String proj = context.getSite().getBranch();
	  
	  LscsClient lscs = new LscsClient(proj);
	  
	  Map<String, String> params = new HashMap<String, String>();
	  params.put(meta_key, meta_value);
	  	  
	  Document doc = null;
	  
	  try {
		  doc = lscs.GetMetaByMeta(params);
	  }catch(Exception ex) {
		  LOGGER.error("[SampleExternal] " + ex.getMessage());
	  }
	  
	  if(doc == null) {
		  doc = Dom4jUtils.newDocument();
		  doc.addElement("results");
	  }
	  LOGGER.debug(doc.asXML());
      return doc;
  }

  public Document getContents(RequestContext context)
  {
	  final String cat_name = "Content Categories";
	  final String type_name = "Content Types";
	  
	  String proj = context.getSite().getBranch();
	  String cat = (String)context.getRequest().getParameter("ContentCategory");
	  String type = (String)context.getRequest().getParameter("ContentType");
	  LOGGER.debug("Params 1: " + cat + " ; " + type);
	  
	  if(cat == null && type == null) {
	  	cat = (String)context.getRequest().getAttribute("ContentCategory");
	  	type = (String)context.getRequest().getAttribute("ContentType");
	  }
	  
      LOGGER.debug("Params 2: " + cat + " ; " + type);
	  if(cat == null && type == null) {
		  	type = "*";
	  }
      LOGGER.debug("Params 3: " + cat + " ; " + type);

	  
      LscsClient lscs = new LscsClient(proj);
	  
	  Map<String, String> params = new HashMap<String, String>();
	  
	  if(cat != null && cat.length() > 0) {
		  params.put("TeamSite/Metadata/ContentCategories", lscs.GetTaxIdbyName(cat_name, cat));
	  }
	  
	  if(type != null && type.length() > 0) {
		  params.put("TeamSite/Metadata/ContentType", lscs.GetTaxIdbyName(type_name, type));
	  }
	  
	  Document doc = null;
	  
	  try {
		  doc = lscs.GetMetaByMeta(params);
	  }catch(Exception ex) {
		  LOGGER.error("[SampleExternal] " + ex.getMessage());
	  }
	  
//	  LOGGER.debug("Attribute Remove Start");
//	  Element root = doc.getRootElement();
//
//	  root.attributes().clear();
//	  while(root.attributeIterator().hasNext()) {
//		  Attribute att = (Attribute)root.attributeIterator().next();
//		  LOGGER.debug("Attribute Remove : " + att.getName());
//		  root.remove(att);
//	  }
//	  for(Namespace nms : (List<Namespace>)root.declaredNamespaces()) {
//		  LOGGER.debug("Namespace Remove : " + nms.getName());
//		  root.remove(nms);
//	  }
//	  for(Namespace nms : (List<Namespace>)root.additionalNamespaces()) {
//		  LOGGER.debug("Namespace Remove : " + nms.getName());
//		  root.remove(nms);
//	  }
//	  root.remove(root.getNamespace());
//	  LOGGER.debug("Attribute Remove End");
//
//	  
//	  Document ret = Dom4jUtils.newDocument();
//	  Element results = ret.addElement("results");
//	  if(root.elementIterator().hasNext()) {
//		  Element ele = (Element)root.elementIterator().next();
//		  LOGGER.debug("Element Copy : " + ele.getName());
//		  results.add((Element)ele.clone());
//	  }

	  if(doc == null) {
		  doc = Dom4jUtils.newDocument();
		  doc.addElement("results");
	  }
	  LOGGER.debug(doc.asXML());
      return doc;
//	  LOGGER.debug(ret.asXML());
//    return ret;
  }
  
  /**
   * external to get page scope data
   *
   * @param context ReqauestContext
   * @return document
   */
  public Document getPageScopeData(RequestContext context)
  {
     String str = (String) context.getPageScopeData().get("test.data");

     if(str == null)
     {
       str = "null...";
     }

     Document doc = Dom4jUtils.newDocument();
     doc.addElement("message").addCDATA(str);

     return doc;
  }

  /**
   * external to get attribute value
   *
   * @param context ReqauestContext
   * @return document
   */
  public Document getAttribute(RequestContext context)
  {
     String str = (String) context.getRequest().getAttribute("test.data");
     if(str == null)
     {
       str = "null...";
     }

     Document doc = Dom4jUtils.newDocument();
     doc.addElement("message").addCDATA(str);

     return doc;
  }

  /**
   * external to get attribute value
   *
   * @param context ReqauestContext
   * @return document
   */
  public Document getSegment(RequestContext context)
  {
     Document doc = Dom4jUtils.newDocument();

     doc.addElement("Segment").addCDATA(context.getSession().getUserProfile().getSegmentName());

     return doc;
  }

  /**
   * Do nothing but will allow Datums defined in Data section to become part of the parameters
   *
   * @param context RequestConext
   * @return null always
   */
  public Document nop(RequestContext context)
  {
    return null;
  }
}

