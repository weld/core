/*
 * Copyright The Weld Authors
 * SPDX-License-Identifier: Apache-2.0
 */
module weld.tests.jpms {
    requires org.jboss.weld.se;
    opens org.jboss.weld.tests.jpms.beans to org.jboss.weld.se;
}
