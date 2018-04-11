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

package com.sample.livesite.runtime.controller.impl;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.URLRedirectForwardAction;
import com.interwoven.livesite.p13n.LoginUtils;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.UserSession;


/**
 * Sample login controller that checks user/password against a hard-coded list
 *
 * @author $Author: rpetti $
 * @version $Revision: #1 $
 *
 */
public abstract class LoginControllerBase
{
  /** the logger */
  private static final Log LOGGER = LogFactory.getLog(LoginControllerBase.class);

  /** URL redirect parameter */
  public static final String REDIRECT_LOGIN_SUCCESS_URL = "LoginSuccessUrl";
  
  /** URL redirect parameter */
  public static final String REDIRECT_LOGIN_FAIL_URL = "LoginFailUrl";

  /** URL redirect parameter */
  public static final String REDIRECT_LOGOUT_URL = "LogoutUrl";
 
  /**
   * Login the user and redirect if valid
   *   do not redirect if login invalid
   * 
   * @param context runtime context
   * @return forward action 
   * @throws IOException write exception
   */
  public ForwardAction login(RequestContext context) throws IOException
  {
    //a_Already logged in or authenticated
    ForwardAction faction = null;
    UserSession session = context.getSession();
        
    //a_Authenticate
    //if ((null != session && session.isLoggedIn()) || authenticateUser(context) )
    if (authenticateUser(context) )
    {
      //a_Find the page that the user came from
      String redirectUrl = context.getRedirectUrl();
      LOGGER.debug("redirect url is : " + redirectUrl);
      
      if (null == redirectUrl || 0 == redirectUrl.length())
      {
        //a_No redirect page in session, try to get override in component's controller
        redirectUrl = context.getLoginUrl();
      }
      
      //a_If redirect page found, then redirect
      if (null != redirectUrl && redirectUrl.length() > 0)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug(
            "Login successful, forwarding: user="
            +context.getParameterString(LoginUtils.PARAM_USERNAME, "null")
            +"  password(SHA1)="
            +context.getParameterString(LoginUtils.PARAM_PASSWORD, "null")
          );
        }
        faction = new URLRedirectForwardAction(context, redirectUrl);
        context.setRedirectUrl(null);
      }
    }
    else
    {
      String redirectUrl = context.getParameterString(REDIRECT_LOGIN_FAIL_URL);
      if (null != redirectUrl && redirectUrl.length() > 0)
      {
        if (LOGGER.isDebugEnabled())
        {
          LOGGER.debug(
            "Login failed, forwarding: user="
            +context.getParameterString(LoginUtils.PARAM_USERNAME, "null")
            +"  password(SHA1)="
            +context.getParameterString(LoginUtils.PARAM_PASSWORD, "null")
          );
        }
        faction = new URLRedirectForwardAction(context, redirectUrl);
        context.setRedirectUrl(null);
      }
    }
    return faction;
  }
      
  /**
   * Logout the user and redirect
   * 
   * @param context runtime context
   * @return forward action 
   * @throws IOException write exception
   */
  public ForwardAction logout(RequestContext context) throws IOException
  {
    String redirectUrl = context.getLogoutUrl();
    if (context.getSession().isLoggedIn())
    {
      LoginUtils.logout(context);
    }
    
    //a_Redirect somewhere else if URL parameter specified
    ForwardAction faction = null;
    if (null != redirectUrl && redirectUrl.length() > 0)
    {
      faction = new URLRedirectForwardAction(context, redirectUrl);
    }
    return faction;
  }
  
  /**
   * Authenticate the user
   * 
   * @param context RequestContext
   * @return true if authenticated
   */
  protected abstract boolean authenticateUser(RequestContext context);
}
