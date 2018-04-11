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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;

import com.interwoven.livesite.common.codec.URLUTF8Codec;
import com.interwoven.livesite.model.rule.Segment;
import com.interwoven.livesite.runtime.RequestContext;

/**
 * this encapsulates Livesite external functionality (site map)
 *
 * @author $Author: jchang $
 * @version $Revision: #2 $
 */
public class LivesiteSiteMap
{
  /**
   * Used by getMergedSiteMap. This variable is a comma-separated list of
   * strings. When two nodes are merged, any query string parameters whose names
   * are found in this list will be merged.
   */
  private static final String NAME_CONSTANT_MERGE_PARAMS = "mergeParams";

  /**
   * the logger
   */
  protected final Log mLogger = LogFactory.getLog(this.getClass());

  /**
   * get the site map document
   *
   * @param context the requestContext
   * @return the site map document if processing fails
   */
  public Document getSiteMap(RequestContext context)
  {
    Document doc = (Document) context.getLiveSiteDal().getSiteMap().getDocument().clone();

    findSegmentXml(doc, context);
    urlEncodeQueryValues(doc);
    if (mLogger.isDebugEnabled())
    {
      String xml = doc.asXML();
      mLogger.debug(xml);
    }
    return doc;
  }

  /**
   * get the site map document, merging the nodes of all segments to which the
   * user belongs.
   *
   * @param context the requestContext
   * @return the site map document if processing fails
   */
  public Document getMergedSiteMap(RequestContext context)
  {
    Document doc = (Document) context.getLiveSiteDal().getSiteMap().getDocument().clone();

    // determine what parameters, if any, need to be merged
    String[] mergeParams;
    String mergeParamsString = context.getParameterString(NAME_CONSTANT_MERGE_PARAMS);
    if (StringUtils.isNotBlank(mergeParamsString))
    {
      mergeParams = mergeParamsString.split(",");
    }
    else
    {
      mergeParams = null;
    }

    findSegmentXml(doc, context, true, mergeParams);
    urlEncodeQueryValues(doc);
    if (mLogger.isDebugEnabled())
    {
      String xml = doc.asXML();
      mLogger.debug(xml);
    }
    return doc;
  }

  /**
   *
   * Gets the segment fragment with the current node location as the leaf.
   *
   * @param context
   *          the request context
   * @return the site map document
   *
   */
  public Document getBreadCrumb(RequestContext context)
  {
    Document doc = (Document) context.getLiveSiteDal().getSiteMap().getDocument().clone();

    findSegmentXml(doc, context);
    urlEncodeQueryValues(doc);
    findCurrentSegmentNodeXml(doc, context);
    if (mLogger.isDebugEnabled())
    {
      String xml = doc.asXML();
      mLogger.debug(xml);
    }
    
    doc.getRootElement().addElement("currentPage").addCDATA(context.getPageName());
    
    return doc;
  }

  /**
   *
   * Retrieves a pared down Document containing only the current segment If in
   * development mode, the default segment will be selected otherwise, the first
   * segment in the list under runtime will be selected.
   *
   * @param doc Document object containing the full site map
   * @param context RequestContext passed to this component.
   *
   */
  protected void findSegmentXml(Document doc, RequestContext context)
  {
    findSegmentXml(doc, context, false, null);
  }

  /**
   * Retrieves a pared down Document based on the segments provided in the set.
   *
   * If the mergeSegments parameter is true, the resulting document will
   * combine the &lt;node&gt; elements from all of the provided segments. If
   * false, the resulting document will only contain the &lt;node&gt; elements
   * from the first segment in the set.
   *
   * If the mergeQueryParameters parameter is true, the resulting document will
   * also merge the query string parameters of whenever multiple segments have
   * equivalent nodes.
   *
   * @param doc Document object containing the full site map
   * @param context RequestContext passed to this component, used to determine
   * the segments
   * @param mergeSegments If true, the segments will be merged
   * @param mergeQueryParameters If non-null, the query params will also be
   *  merged whenever a node is merged
   */
  protected void findSegmentXml(Document doc, RequestContext context,
      boolean mergeSegments, String[] mergeQueryParameters)
  {
    // get the current segment id from session
    Set segments = context.getSession().getUserProfile().getSegments();
    Document originalDoc = (Document) context.getLiveSiteDal().getSiteMap().getDocument();
    findSegmentXml(doc, originalDoc, segments, mergeSegments, mergeQueryParameters);
  }


