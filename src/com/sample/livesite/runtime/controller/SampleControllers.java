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

package com.sample.livesite.runtime.controller;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.forward.target.FullUrlTarget;
import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.common.web.forward.target.ContextRelativeTarget;
import com.interwoven.livesite.common.web.Http302ForwardAction;
import com.interwoven.livesite.common.web.RequestDispatcherForwardAction;
import com.interwoven.livesite.common.web.StopForwardAction;
import com.interwoven.livesite.common.web.URLRedirectForwardAction;
import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.p13n.LoginUtils;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;
import com.interwoven.livesite.runtime.model.page.RuntimePage;
import com.sample.livesite.util.api.LscsClient;
import com.sample.livesite.util.db.*;

/**
 * Sample controllers
 */
public class SampleControllers
{
  /** RNG */
  protected Random mRNG = new Random(System.currentTimeMillis());

  /** logger */
  private static final transient Log LOGGER = LogFactory.getLog(SampleControllers.class);

  /** Used in testing between local, session and application scope */
  protected String mClassVariable = "initial";

  public ForwardAction doSubmit(RequestContext context)
  {
	  BaseUserSession session = ((BaseUserSession) context.getSession());
      ForwardAction fa = null;
      
      String fname = context.getRequest().getParameter("FirstName");
      String lname = context.getRequest().getParameter("LastName");
      String usrid = context.getRequest().getParameter("Email");
      String passwd = context.getRequest().getParameter("Password");
      String inputDiv = "0";
      
      if(fname.length() > 0 && lname.length() > 0 && usrid.length() > 0 && passwd.length() > 0 ) {
    	  inputDiv = "1";
      }else {
    	  Map<String, String> validerr = new HashMap<String, String>();
    	  
    	  if(usrid.length() == 0) {
    		  validerr.put("UserId", "UserId is required.");
    	  }
    	  if(passwd.length() == 0) {
    		  validerr.put("Password", "Password is required.");
    	  }
    	  if(fname.length() == 0) {
    		  validerr.put("FirstName", "FirstName is required.");
    	  }
    	  if(lname.length() == 0) {
    		  validerr.put("LastName", "LastName is required.");
    	  }
    	  
    	  context.getRequest().setAttribute("ValidationError", validerr);
    	  
    	  String message = "入力エラーがあります。";
    	  context.getRequest().setAttribute("message", message);
      }
      
      context.getRequest().setAttribute("inputing",inputDiv);
      context.getRequest().setAttribute("input.FirstName", fname); 
      context.getRequest().setAttribute("input.LastName", lname);
      context.getRequest().setAttribute("input.UserId", usrid);
      context.getRequest().setAttribute("input.Password", passwd);
      
	  return fa;
  }

  public ForwardAction doRegist(RequestContext context)
  {
	  BaseUserSession session = ((BaseUserSession) context.getSession());
      ForwardAction fa = null;
      
      String fname = context.getRequest().getParameter("FirstName");
      String lname = context.getRequest().getParameter("LastName");
      String usrid = context.getRequest().getParameter("Email");
      String passwd = context.getRequest().getParameter("Password");
      String message = "完了しました。";

      UserProfile profile = session.getUserProfile();
      
      UsersBean bn = new UsersBean();
      UsersDAO dao = new UsersDAO();
      int result = 0;
      
      if(session.isLoggedIn()) {
	      bn.setEmail(profile.getUserName());
	      bn.setPassword(passwd);
	      bn.setFirstName(fname);
	      bn.setLastName(lname);
	      
	      result = dao.update(bn);
      }else {
    	  bn.setEmail(usrid);
    	  bn.setPassword(passwd);
	      bn.setFirstName(fname);
	      bn.setLastName(lname);
	      
	      result = dao.insert(bn);
      }
      
      if(result == 1) {
	      profile.setFirstName(fname);
	      profile.setLastName(lname);
	
	      context.getRequest().removeAttribute("inputing");
	      context.getRequest().removeAttribute("input.FirstName");
	      context.getRequest().removeAttribute("input.LastName");
	      context.getRequest().removeAttribute("input.UserId");
	      context.getRequest().removeAttribute("input.Password");
	
	      context.getRequest().setAttribute("completed", "1");

      }else {
    	  message = "残念ながらエラーです。";
      }
      
      context.getRequest().setAttribute("message", message);
	  
	  return fa;
  }

