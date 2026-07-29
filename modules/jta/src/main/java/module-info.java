/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
module org.jboss.weld.module.jta {
    requires org.jboss.weld.core;
    requires org.jboss.weld.spi;
    requires static jakarta.transaction;

    provides org.jboss.weld.module.WeldModule
        with org.jboss.weld.module.jta.WeldTransactionsModule;
}
