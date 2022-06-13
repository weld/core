package org.jboss.weld.environment.servlet.test.util;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.BeanDiscoveryMode;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.Asset;
import org.jboss.shrinkwrap.api.asset.ByteArrayAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.impl.BeansXml;

public class Deployments {

    public static final ArchivePath MARKER_SKIP_PROCESSOR = ArchivePaths.create("META-INF/weld.servlet.skipProcessor");

    public static final String DEFAULT_WEB_XML_START = "<web-app version=\"3.1\"\n"
            + "   xmlns=\"http://xmlns.jcp.org/xml/ns/javaee\"\n"
            + "   xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"
            + "   xsi:schemaLocation=\"\n"
            + "      http://xmlns.jcp.org/xml/ns/javaee\n"
            + "      http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd\">";

    public static final String DEFAULT_WEB_XML_BODY = toListener("org.jboss.weld.environment.servlet.Listener")
            + "<resource-env-ref><resource-env-ref-name>BeanManager</resource-env-ref-name><resource-env-ref-type>jakarta.enterprise.inject.spi.BeanManager</resource-env-ref-type></resource-env-ref> ";
    public static final String DEFAULT_WEB_XML_PREFIX = DEFAULT_WEB_XML_START + DEFAULT_WEB_XML_BODY;
    public static final String DEFAULT_WEB_XML_SUFFIX = "</web-app>";

    public static final Asset DEFAULT_WEB_XML = new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX + DEFAULT_WEB_XML_SUFFIX).getBytes());

    public static final Asset EMPTY_FACES_CONFIG_XML = new ByteArrayAsset(
            "<faces-config version=\"2.0\" xmlns=\"http://java.sun.com/xml/ns/javaee\"></faces-config>".getBytes());

    public static final Asset FACES_WEB_XML = new ByteArrayAsset((DEFAULT_WEB_XML_PREFIX
            + "<listener><listener-class>com.sun.faces.config.ConfigureListener</listener-class></listener> <context-param><param-name>jakarta.faces.DEFAULT_SUFFIX</param-name><param-value>.xhtml</param-value></context-param> <servlet><servlet-name>Faces Servlet</servlet-name><servlet-class>jakarta.faces.webapp.FacesServlet</servlet-class><load-on-startup>1</load-on-startup></servlet> <servlet-mapping><servlet-name>Faces Servlet</servlet-name><url-pattern>*.jsf</url-pattern></servlet-mapping> "
            + DEFAULT_WEB_XML_SUFFIX).getBytes());

    private Deployments() {
    }

    public static WebArchive baseDeployment(BeansXml beansXml, Asset webXml) {
        return ShrinkWrap.create(WebArchive.class).addAsWebInfResource(beansXml, "beans.xml").setWebXML(webXml);
    }

    public static WebArchive baseDeployment(BeansXml beansXml) {
        return baseDeployment(beansXml, DEFAULT_WEB_XML);
    }

    // BeanDiscoveryMode.ALL because many tests have 0 beans to discover and Weld would just skip initialization
    public static WebArchive baseDeployment() {
        return baseDeployment(new BeansXml(BeanDiscoveryMode.ALL), DEFAULT_WEB_XML);
    }

    // BeanDiscoveryMode.ALL because many tests have 0 beans to discover and Weld would just skip initialization
    public static WebArchive baseDeployment(Asset webXml) {
        return baseDeployment(new BeansXml(BeanDiscoveryMode.ALL), webXml);
    }

    public static String toListener(String listenerClassName) {
        return "<listener><listener-class>" + listenerClassName + "</listener-class></listener>";
    }

    public static String toServlet(String servletName, Class<?> servletClass) {
        return "<servlet><servlet-name>" + servletName + "</servlet-name><servlet-class>" + servletClass.getName() + "</servlet-class></servlet>";
    }

    public static String toServletMapping(String servletName, String urlPattern) {
        return "<servlet-mapping><servlet-name>" + servletName + "</servlet-name><url-pattern>" + urlPattern + "</url-pattern></servlet-mapping>";
    }

    public static String toServletAndMapping(String servletName, Class<?> servletClass, String urlPattern) {
        return toServlet(servletName, servletClass) + toServletMapping(servletName, urlPattern);
    }

    public static String toContextParam(String name, String value) {
        return "<context-param><param-name>" + name + "</param-name><param-value>" + value + "</param-value></context-param>";
    }

    /**
     * Inserts the extension into the end of the default web.xml (just before closing web-app)
     *
     * @param extension the extension
     * @return extended web xml
     */
    public static String extendDefaultWebXml(String extension) {
        return DEFAULT_WEB_XML_PREFIX + extension + DEFAULT_WEB_XML_SUFFIX;
    }

    public static <T extends Archive<?>> boolean isProcessorSkipped(T archive) {
        return archive.contains(Deployments.MARKER_SKIP_PROCESSOR);
    }

}
