package org.jboss.weld.util.servlet;

import javax.servlet.ServletContext;

import org.jboss.weld.logging.ServletLogger;
import org.jboss.weld.manager.BeanManagerImpl;
import org.jboss.weld.servlet.spi.HttpContextActivationFilter;
import org.jboss.weld.servlet.spi.helpers.AcceptingHttpContextActivationFilter;
import org.jboss.weld.servlet.spi.helpers.RegexHttpContextActivationFilter;

/**
 * Utilities for working with Servlet API.
 *
 * @author Jozef Hartinger
 *
 */
public class ServletUtils {

    public static final String CONTEXT_MAPPING = "org.jboss.weld.context.mapping";

    private ServletUtils() {
    }

    /**
     * Returns the right {@link HttpContextActivationFilter}. If one is set through the SPI it has precedence. Otherwise, if a mapping is set using web.xml, a
     * new {@link RegexHttpContextActivationFilter} is constructed and returned. By default, {@link AcceptingHttpContextActivationFilter} is used.
     *
     * @param manager
     * @param context
     * @return
     */
    public static HttpContextActivationFilter getContextActivationFilter(BeanManagerImpl manager, ServletContext context) {
        HttpContextActivationFilter filter = manager.getServices().get(HttpContextActivationFilter.class);
        final String pattern = context.getInitParameter(CONTEXT_MAPPING);
        if (filter == AcceptingHttpContextActivationFilter.INSTANCE) {
            // SPI has precedence. If a filter was not set through SPI let's see if a mapping is set in web.xml
            if (pattern != null) {
                return new RegexHttpContextActivationFilter(pattern);
            }
        } else if (pattern != null) {
            ServletLogger.LOG.webXmlMappingPatternIgnored(pattern);
        }
        return filter;
    }
}
