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

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.model.rule.SegmentUtils;
import com.interwoven.livesite.p13n.LoginUtils;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.controller.impl.LoginControllerBase;
import com.interwoven.livesite.runtime.impl.BaseRequestContext;
import com.interwoven.livesite.runtime.impl.BaseUserSession;

import com.sample.livesite.util.db.*;
import com.sample.livesite.util.mongo.*;

/**
 * Sample controller that uses hard coded values to check authentication
 *
 * @author $Author: rpetti $
 * @version $Revision: #1 $
 *
 */
public class SampleLoginController extends LoginControllerBase
{
  /** User name */
  public static final String PARAM_USERNAME = "user";

  /** Password sha1 hash */
  public static final String PARAM_PASSWORD = "password";

  /**
   * {@inheritDoc}
   * 
   * This is the method that is overloaded to provide custom login behavior
   * 
   * @see LoginUtils.authenticateUser for correct usage of session flags/etc
   */
  @Override
  protected boolean authenticateUser(RequestContext context)
  {
    BaseUserSession session = ((BaseUserSession) context.getSession());
    
    UserProfile profile = authenticate(context); 
    
    if (null != profile)
    {
      //a_Login Successful
      session.setUserProfile(profile);
      session.setLoggedIn(true);
      session.setRecognized(true);
      session.setAuthFailed(false);
      session.setAuthFailedCount(0);

      profile.setTransient(false);
      profile.setLastLoginDate(new Date());
      
      return true;
    }
    else
    {
      session.setLoggedIn(false);
      session.setAuthFailed(true);
      session.setAuthFailedCount(session.getAuthFailedCount() + 1);
      return false;
    }
  }

  /**
   * Try to authenticate the user
   * 
   * Username: test
   * Password: test   (SHA-1 encoded: a94a8fe5ccb19ba61c4c0873d391e987982fbbd3)
   *
   * http://lsds:1776/iw/admin/digest.html has encoder for convenience
   *
   * @param context RequestContext
   * @return UserProfile if authenticate or null
   */
  protected UserProfile authenticate(RequestContext context)
  {
    UserProfile profile = null;
    synchronized (context.getSession())
    {
      @SuppressWarnings("deprecation")
	  String username = context.getParameterString(PARAM_USERNAME);
      @SuppressWarnings("deprecation")
	  String password = context.getParameterString(PARAM_PASSWORD, "");  //a_This is SHA-1 done at the browser (using javascript)
    
      // Check against test:test
      //if (password.equals("a94a8fe5ccb19ba61c4c0873d391e987982fbbd3"))

      UsersDAO dao = new UsersDAO();
      List<UsersBean> ret = dao.select(username, password);
      
      if(ret.size() == 1) {
    	  UsersBean bn = ret.get(0);
    	  
          profile = ((BaseRequestContext)context).getRenderingConfig().getUserProfileService().create(context.getRequest());
          profile.setUserName(bn.getEmail());
          profile.setPassword(password);
          profile.setFirstName(bn.getFirstName());
          profile.setLastName(bn.getLastName());
          
          // apply segment 
          String seg = bn.getSegment();
          if(seg != null && seg.length() > 0) {
			  Set<String> segmentNames = new HashSet<String>();
			  segmentNames.add(seg);
			  SortedSet<Segment> segments = SegmentUtils.buildSegmentsFromNames(context, segmentNames.iterator());
			  profile.setSegments(segments);
          }

          // debug
          NewsDAO ndao = new NewsDAO();
          List<NewsBean> news = ndao.findAll("custom", "ot_news_en");
          
      }
      
//      if (password.equals("password"))
//      {
//        profile = ((BaseRequestContext)context).getRenderingConfig().getUserProfileService().create(context.getRequest());
//        profile.setUserName(username);
//      }
      
      context.getPageScopeData().put("sample.inuserid", username);
    }    
    
    return profile;
  }
  
}
