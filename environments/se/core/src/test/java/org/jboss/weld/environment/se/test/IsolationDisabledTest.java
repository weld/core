package org.jboss.weld.environment.se.test;

import org.jboss.weld.environment.se.Weld;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/*
 * Base test structure for the tests that needs to disable the {@link Weld.COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY} property for all the tests.
 */
public class IsolationDisabledTest {

    private static final String isolationDefaultValue = System.getProperty(Weld.COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY);

    @BeforeClass
    public static void setIsolationToTrue() {
        System.setProperty(Weld.COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY, "true");
    }

    @AfterClass
    public static void setIsolationBackToDefault() {
        System.setProperty(Weld.COMPOSITE_ARCHIVE_ENABLEMENT_SYSTEM_PROPERTY, isolationDefaultValue == null ? "" : isolationDefaultValue);
    }

}