  public ForwardAction doBack(RequestContext context)
  {
      ForwardAction fa = null;
      
      String fname = context.getRequest().getParameter("FirstName");
      String lname = context.getRequest().getParameter("LastName");
      String usrid = context.getRequest().getParameter("Email");
      String passwd = context.getRequest().getParameter("Password");

      context.getRequest().setAttribute("inputing","0");
      context.getRequest().setAttribute("input.FirstName", fname); 
      context.getRequest().setAttribute("input.LastName", lname);
      context.getRequest().setAttribute("input.UserId", usrid);
      context.getRequest().setAttribute("input.Password", passwd);
      
      return fa;
  }

  public ForwardAction doDownload(RequestContext context)
  {
	    HttpServletRequest request = context.getRequest();
	    HttpServletResponse response = context.getResponse();

	    BaseUserSession session = ((BaseUserSession) context.getSession());
	    UserProfile profile = session.getUserProfile();
        ForwardAction fa = null;
        String content = profile.getFirstName() + "," + profile.getLastName() + "," + profile.getUserName();
      
        try {
	        response.setContentType("text/html");
	        response.setHeader("Content-Disposition","attachment");
	        response.getWriter().write(content);
	        
	        String encodedFilename = URLEncoder.encode("userdata.csv", "UTF-8");
	        response.setHeader("Content-Disposition","attachment;filename*=\"UTF-8''" + encodedFilename + "\"");
	        
//	        response.getWriter().flush();
//	        response.getWriter().close();

        }catch(Exception e) {
        	LOGGER.error("doDownload:" + e.getMessage() +"\n" + e.getStackTrace());
        }

        return new StopForwardAction();
  }

  public ForwardAction doRedirect(RequestContext context)
  {
	  BaseUserSession session = ((BaseUserSession) context.getSession());
      ForwardAction fa = null;
      
      String contName = context.getRequest().getParameter("iwPreActions");
      String url = context.getRequest().getParameter("url");
      String id = context.getRequest().getParameter("id");
      
	  LOGGER.warn("Executing redirectToUrl : " + contName);
      
	  url += "id=" + id;
	  context.setRedirectUrl(url);
	  
      if(contName.equals("NewRegist")) {
  	    FullUrlTarget fullurl = new FullUrlTarget(context.getPageLink("new"));
  	    fa = new Http302ForwardAction(fullurl);
      }else if(contName.equals("Read")) {
	    FullUrlTarget fullurl = new FullUrlTarget(context.getPageLink(context.getSite().getLoginPage()));
	    fa = new Http302ForwardAction(fullurl);  
      }else {
    	fa = null;
      }
      
	  
	  return fa;
  }
  
  public ForwardAction getImgPath(RequestContext context) {
      HttpServletRequest request = context.getRequest();
	  HttpServletResponse response = context.getResponse();

	  BaseUserSession session = ((BaseUserSession) context.getSession());
	  UserProfile profile = session.getUserProfile();
      
	  ForwardAction fa = null;
      String content = "/HP_logo_630x630.png";
      
      if(session.isLoggedIn()) {
    	  content = "/Autonomy_Product_Stack.png";
      }
    
      String key = request.getParameter("key");
      String value = request.getParameter("value");
      
      if(key != null && key.length() > 0 &&
          value != null && value.length() > 0) {
    	  String proj = context.getSite().getBranch();
    	  LscsClient lscs = new LscsClient(proj);

    	  Map<String, String> params = new HashMap<String, String>();

    	  LOGGER.error("[SampleControllers] " + key + " - " + value);
    	  params.put("TeamSite/Metadata/" + key, value);

    	  String ret = null;

    	  try {
    	  	  ret = lscs.GetMetaByMetaJson(params);
    	  	  content = ret;
    	  }catch(Exception ex) {
    	  	  LOGGER.error("[SampleControllers] " + ex.getMessage());
    	  }
    	  
    	  content = content.replaceAll("TeamSite/Metadata/", "TeamSite_Metadata_");
//    	  content = content.replaceAll("[\\\\]/", "/");
    	  
//    	  LOGGER.debug("debug xml : " + doc.asXML());
//    	  try {
//    		  doc.getRootElement().addNamespace("iwrr", "http://www.interwoven.com/schema/iwrr");
//	    	  content = "/" + doc.getRootElement().selectSingleNode("/iwrr:results/iwrr:assets/iwrr:document/@path").getText();
//    	  }catch(Exception ex) {
//    	  	  LOGGER.error("[SampleControllers] " + ex.getMessage());
//    	  }
      
    	  LOGGER.info("[SampleControllers] Return content : " + content);
      }
      
      try {
	        response.setContentType("application/json");
	        response.getWriter().write(content);
	        
      }catch(Exception e) {
      	LOGGER.error("doDownload:" + e.getMessage() +"\n" + e.getStackTrace());
      }

      return new StopForwardAction();
	  
  }

