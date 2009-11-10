<?xml version='1.0'?>

<!--
   Copyright 2008 JBoss, a division of Red Hat
   License: LGPL
   Author: Pete Muir
   Author: Mark Newton <mark.newton@jboss.org>
-->

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                version="1.0"
                xmlns="http://www.w3.org/TR/xhtml1/transitional"
                xmlns:fo="http://www.w3.org/1999/XSL/Format"
                exclude-result-prefixes="#default">

   <xsl:import href="classpath:/xslt/org/jboss/seam/pdf.xsl"/>
   
     <!-- Change the font color for titles to black one -->
   <xsl:param name="title.color">black</xsl:param>
   <xsl:param name="titlepage.color">black</xsl:param>
   <xsl:param name="chapter.title.color">black</xsl:param>
   <xsl:param name="section.title.color">black</xsl:param>
   
   <!-- Change to monospace font for programlisting, needed to workaround crappy callouts -->
   <xsl:param name="programlisting.font" select="$monospace.font.family" />

   <!-- Make the font for programlisting slightly smaller -->
   <xsl:param name="programlisting.font.size" select="'75%'" />
   <xsl:param name="body.font.size" select="'75%'" /> 

<xsl:template name="book.titlepage.recto">
  <xsl:choose>
    <xsl:when test="bookinfo/title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/title"/>
    </xsl:when>
    <xsl:when test="info/title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/title"/>
    </xsl:when>
    <xsl:when test="title">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="title"/>
    </xsl:when>
  </xsl:choose>
  


  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/issuenum"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/issuenum"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="issuenum"/>

  <xsl:choose>
    <xsl:when test="bookinfo/subtitle">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/subtitle"/>
    </xsl:when>
    <xsl:when test="info/subtitle">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/subtitle"/>
    </xsl:when>
    <xsl:when test="subtitle">
      <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="subtitle"/>
    </xsl:when>
  </xsl:choose>

  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/corpauthor"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/corpauthor"/>

  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/authorgroup"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/authorgroup"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="bookinfo/author"/>
  <xsl:apply-templates mode="book.titlepage.recto.auto.mode" select="info/author"/>

</xsl:template>


<xsl:template match="author" mode="titlepage.mode">
  <fo:block margin-top="10px">
    <xsl:call-template name="anchor"/>
    <xsl:call-template name="person.name"/>
  </fo:block>
  <xsl:if test="affiliation/jobtitle">
    <fo:block>
      <xsl:apply-templates select="affiliation/jobtitle" mode="titlepage.mode"/>
    </fo:block>
  </xsl:if>
  <xsl:if test="affiliation/orgname">
    <fo:block>
      <xsl:apply-templates select="affiliation/orgname" mode="titlepage.mode"/>
    </fo:block>
  </xsl:if>
  <xsl:if test="email|affiliation/address/email">
    <fo:block>
      <xsl:apply-templates select="(email|affiliation/address/email)[1]"/>
    </fo:block>
  </xsl:if>
</xsl:template>

</xsl:stylesheet>
