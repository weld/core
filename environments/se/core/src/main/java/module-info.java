/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
module org.jboss.weld.se {
    requires transitive org.jboss.weld.core;
    requires org.jboss.weld.environment.common;
    requires org.jboss.weld.lite.extension.translator;
    requires org.jboss.logging;
    requires static org.jboss.logging.annotations;

    opens org.jboss.weld.environment.se.logging to org.jboss.logging;

    exports org.jboss.weld.environment.se;
    exports org.jboss.weld.environment.se.bindings;
    exports org.jboss.weld.environment.se.contexts;
    exports org.jboss.weld.environment.se.events;

    provides jakarta.enterprise.inject.se.SeContainerInitializer
        with org.jboss.weld.environment.se.Weld;
    provides jakarta.enterprise.inject.spi.CDIProvider
        with org.jboss.weld.environment.se.WeldSEProvider;
    provides jakarta.enterprise.inject.spi.Extension
        with org.jboss.weld.environment.se.WeldSEBeanRegistrant;
}