  public ForwardAction getFileContents(RequestContext context) {
      HttpServletRequest request = context.getRequest();
	  HttpServletResponse response = context.getResponse();

	  BaseUserSession session = ((BaseUserSession) context.getSession());
	  UserProfile profile = session.getUserProfile();

	  String key = request.getParameter("fileid");
	  String filename = request.getParameter("filename");
	  
	  LOGGER.debug("debug key : " + key);
	  LOGGER.debug("debug filename : " + filename);
	  
	  String proj = context.getSite().getBranch();
	  
	  LscsClient lscs = new LscsClient(proj);
	 
	  Pattern p = Pattern.compile("^(.*)/(.+)$");
	  Matcher m = p.matcher(filename);
	  if (m.find()){
	    String matchstr = m.group();
	    
	    filename = m.group(2);
	  }
	  filename = filename.replaceAll(" ", "_");
	  LOGGER.debug("debug filename : " + filename);
	  
	  OutputStream out = null;
	  InputStream in = null;
	 
	  try {
		  in = lscs.GetDocsById(key);

	      response.setContentType("text/html");
          String encodedFilename = URLEncoder.encode(filename, "UTF-8");
	      response.setHeader("Content-Disposition","attachment;filename*=\"UTF-8''" + encodedFilename + "\"");

          out = response.getOutputStream();
	     
	      byte[] buff = new byte[1024];
	      int len = 0;
	      while ((len = in.read(buff, 0, buff.length)) != -1) {
	        out.write(buff, 0, len);
	      }
	  }catch(Exception ex) {
		  LOGGER.error("[SampleExternal] " + ex.getMessage());
		  FullUrlTarget fullurl = new FullUrlTarget(context.getPageLink(context.getSite().getErrorPage()));
		  return new Http302ForwardAction(fullurl);  
	  }finally {
	      if (in != null) {
	          try {
	              in.close();
	          } catch (IOException e) {
	          }
	      }
	      if (out != null) {
	          try {
	              out.close();
	          } catch (IOException e) {
	          }
	      }
	  }
	  
	  return new StopForwardAction();
  }
  
  public ForwardAction checkLogin(RequestContext context)
  {
	  BaseUserSession session = ((BaseUserSession) context.getSession());
      ForwardAction fa = null;
      
      if(session.isLoggedIn()) {
          LOGGER.warn("Executing checkLogin Login");
     	  fa = null;
    	  context.getPageScopeData().put("sample.userid", session.getUserProfile().getUserName());
      }else {
    	  LOGGER.warn("Executing checkLogin NOT Login");
    	  
    	  if(context.isRuntime()) {
    	    String url = context.getSite().getLoginPage();
    	    context.setRedirectUrl(url);
    	    //ContextRelativeTarget tar = new ContextRelativeTarget(url);
    	    FullUrlTarget fullurl = new FullUrlTarget(context.getPageLink(url));
    	    fa = new Http302ForwardAction(fullurl);
    	  }
      }
	  
	  return fa;
  }

  public ForwardAction forwardToLogin(RequestContext context)
  {
	  BaseUserSession session = ((BaseUserSession) context.getSession());
      ForwardAction fa;
      
   	  LOGGER.warn("Executing forwardToLogin");
   	  String url = context.getSite().getLoginPage();
   	  //context.setRedirectUrl(null);
   	  LOGGER.warn("Redirect to : " + url);
   	  FullUrlTarget fullurl = new FullUrlTarget(context.getPageLink(url));
   	  fa = new Http302ForwardAction(fullurl);
	  
	  return fa;
  }

  /**
   * Demonstrate usage of Scope attribute
   *
   * local will create this class every time and value echoed will always be 'initial'
   * session will be 'initial' first time and then 'set' for the given user (until session cookie is cleared)
   * application will be 'initial' first time and then always 'set' until server reset
   *
   * @param context RequestContext
   * @return null always
   */
  public ForwardAction echoClassVariable(RequestContext context)
  {
    LOGGER.warn("Class Variable is currently: '"+mClassVariable+"' setting to 'set'.  Local should revert to 'initial', session should remain 'set' for user and application will remain 'set' from now on for all users.");

    mClassVariable = "set";

    return null;
  }

