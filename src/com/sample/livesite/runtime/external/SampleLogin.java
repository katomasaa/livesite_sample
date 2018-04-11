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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import com.interwoven.livesite.dom4j.Dom4jUtils;
import com.interwoven.livesite.p13n.LoginUtils;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.livesite.runtime.UserSession;

/*****************************************************************************
 * Login externals
 *
 * @author $Author: rpetti $
 * @version $Revision: #1 $
 *****************************************************************************/
public class SampleLogin
{
  /**
   * the logger
   */
  protected final Log mLogger = LogFactory.getLog(this.getClass());

  /** default ctor */
  public SampleLogin()
  {
  }

  /**
   * Execute login external (LoginController will set the data this external uses to emit)
   *
   * Output XPath: /Result/Login
   *
   * @param context request
   * @return Result DOM
   */
  public Document login(RequestContext context)
  {
    Document doc = Dom4jUtils.newDocument();
    Element login = doc.addElement("Login");

    //a_Add persist login via cookie if site is enabled for it
    if (LoginUtils.isSiteUsingCookies(context))
    {
      login.addElement("SiteUsingCookies").addText(LoginUtils.PARAM_REMEMBERME);
    }

    //a_Add session based elements
    UserSession userSession = context.getSession();
    synchronized (userSession)
    {
      if (userSession.isLoggedIn())
      {
        login.addElement("LoggedIn");
      }
      else
      {
        login.addElement("NotLoggedIn");

        if (userSession.isAuthFailed())
        {
          //a_<AuthFailed count="3"/>  entry for if this is a failure and how many failures so far
          Element failed = login.addElement("AuthFailed");
          failed.addAttribute("count", new Integer(context.getSession().getAuthFailedCount()).toString());
        }
      }

      UserProfile up = userSession.getUserProfile();
      if (null != up)
      {
        Element e = up.toElement();
        login.add(e);
        if (mLogger.isDebugEnabled())
        {
          mLogger.debug("UserProfile="+e);
        }
      }
    }
    
//    com.sample.livesite.util.api.LscsUtil ls = new com.sample.livesite.util.api.LscsUtil("//Demo3/default/main/sample");
//
//    Map<String, String> params = new HashMap<String, String>();
//
//    params.put("TeamSite/Templating/DCR/Type", "product/column");
//    params.put("iw_form_valid", "true");
//
//    Document doc2 = ls.GetDocsByMeta(params);
//    
//    mLogger.debug(doc2.asXML());
    
    return doc;
  }
}
