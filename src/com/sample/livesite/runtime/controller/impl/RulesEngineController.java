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

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import com.interwoven.livesite.common.web.ForwardAction;
import com.interwoven.livesite.common.web.Http302LocationRedirectForwardAction;
import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.model.rule.SegmentUtils;
import com.interwoven.livesite.model.sitemap.ReadOnlySiteMapXml;
import com.interwoven.livesite.p13n.model.UserProfile;
import com.interwoven.livesite.runtime.RequestContext;
import com.interwoven.wcm.rules.engine.base.RuleContext;
import com.interwoven.wcm.rules.engine.base.RuleExecDef;


/**
 * Rules Engine execution controller
 */
public class RulesEngineController
{
  /** the logger */
  private static final Log LOGGER = LogFactory.getLog(RulesEngineController.class);

  /**
   * Parameter that specifies which rules to execute, comma delimited
   *
   * e.g. MyRule.rule,SomeOtherRule.rule,MoreRules.rule
   */
  public static final String RULES_TO_EXECUTE = "RulesToExecute";

  /** If this element is set then redirect */
  public static final String ELEMENT_REDIRECT = "Redirect";

  /** Segments to set parameter */
  public static final String SEGMENTS = "Segments";

  /**
   * Execute a set of rules
   *
   * If output/Redirect is found, then take the text and redirect to it
   *
   * Following action can set the redirect:
   * <Action Operator='OutputValueSet'>
   *   <Path>Redirect</Path>
   *   <Value Source='Constant'>
   *     <Data>$PAGE_LINK[mypage]</Data>
   *   </Value>
   * </Action>
   *
   * @param context runtime context
   * @return forward action
   */
  public ForwardAction executeRules(RequestContext context)
  {
    String rulenames = context.getParameterString(RULES_TO_EXECUTE);
    if (null == rulenames)
    {
      throw new RuntimeException("executeRules: Unable to find '"+RULES_TO_EXECUTE+"' parameter.");
    }

    StringTokenizer tokens = new StringTokenizer(rulenames, ",");
    List<RuleExecDef> rulesToExecute = new LinkedList<RuleExecDef>();
    while (tokens.hasMoreTokens())
    {
      rulesToExecute.add(new RuleExecDef(tokens.nextToken()));
    }

    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("executeRules: Preparing to call rules engine for following rules: "+rulesToExecute);
    }

