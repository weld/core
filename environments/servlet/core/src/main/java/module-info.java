module org.jboss.weld.servlet {
    requires transitive org.jboss.weld.core;
    requires org.jboss.weld.environment.common;
    requires org.jboss.weld.lite.extension.translator;
    requires org.jboss.logging;
    requires static org.jboss.logging.annotations;
    requires jakarta.servlet;
    requires static org.apache.tomcat.catalina;

    opens org.jboss.weld.environment.servlet.logging to org.jboss.logging;

    exports org.jboss.weld.environment.servlet;

    provides jakarta.servlet.ServletContainerInitializer
        with org.jboss.weld.environment.servlet.EnhancedListener;
    provides jakarta.enterprise.inject.spi.CDIProvider
        with org.jboss.weld.environment.servlet.WeldProvider;
}
