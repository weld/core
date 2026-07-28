module org.jboss.weld.environment.common {
    requires transitive org.jboss.weld.core;
    requires org.jboss.logging;
    requires static org.jboss.jandex;

    exports org.jboss.weld.environment to
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.environment.deployment to
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.environment.deployment.discovery to
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.environment.deployment.discovery.jandex to
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.environment.logging to
        org.jboss.weld.se, org.jboss.weld.servlet;
    exports org.jboss.weld.environment.util to
        org.jboss.weld.se, org.jboss.weld.servlet;
}
