module org.jboss.weld.lite.extension.translator {
    requires org.jboss.weld.core;
    requires org.jboss.weld.spi;
    requires org.jboss.logging;
    requires static org.jboss.logging.annotations;

    opens org.jboss.weld.lite.extension.translator.logging to org.jboss.logging;

    exports org.jboss.weld.lite.extension.translator to
        org.jboss.weld.se, org.jboss.weld.servlet;

    uses jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;

    provides jakarta.enterprise.inject.build.compatible.spi.BuildServices
        with org.jboss.weld.lite.extension.translator.BuildServicesImpl;
}
