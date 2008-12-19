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

   <xsl:import href="classpath:/xslt/org/jboss/pdf.xsl"/>
   <xsl:import href="common.xsl"/>
   <xsl:import href="fop1.xsl" />
      
   <!-- Change the font color for titles to SeamFramework.org one -->
   <xsl:param name="title.color">#576C74</xsl:param>
   <xsl:param name="titlepage.color">#885324</xsl:param>
   <xsl:param name="chapter.title.color">#BA5624</xsl:param>
   <xsl:param name="section.title.color">#BA5624</xsl:param>

   <!-- Style tables to look like SeamFramework.org-->
   <xsl:param name="table.cell.border.color">#D3D2D1</xsl:param>
   <xsl:param name="table.frame.border.color">#D3D2D1</xsl:param>
   <xsl:param name="table.cell.border.thickness">0.6pt</xsl:param>

   <xsl:param name="table.frame.border.thickness">0.6pt</xsl:param>
   <xsl:param name="table.cell.border.right.color">white</xsl:param>
   <xsl:param name="table.cell.border.left.color">#D3D2D1</xsl:param>
   <xsl:param name="table.frame.border.right.color">white</xsl:param>
   <xsl:param name="table.frame.border.left.color">white</xsl:param>

   <xsl:template name="table.cell.block.properties">
      <!-- highlight this entry? -->
      <xsl:if test="ancestor::thead or ancestor::tfoot">
         <xsl:attribute name="font-weight">bold</xsl:attribute>
         <xsl:attribute name="background-color">#EDE8DB</xsl:attribute>
         <xsl:attribute name="color">black</xsl:attribute>
      </xsl:if>
   </xsl:template>

   <!--
      From: fo/table.xsl
      Reason: Table Header format
      Version:1.72
   -->
   <!-- customize this template to add row properties -->
   <xsl:template name="table.row.properties">
      <xsl:variable name="bgcolor">
         <xsl:call-template name="dbfo-attribute">
            <xsl:with-param name="pis" select="processing-instruction('dbfo')" />
            <xsl:with-param name="attribute" select="'bgcolor'" />
         </xsl:call-template>
      </xsl:variable>
      <xsl:if test="$bgcolor != ''">
         <xsl:attribute name="background-color">
      <xsl:value-of select="$bgcolor" />
    </xsl:attribute>
      </xsl:if>
      <xsl:if test="ancestor::thead or ancestor::tfoot">
         <xsl:attribute name="background-color">#EDE8DB</xsl:attribute>
      </xsl:if>
   </xsl:template>

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