    Element result = DocumentHelper.createElement(RuleContext.RESPONSE_MODEL);
    context.executeRules(result, rulesToExecute);
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("executeRules: Rules engine returned: "+result.asXML());
    }

    Element output = result.element(RuleContext.OUTPUT_MODEL);
    if (null != output)
    {
      String redirectUrl = output.elementText(ELEMENT_REDIRECT);
      return new Http302LocationRedirectForwardAction(context, redirectUrl);
    }
    return null;
  }

  /**
   * Force set named segment on the current profile, removes all previous segments
   * Gets current segments (or default if none), adds the new segment names and then reprioritizes the set and sets on user
   *
   * NOTE: Rules engine is NOT called, segment is set on a user and assumed correct
   *
   * @param context RequestContext
   * @return null
   */
  public ForwardAction setSegments(RequestContext context)
  {
    if(context.getRequest().getAttribute(Segment.SEGMENT_EXECUTION_BYPASS) != null)
    {
      return null;
    }

    //a_Get requested segments
    String segmentnames = context.getParameterString(SEGMENTS);
    if (null == segmentnames)
    {
      throw new RuntimeException("setSegments: Missing required controller parameter: "+SEGMENTS);
    }
    ReadOnlySiteMapXml siteMap = context.getLiveSiteDal().getSiteMap().getReadOnlySiteMap();
    StringTokenizer tokens = new StringTokenizer(segmentnames, ",");
    Set<String> segmentNames = new HashSet<String>();
    while (tokens.hasMoreTokens())
    {
      String segmentName = tokens.nextToken();
      if (siteMap.isSegmentEnabledByName(segmentName))
      {
        segmentNames.add(segmentName);
      }
      else
      {
        if (LOGGER.isWarnEnabled())
        {
          LOGGER.warn("setSegments: Segment '"+segmentNames+"' is not found or enabled on site '"+context.getSite().getName()+"'");
        }
      }
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("setSegments: Segments to set: "+segmentNames);
    }

    //a_Set the new segments set on the profile if a new segment was added
    if (segmentNames.size() > 0)
    {
      SortedSet<Segment> segments = SegmentUtils.buildSegmentsFromNames(context, segmentNames.iterator());
      UserProfile profile = context.getSession().getUserProfile();
      profile.setSegments(segments);
    }
    else
    {
      if (LOGGER.isWarnEnabled())
      {
        LOGGER.warn("setSegments: No valid segments to set were specified for the given site.");
      }
    }
    return null;
  }

  /**
   * Execute segment rules against the rules engine and set result segments on the current user profile
   *
   * @param context RequestContext
   * @return null
   */
  public ForwardAction executeSegments(RequestContext context)
  {
    if(context.getRequest().getAttribute(Segment.SEGMENT_EXECUTION_BYPASS) != null)
    {
      return null;
    }

    //a_Get requested segments
    List<String> segmentsToExecute = new LinkedList<String>();
    String segmentNames = context.getParameterString(SEGMENTS);
    if (null == segmentNames)
    {
      throw new RuntimeException("executeSegments: Missing required controller parameter: "+SEGMENTS);
    }
    ReadOnlySiteMapXml siteMap = context.getLiveSiteDal().getSiteMap().getReadOnlySiteMap();
    StringTokenizer tokens = new StringTokenizer(segmentNames, ",");
    while (tokens.hasMoreTokens())
    {
      String segmentName = tokens.nextToken();
      if (siteMap.isSegmentEnabledByName(segmentName))
      {
        segmentsToExecute.add(segmentName);
      }
      else
      {
        if (LOGGER.isWarnEnabled())
        {
          LOGGER.warn("setSegments: Segment '"+segmentNames+"' is not found or enabled on site '"+context.getSite().getName()+"'");
        }
      }
    }
    if (LOGGER.isDebugEnabled())
    {
      LOGGER.debug("executeSegments: Segments to execute: "+segmentsToExecute);
    }

    //a_Execute and set new segments
    if (segmentsToExecute.size() > 0)
    {
      SortedSet<Segment> segments = SegmentUtils.executeSegmentRules(context, segmentsToExecute);
      UserProfile profile = context.getSession().getUserProfile();
      profile.setSegments(segments);
    }
    else
    {
      if (LOGGER.isWarnEnabled())
      {
        LOGGER.warn("executeSegments: No segments to execute were specified, doing nothing");
      }
    }
    return null;
  }

  /**
   * Execute site active segments
   *
   * @param context RequestContext
   * @return null
   */
  public ForwardAction executeSiteSegments(RequestContext context)
  {
    if(context.getRequest().getAttribute(Segment.SEGMENT_EXECUTION_BYPASS) != null)
    {
      return null;
    }

    List<String> segmentsToExecute = context.getLiveSiteDal().getSiteMap().getReadOnlySiteMap().getSegmentsForExecution();

    //a_Execute and set new segments
    if (segmentsToExecute.size() > 0)
    {
      SortedSet<Segment> segments = SegmentUtils.executeSegmentRules(context, segmentsToExecute);
      UserProfile profile = context.getSession().getUserProfile();
      profile.setSegments(segments);
    }
    else
    {
      if (LOGGER.isWarnEnabled())
      {
        LOGGER.warn("executeSiteSegments: No segments to execute were specified for the given site.  Make sure the site has at least one segment enabled.");
      }
    }
    return null;
  }
}
