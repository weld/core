<?xml version='1.0'?>

<!--
  Copyright 2008 JBoss, a division of Red Hat
  License: LGPL
  Author: Mark Newton <mark.newton@jboss.org>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:param name="use.simplified.author.group" select="1"/>

  <!-- XHTML settings -->
  <xsl:param name="html.stylesheet" select="'css/seamframework.css'"/>
     
  <xsl:param name="siteHref" select="'http://www.seamframework.org'"/>
  <xsl:param name="docHref" select="'http://www.seamframework.org/Documentation'"/>
  <xsl:param name="siteLinkText" select="'SeamFramework.org'"/>
  
  <!-- XHTML and PDF --> 
  <xsl:param name="callout.graphics.path">
    <xsl:if test="$img.src.path != ''">
      <xsl:value-of select="$img.src.path"/>
    </xsl:if>
    <xsl:text>images/seamframework/docbook/callouts/</xsl:text>
  </xsl:param>
 
  <xsl:param name="admon.graphics.path">
    <xsl:if test="$img.src.path != ''">
      <xsl:value-of select="$img.src.path"/>
    </xsl:if>
    <xsl:text>images/seamframework/docbook/</xsl:text>
  </xsl:param>
  
</xsl:stylesheet>
