<?xml version='1.0'?>

<!--
  Copyright 2008 JBoss, a division of Red Hat
  License: LGPL
  Author: Mark Newton <mark.newton@jboss.org>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">

  <xsl:import href="classpath:/xslt/org/jboss/eclipse.xsl"/>
  <xsl:import href="common.xsl"/>

  <xsl:param name="html.stylesheet" select="'css/seamframework-eclipse.css'"/>

  <xsl:param name="eclipse.plugin.name">Seam Framework Help</xsl:param>
  <xsl:param name="eclipse.plugin.id">org.jboss.seam.help</xsl:param>
  <xsl:param name="eclipse.plugin.provider">SeamFramework.org</xsl:param>
  
</xsl:stylesheet>
