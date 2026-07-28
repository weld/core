module org.jboss.weld.core {
    requires transitive org.jboss.weld.spi;
    requires org.jboss.classfilewriter;
    requires org.jboss.logging;
    requires static org.jboss.logging.annotations;
    requires static jakarta.el;
    requires static jakarta.persistence;
    requires static java.naming;
    requires static jdk.unsupported;

    exports org.jboss.weld.module;

    uses org.jboss.weld.module.WeldModule;
    uses jakarta.enterprise.inject.spi.Extension;
    uses jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
}
