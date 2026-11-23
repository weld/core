<?xml version="1.0" encoding="UTF-8"?>
<!-- Convert the Platform TCK's versioned TestNG class/method exclusions into
     Surefire excludesFile entries. Unsupported TestNG regexes fail the build. -->
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0">
    <xsl:output method="text" encoding="UTF-8"/>

    <xsl:template match="/">
        <xsl:for-each select="/suite/test/classes/class[methods/exclude]">
            <xsl:variable name="classPath" select="translate(@name, '.', '/')"/>
            <xsl:for-each select="methods/exclude">
                <xsl:choose>
                    <xsl:when test="@name = '.*'">
                        <xsl:text>**/</xsl:text><xsl:value-of select="$classPath"/><xsl:text>.java&#10;</xsl:text>
                    </xsl:when>
                    <xsl:when test="string-length(@name) &gt; 0 and translate(@name, 'abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_$', '') = ''">
                        <xsl:text>**/</xsl:text><xsl:value-of select="$classPath"/><xsl:text>.java#</xsl:text><xsl:value-of select="@name"/><xsl:text>&#10;</xsl:text>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:message terminate="yes">Unsupported Platform TCK method exclusion: <xsl:value-of select="../../@name"/>#<xsl:value-of select="@name"/></xsl:message>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
</xsl:stylesheet>
