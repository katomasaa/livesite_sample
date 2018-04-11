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

import java.util.ArrayList;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.runtime.RequestContext;



/**
 * Email controller actions
 *
 * @author $Author: jchang $
 * @version $Revision: #2 $
 */
public class EmailController
{
  /** logger */
  private static final transient Log LOGGER = LogFactory.getLog(EmailController.class);

  /**
   * mail to request param name
   * This is a name of the actual parameter that contakins a list of mail-to targets
   */
  private static final String MAIL_TO_REQUEST_NAME = "mail.to.request.name";
  /** mail to */
  private static final String MAIL_TO = "mail.to";
  /** mail subject */
  private static final String MAIL_SUBJECT = "mail.subject";
  /** mail body */
  private static final String MAIL_BODY = "mail.body";
  /** mail host */
  private static final String MAIL_HOST = "mail.host";
  /** mail from */
  private static final String MAIL_FROM = "mail.from";
  /** mail user */
  private static final String MAIL_USER = "mail.user";
  /** mail store protocol */
  private static final String MAIL_STORE_PROTOCOL = "mail.store.protocol";
  /** mail transport protocol */
  private static final String MAIL_TRANSPORT_PROTOCOL = "mail.transport.protocol";
  /** mail smtp host */
  private static final String MAIL_SMTP_HOST = "mail.smtp.host";
  /** mail smtp user */
  private static final String MAIL_SMTP_USER = "mail.smtp.user";
  /** mail debug */
  private static final String MAIL_DEBUG = "mail.debug";

  /**
   * create new EmailControllerActions
   *
   */
  public EmailController()
  {
  }

  /**
   * Sends an email
   *
   * @param context Runtime context
   * @return ForwardAction
   */
  public ForwardAction sendEmail(RequestContext context)
  {
    //a_Check target
    ArrayList mailToList = new ArrayList();
    String mailToName = context.getParameterString(MAIL_TO_REQUEST_NAME);
    if (null != mailToName)
    {
      String[] mailToRecipients = context.getRequest().getParameterValues(mailToName);
      if (null != mailToRecipients)
      {
        for (int i=0; i<mailToRecipients.length; ++i)
        {
          mailToList.add(mailToRecipients[i]);
        }
      }
    }

    String mailTo = context.getParameterString(MAIL_TO);
    if (null != mailTo)
    {
      mailToList.add(mailTo);
    }

    //a_Should have at least one recipient
    if (mailToList.size() == 0)
    {
      LOGGER.warn("EmailControllerAction: No mail recipients found.");
      return null;
    }

    //a_From
    String mailFrom = context.getParameterString(MAIL_FROM);

    //a_Subject
    String mailSubject = context.getParameterString(MAIL_SUBJECT);
    if (null == mailSubject)
    {
      LOGGER.warn("EmailControllerAction: "+MAIL_SUBJECT+" param not specified, not sending email.");
      return null;
    }

    //a_Body
    String mailBody = context.getParameterString(MAIL_BODY);
    if (null == mailBody)
    {
      LOGGER.warn("EmailControllerAction: "+MAIL_BODY+" param not specified, not sending email.");
      return null;
    }

    Properties props = getMailProperties(context);
    if (null != props)
    {
      Session session = Session.getInstance(props);
      MimeMessage message = new MimeMessage(session);
      try
      {
        //message.setFrom( new InternetAddress(mailFrom));
        for (int i=0; i<mailToList.size(); ++i)
        {
          message.addRecipient(Message.RecipientType.TO, new InternetAddress((String)mailToList.get(i)));
        }
        if (null != mailFrom)
        {
          message.setFrom(new InternetAddress(mailFrom));
        }
        message.setSubject(mailSubject);
        message.setText(mailBody);
        Transport.send(message);
      }
      catch (MessagingException ex)
      {
        LOGGER.error("EmailControllerAction: Cannot send email.", ex);
      }
    }
    return null;
  }

  /**
   * Populate props from component
   *
   * @param context request
   * @return Properties of mail server
   */
  public Properties getMailProperties(RequestContext context)
  {
    //a_Mail properties
    Properties props = new Properties();
    String host = context.getParameterString(MAIL_HOST);

    //a_Check required
    if (null == host)
    {
      LOGGER.warn("EmailControllerAction: "+MAIL_HOST+" param not specified, not sending email.");
      return null;
    }
    props.put(MAIL_HOST, host);

    //a_Add optionals
    if (null != context.getParameterString(MAIL_USER))
    {
      props.put(MAIL_USER, context.getParameterString(MAIL_USER));
    }
    if (null != context.getParameterString(MAIL_STORE_PROTOCOL))
    {
      props.put(MAIL_STORE_PROTOCOL, context.getParameterString(MAIL_STORE_PROTOCOL));
    }
    if (null != context.getParameterString(MAIL_TRANSPORT_PROTOCOL))
    {
      props.put(MAIL_TRANSPORT_PROTOCOL, context.getParameterString(MAIL_TRANSPORT_PROTOCOL));
    }
    if (null != context.getParameterString(MAIL_SMTP_HOST))
    {
      props.put(MAIL_SMTP_HOST, context.getParameterString(MAIL_SMTP_HOST));
    }
    if (null != context.getParameterString(MAIL_SMTP_USER))
    {
      props.put(MAIL_SMTP_USER, context.getParameterString(MAIL_SMTP_USER));
    }
    if (null != context.getParameterString(MAIL_DEBUG))
    {
      props.put(MAIL_DEBUG, context.getParameterString(MAIL_DEBUG));
    }

    return props;
  }
}