  /**
   * Retrieves a pared down Document based on the segments provided in the set.
   *
   * If the mergeSegments parameter is true, the resulting document will
   * combine the &lt;node&gt; elements from all of the provided segments. If
   * false, the resulting document will only contain the &lt;node&gt; elements
   * from the first segment in the set.
   *
   * If the mergeQueryParameters parameter is true, the resulting document will
   * also merge the query string parameters of whenever multiple segments have
   * equivalent nodes.
   *
   * @param doc Document object containing the full site map
   * @param segments Segments that will be included in the XML
   * @param mergeSegments If true, the segments will be merged
   * @param mergeQueryParameters If non-null, the query params will also be
   *  merged whenever a node is merged
   */
  protected void findSegmentXml(Document doc, Set segments,
      boolean mergeSegments, String[] mergeQueryParameters)
  {
    Document originalDoc = (Document)doc.clone(); // used to expand any refId's in the stripped document.
    findSegmentXml(doc, originalDoc, segments, mergeSegments, mergeQueryParameters);
  }

  /**
   * Retrieves a pared down Document based on the segments provided in the set.
   *
   * If the mergeSegments parameter is true, the resulting document will
   * combine the &lt;node&gt; elements from all of the provided segments. If
   * false, the resulting document will only contain the &lt;node&gt; elements
   * from the first segment in the set.
   *
   * If the mergeQueryParameters parameter is true, the resulting document will
   * also merge the query string parameters of whenever multiple segments have
   * equivalent nodes.
   *
   * @param doc Document object containing the full site map
   * @param originalDoc Document object containing the original full site map
   * @param segments Segments that will be included in the XML
   * @param mergeSegments If true, the segments will be merged
   * @param mergeQueryParameters If non-null, the query params will also be
   *  merged whenever a node is merged
   */
   private void findSegmentXml(Document doc, Document originalDoc, Set segments,
      boolean mergeSegments, String[] mergeQueryParameters)
   {
    if (segments.size() > 0)
    {
      if (mergeSegments)
      {
        if (mLogger.isDebugEnabled())
        {
          mLogger.debug("Creating merged segment document");
        }
        /*
         * 1. Create a new segment node with id='merged'
         * 2. For each existing segment that the user belongs to:
         *    a. Find a matching <segment> node in the document.
         *    b. Add any non-conflicting nodes to the merged segment.
         *    c. Merge any matching child nodes
         * 3. Detach the existing segments, since it has now been merged
         */

        Element mergedSegment = doc.getRootElement().addElement("segment");
        String mergedId = "merged";
        mergedSegment.addAttribute("id", mergedId);
        mergedSegment.addAttribute("priority", "-1");
        mergedSegment.addAttribute("enabled", "true");
        mergedSegment.addAttribute("default", "false");

        for (Iterator i = segments.iterator(); i.hasNext();)
        {
          Segment segment = (Segment)i.next();
          if (mLogger.isDebugEnabled())
          {
            mLogger.debug("Processing segment "+segment.getName()+" ["
                +segment.getId()+"]");
          }
          Element segmentElem = (Element) doc.selectSingleNode(
              "/site-map/segment[@id='"+segment.getId()+"']");

          if (null != segmentElem)
          {
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("Merging this segment.");
            }
            mergeSitemapNodes(segmentElem, mergedSegment, mergeQueryParameters);
          }
        }

        // detach existing segments
        List existingSegments =
          doc.selectNodes("site-map/segment[@id!='"+mergedId+"']");
        for (Iterator i = existingSegments.iterator(); i.hasNext();)
        {
          ((Node)i.next()).detach();
        }
      }
      else
      {
        // Get the first one and pare out the rest.
        Segment segment = (Segment) segments.iterator().next();
        String id = segment.getId();

        // select all nodes not matching and remove from the document.
        List nodeList = doc.selectNodes("/site-map/segment[@id!='" + id + "']");
        for (Iterator iter = nodeList.iterator(); iter.hasNext();)
        {
          Node element = (Node) iter.next();
          element.detach();
        }
      }
    }
    else
    {
      // fallback but this should never happen.
      List nodeList = doc.selectNodes("/site-map/segment[@default='false']");
      for (Iterator iter = nodeList.iterator(); iter.hasNext();)
      {
        Node element = (Node) iter.next();
        element.detach();
      }
    }