  /**
   * Controller that dumps context to logger
   *
   * @param context RequestContext
   * @return redirect
   */
  public ForwardAction dumpContext(RequestContext context)
  {
    //a_Log as error so that it shows up all the time since this is a sample that is intended to dump context
    LOGGER.error("Executing SampleControllers.dumpContext");
    LOGGER.error(context.toElement().asXML());
    return null;
  }

  /**
   * Controller that adds page scope data
   *
   * @param context ReqauestContext
   * @return null
   */
  public ForwardAction setPageScopeData(RequestContext context)
  {
    context.getPageScopeData().put("test.data", "hello!");
    context.getPageScopeData().put("username", "hello!");

    return null;
  }

  /**
   * Controller that adds an attribute
   *
   * @param context ReqauestContext
   * @return null
   */
  public ForwardAction setAttribute(RequestContext context)
  {
    context.getRequest().setAttribute("test.data", "hello!");

    return null;
  }

  /**
   * Controller that adds javascript to the head portion of a page
   *
   * @param context ReqauestContext
   * @return null
   */
  public ForwardAction insertJavascriptIntoHead(RequestContext context)
  {
    Object obj = context.getPageScopeData().get(RuntimePage.PAGESCOPE_HEAD_INJECTION);
    if (null != obj)
    {
      String str = (String)obj;  // Assuming a String is in here already
      context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION, str + "\r\n<script language='javascript' type='text/javascript'><!--\r\n alert('Injection into head from controller'); \r\n//--></script>");
    }
    else
    {
      context.getPageScopeData().put(RuntimePage.PAGESCOPE_HEAD_INJECTION, "\r\n<script language='javascript' type='text/javascript'><!--\r\n alert('Injection into head from controller'); \r\n//--></script>");
    }

    return null;
  }

  /**
   * Set random page title
   *
   * @param context RequestContext
   * @return null
   */
  public ForwardAction setRandomPageTitle(RequestContext context)
  {
    String[] sample = { "clowns", "eggplants", "shoes", "tacos", "elephants", "salami", "books", "ice" };
    int sel = mRNG.nextInt(sample.length);

    String title = "Page about " + sample[sel];
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("Executing Debugging.setRandomPageTitle: "+title);
      LOGGER.debug("--- START: RequestContext ---");
      LOGGER.debug(context.toString());
      LOGGER.debug("--- END: RequestContext ---");
    }

    context.getPageScopeData().put(RuntimePage.PAGESCOPE_TITLE, title);

    return null;

  }

  /**
   * Controller that always throws an exception
   *
   * @param context RequestContext
   * @return redirect is nothing, throws an exception
   */
  public ForwardAction throwException(RequestContext context)
  {
    //a_Log as error so that it shows up all the time since this is a sample that is intended to dump context
    LOGGER.error("Executing SampleControllers.dumpContext");

    throw new RuntimeException("Expected exception thrown by SampleControllers.throwException");
  }

  /**
   * Controller to set parameter string with value from session
   * Demonstrates simple targeting with manual segment setting (no LSCS or rules, java driven)
   *
   * Will toggle location between "1" and "2"
   * The segment name is used in generating the cache key so this will force 2 separate component cache entries for each location
   * The session is used simply to store state and toggle (toggle so that you can get both states by simply refreshing the page)
   *
   * @param context RequestContext
   * @return null always, no redirection
   */
  public ForwardAction setParameterFromSession(RequestContext context)
  {
    // Get the location from the session
    String location = (String)context.getSession().getAttribute("location");
    if (null != location)
    {
      // "location" was found in the session
      if (location.equals("1"))
      {
        // Detected location 1, toggle back to 2
        Segment segment = new Segment("2");
        segment.setName("Loc2");
        SortedSet<Segment> segments = new TreeSet<Segment>();
        segments.add(segment);
        context.getSession().getUserProfile().setSegments(segments);
        context.getSession().setAttribute("location", "2");
      }
      else
      {
        // Detected location 2, toggle back to 1
        Segment segment = new Segment("1");
        segment.setName("Loc1");
        SortedSet<Segment> segments = new TreeSet<Segment>();
        segments.add(segment);
        context.getSession().getUserProfile().setSegments(segments);
        context.getSession().setAttribute("location", "1");
      }
    }
    else
    {
      // Initial state, no session attribute "location" was present
      context.getSession().setAttribute("location", "1");
      Segment segment = new Segment("1");
      segment.setName("Loc1");
      SortedSet<Segment> segments = new TreeSet<Segment>();
      segments.add(segment);
      context.getSession().getUserProfile().setSegments(segments);
    }
    return null;
  }
}
