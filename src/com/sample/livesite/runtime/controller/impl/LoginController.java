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

import com.interwoven.livesite.p13n.LoginUtils;
import com.interwoven.livesite.runtime.RequestContext;

/*****************************************************************************
 * Login Controller actions
 *
 * @author $Author: rpetti $
 * @version $Revision: #1 $
 *****************************************************************************/
public class LoginController extends LoginControllerBase
{
  /**
   * Authenticate the user
   * 
   * @param context RequestContext
   * @return UserProfile if authenticated or null
   */
  protected boolean authenticateUser(RequestContext context)
  {
    return LoginUtils.authenticateUser(context);    
  }
}