    // create an expanded xml doc if any of the nodes have refid's
    if(doc.selectNodes("//node/@refid").size()>0) // only expand if there are refid attrs present.
    {
      if(mLogger.isDebugEnabled())
      {
        mLogger.debug("Expanding nodes with refid attribute.");
      }
      expandSegmentRefNodes(originalDoc, doc);
    }
   }

  /**
   * Merges the children of two sitemap nodes. Any nodes in the source which are
   * not present in the sink are added to the sink. Any nodes in the source
   * which are present in the sink will be recursively merged.
   *
   * @param source The sitemap node containing children to be merged
   * @param sink The sitemap node which will be the recipient of the merged children.
   * @param mergeQueryParameters Indicates whether query parameters should be merged when source and sink contain nodes with same name, same link
   */
  private void mergeSitemapNodes(Element source, Element sink, String[] mergeQueryParameters)
  {
    // get the children of the source node
    List sourceElems = source.selectNodes("node");
    if (null != sourceElems)
    {
      // iterate through the source children
      for (Iterator nodeIterator = sourceElems.iterator(); nodeIterator.hasNext();)
      {
        Element sourceChild = (Element) nodeIterator.next();
        if (mLogger.isDebugEnabled())
        {
          mLogger.debug("Processing node " + sourceChild);
        }

        /*
         * Check if there is a conflicting node in the sink. If there is no
         * conflicting node, just add the source node to the sink. If there is a
         * conflicting node, merge the two's children, and optionally merge the
         * query string parameters of the two nodes.
         */
        List conflictingNodes;

        // determine if this is a reference node, which is either copied or not,
        // but there is no point in merging them.
        Node refIdNode = sourceChild.selectSingleNode("@refid");
        if (null == refIdNode)
        {
          if (mLogger.isDebugEnabled())
          {

            mLogger.debug("This is a real node: " + sourceChild.selectSingleNode("label").getText());
          }
          String label = sourceChild.selectSingleNode("label").getText();
          conflictingNodes = sink.selectNodes("node[label='" + label + "']");
          if (conflictingNodes.isEmpty())
          {
            sink.add(sourceChild.detach());
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("Merged this node.");
            }
          }
          else
          {
            // this node conflicts, but we want to merge the children together
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("A node exists with this name. Recursing into children...");
            }
            Element sinkChild = (Element) conflictingNodes.get(0);
            mergeSitemapNodes(sourceChild, sinkChild, mergeQueryParameters);

            // optionally merge the query parameters together.
            if (null != mergeQueryParameters)
            {
              Element sourceQueryString = (Element) sourceChild.selectSingleNode("link/query-string");

              Element sinkQueryString = (Element) sinkChild.selectSingleNode("link/query-string");

              for (int i = 0; i < mergeQueryParameters.length; i++)
              {
                String paramName = mergeQueryParameters[i];
                if (mLogger.isDebugEnabled())
                {
                  mLogger.debug("Checking for query parameter " + paramName);
                }
                Element paramNode = (Element) sourceQueryString.selectSingleNode("parameter[name='" + paramName + "']");
                if (null != paramNode)
                {
                  sinkQueryString.add(paramNode.detach());
                  if (mLogger.isDebugEnabled())
                  {
                    mLogger.debug("Merged for query parameter " + paramName + "=" + paramNode.selectSingleNode("value").getText());
                  }
                }
              }
            }
          }
        }
        else
        {
          if (mLogger.isDebugEnabled())
          {
            mLogger.debug("This is a reference node");
          }
          conflictingNodes = sink.selectNodes("node[@refid='" + refIdNode.getText() + "']");
          if (conflictingNodes.isEmpty())
          {
            sink.add(sourceChild.detach());
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("Merged this node.");
            }
          }
          else
          {
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("A node exists with this refid. Ignoring this node.");
            }
          }
        }
      }
    }
  }

  /**
   * expand segment ref nodes
   *
   * @param originalDoc something
   * @param doc something
   */
  private void expandSegmentRefNodes(Document originalDoc, Document doc)
  {
    if (mLogger.isDebugEnabled())
    {
      mLogger.debug("Before expandSegmentRefNodes()");
      String xml = doc.asXML();
      mLogger.debug(xml);
    }
    List refidNodes = doc.selectNodes("//node[@refid]");
    for (Iterator iter = refidNodes.iterator(); iter.hasNext();)
    {
      Element refElement = (Element) iter.next();
      String refid = refElement.attributeValue("refid", "");

      // find the node in the originalDoc
      // and copy it's children.
      Node originalNode = originalDoc.selectSingleNode("//node[@id='" + refid + "']");
      List originalNodeChildren = ((Element) originalNode).elements();
      List deepCopyChildren = new ArrayList();
      for (Iterator iter2 = originalNodeChildren.iterator(); iter2.hasNext();)
      {
        Element childElement = (Element) iter2.next();
        Element deepCopyNode = (Element) childElement.clone();
        deepCopyChildren.add(deepCopyNode);
      }
      // remove the ref id from the refElement
      Attribute refElementAttr = refElement.attribute("refid");
      refElement.remove(refElementAttr);

      // copy the attr nodes of the orignal to the ref
      List originalAttrs = ((Element) originalNode).attributes();
      for (int x = 0; x < originalAttrs.size(); x++)
      {
        Attribute attrToAdd = (Attribute) originalAttrs.get(x);
        Attribute attrClone = (Attribute) attrToAdd.clone();
        refElement.add(attrClone);
      }

      // copy the deep copy children elements to the refElement .
      for (int i = 0; i < deepCopyChildren.size(); i++)
      {
        Element childToAdd = (Element) deepCopyChildren.get(i);
        refElement.add(childToAdd);
      }

    }
    if (mLogger.isDebugEnabled())
    {
      mLogger.debug("After expandSegmentRefNodes()");
      String xml = doc.asXML();
      mLogger.debug(xml);
    }
  }

  /**
   *
   * Strips down Document containing only the hierarchy of nodes leading down to
   * the current node.
   *
   * @param doc Document object containing the full site map
   * @param context RequestContext passed to this component.
   *
   */
  private void findCurrentSegmentNodeXml(Document doc, RequestContext context)
  {
    if (context.isRuntime())
    {
      // TODO: need to make the parameter piece work to get the node-id at
      // runtime.
      String nodeId = context.getParameterString("node-id");
      if (nodeId == null)
      {
        if (mLogger.isDebugEnabled())
        {
          mLogger.debug("findCurrentSegmentNodeXml: node id not found in request");
        }
        Element segmentElement = (Element) doc.getRootElement().selectSingleNode("segment");
        String segmentId = segmentElement.attributeValue("id", "");

        Element node = null;
        if (context.getPageName() != null && !"".equals(context.getPageName()))
        {

          // see if the current page is in the sitemap.
          List nodes = doc.getRootElement().selectNodes("//node[@visible-in-breadcrumbs='true' and link[@type='page']/value='" + context.getPageName() + "']");
          if (mLogger.isDebugEnabled())
          {
            mLogger.debug("findCurrentSegmentNodeXml: segment: " + segmentId + ", found " + nodes.size() + " nodes for page: " + context.getPageName());
          }
          // take the first one.
          if (nodes.size() > 0)
          {
            node = (Element) nodes.get(0);
          }
          else
          {
            if (mLogger.isDebugEnabled())
            {
              mLogger.debug("findCurrentSegmentNodeXml: segment: " + segmentId + ", current page: " + context.getPageName() + " not found in sitemap.");
            }
          }
        }
        else
        {
          String startPage = context.getSite().getStartPage();
          // see if the start page is in the sitemap.
          List nodes = doc.getRootElement().selectNodes("//node[@visible-in-breadcrumbs='true' and link[@type='page']/value='" + startPage + "']");
          if (mLogger.isDebugEnabled())
          {
            mLogger.debug("findCurrentSegmentNodeXml: segment: " + segmentId + ", found " + nodes.size() + " nodes for start page: " + startPage);
          }
          // take the first one.
          if (nodes.size() > 0)
          {
            node = (Element) nodes.get(0);
          }
          else
          // remove all.
          {
            if (mLogger.isWarnEnabled())
            {
              mLogger.warn("findCurrentSegmentNodeXml: segment: " + segmentId + ", no nodes present for current page or start page, removing all nodes.");
            }
            segmentElement.content().clear();
          }
        }
        if (node != null)
        {
          if (mLogger.isDebugEnabled())
          {
            mLogger.debug("findCurrentSegmentNodeXml: segment: " + segmentId + ", selected node: " + node.valueOf("@id"));
          }
          parseLeafNodeHierarchy(doc, node.attributeValue("id"));
        }
      }
      else
      {
        parseLeafNodeHierarchy(doc, nodeId);
      }
    }
    else
    {
      // Select the root node, the traverse the first node of each subsequent
      // child to
      // to get an end leaf node for preview purposes.
      Element currentElement = (Element) doc.selectSingleNode("/site-map/segment");
      List elementList = currentElement.elements("node");
      while (elementList.size() > 0)
      {
        // find the first element that's visible.
        Element foundElement = (Element) currentElement.selectSingleNode("node[@visible-in-breadcrumbs='true']");
        if (foundElement != null)
        {
          currentElement = foundElement;
          elementList = currentElement.elements();
        }
        else
        {
          break;
        }
      }
      // now pare down the segment tree to just a single hierarchical path to
      // the leaf node.
      // get the id of the last leaf node found.
      if (currentElement.getName().equals("node"))
      {
        String nodeId = currentElement.attributeValue("id");
        parseLeafNodeHierarchy(doc, nodeId);
      }
    }
  }

  /**
   *
   * Traverses up the segment tree from the specified node to get an ancestors
   * only tree fragment to the specified node
   *
   * @param doc Sitemap segment document
   * @param nodeId The nodeId of the current node.
   *
   */
  private void parseLeafNodeHierarchy(Document doc, String nodeId)
  {
    Element currentElement = (Element) doc.selectSingleNode("//node[@id='" + nodeId + "']");

    // Detach the children of the selected node.
    List childernToDetach = currentElement.selectNodes("node");

    for (Iterator iter = childernToDetach.iterator(); iter.hasNext();)
    {
      Node element = (Node) iter.next();
      element.detach();
    }

    Element parentElement = currentElement.getParent();
    while (parentElement != null)
    {
      List nodesToDetach = parentElement.selectNodes("node[@id!='" + nodeId + "']");
      for (Iterator iter = nodesToDetach.iterator(); iter.hasNext();)
      {
        Node element = (Node) iter.next();
        element.detach();
      }
      nodeId = parentElement.attributeValue("id");
      parentElement = parentElement.getParent();
    }
  }

  /**
   *
   * This method will find any query values in the site map and make sure they
   * are url encoded.
   *
   * @param doc Document object containing the site map segment.
   */
  private void urlEncodeQueryValues(Document doc)
  {
    // The document should already be pared down to just one segment at this
    // point.
    List nodeList = doc.selectNodes("//query-string/parameter");
    for (Iterator iter = nodeList.iterator(); iter.hasNext();)
    {
      Element element = (Element) iter.next();
      List elements = element.elements();
      for (Iterator iterator = elements.iterator(); iterator.hasNext();)
      {
        Element childElement = (Element) iterator.next();
        if (childElement.getName().equals("name") || childElement.getName().equals("value"))
        {
          String originalValue = childElement.getText();
          childElement.setText(URLUTF8Codec.encodeString(originalValue));
        }
      }
    }
  }
}
