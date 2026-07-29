/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
module org.jboss.weld.module.web {
    requires org.jboss.weld.core;
    requires org.jboss.weld.spi;
    requires org.jboss.logging;
    requires static org.jboss.logging.annotations;
    requires static jakarta.cdi.el;
    requires static jakarta.el;
    requires static jakarta.servlet;

    opens org.jboss.weld.module.web.logging to org.jboss.logging;

    provides org.jboss.weld.module.WeldModule
        with org.jboss.weld.module.web.WeldWebModule;
}
