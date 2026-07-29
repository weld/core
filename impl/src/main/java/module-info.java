module org.jboss.weld.core {
    requires transitive org.jboss.weld.spi;
    requires org.jboss.classfilewriter;
    requires org.jboss.logging;
    requires java.logging;
    requires static org.jboss.logging.annotations;
    requires static jakarta.cdi.el;
    requires static jakarta.el;
    requires static jakarta.persistence;
    requires static java.naming;
    requires static jdk.unsupported;

    exports org.jboss.weld to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jta, org.jboss.weld.module.jsf,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.module;

    // Proxy packages — exported unqualified because proxy bytecode defined in
    // user modules via Lookup.defineClass() references these types and the JVM
    // resolves supertypes eagerly at class-linking time
    exports org.jboss.weld.bean.proxy;
    exports org.jboss.weld.bean.proxy.util;
    exports org.jboss.weld.interceptor.proxy;
    exports org.jboss.weld.interceptor.util.proxy;

    // Qualified exports for sibling Weld modules
    exports org.jboss.weld.annotated.enhanced to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.annotated.enhanced.jlr to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.annotated.slim to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.bean to
        org.jboss.weld.module.ejb, org.jboss.weld.lite.extension.translator;
    exports org.jboss.weld.bean.attributes to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.bean.builtin to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.bean.builtin.ee to
        org.jboss.weld.module.jta;
    exports org.jboss.weld.bean.interceptor to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.bootstrap to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.bootstrap.events to
        org.jboss.weld.lite.extension.translator, org.jboss.weld.se;
    exports org.jboss.weld.config to
        org.jboss.weld.module.web, org.jboss.weld.environment.common,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.contexts to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.se;
    exports org.jboss.weld.contexts.beanstore to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.se;
    exports org.jboss.weld.contexts.cache to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web;
    exports org.jboss.weld.event to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jta, org.jboss.weld.se;
    exports org.jboss.weld.exceptions to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.lite.extension.translator, org.jboss.weld.environment.common,
        org.jboss.weld.se;
    exports org.jboss.weld.executor to
        org.jboss.weld.se;
    exports org.jboss.weld.injection to
        org.jboss.weld.module.ejb, org.jboss.weld.module.jta;
    exports org.jboss.weld.injection.producer to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.interceptor.spi.model to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.invokable to
        org.jboss.weld.lite.extension.translator;
    exports org.jboss.weld.literal to
        org.jboss.weld.environment.common;
    exports org.jboss.weld.logging to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jta, org.jboss.weld.lite.extension.translator;
    exports org.jboss.weld.manager to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jta, org.jboss.weld.module.jsf,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.metadata to
        org.jboss.weld.se;
    exports org.jboss.weld.metadata.cache to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.resolution to
        org.jboss.weld.module.jta;
    exports org.jboss.weld.resources to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jsf, org.jboss.weld.environment.common,
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.serialization to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web;
    exports org.jboss.weld.util to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.module.jta, org.jboss.weld.module.jsf,
        org.jboss.weld.environment.common, org.jboss.weld.se,
        org.jboss.weld.servlet;
    exports org.jboss.weld.util.annotated to
        org.jboss.weld.se;
    exports org.jboss.weld.util.bytecode to
        org.jboss.weld.module.ejb;
    exports org.jboss.weld.util.cache to
        org.jboss.weld.environment.common, org.jboss.weld.se,
        org.jboss.weld.servlet;
    exports org.jboss.weld.util.collections to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.environment.common, org.jboss.weld.se,
        org.jboss.weld.servlet;
    exports org.jboss.weld.util.reflection to
        org.jboss.weld.module.ejb, org.jboss.weld.module.web,
        org.jboss.weld.lite.extension.translator, org.jboss.weld.se,
        org.jboss.weld.servlet;
    exports org.jboss.weld.xml to
        org.jboss.weld.environment.common;

    opens org.jboss.weld.logging to org.jboss.logging;

    uses org.jboss.weld.module.WeldModule;
    uses jakarta.enterprise.inject.spi.Extension;
    uses jakarta.enterprise.inject.build.compatible.spi.BuildCompatibleExtension;
}
