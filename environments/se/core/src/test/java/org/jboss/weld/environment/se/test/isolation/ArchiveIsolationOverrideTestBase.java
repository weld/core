package org.jboss.weld.environment.se.test.isolation;

import org.jboss.arquillian.container.test.api.Deployer;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.weld.environment.se.Weld;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

/*
 * Base test structure for test classes that make assumptions about the value of {@link Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY}.
 * The implementing test class must contain a static method annotated @Deployment(managed = false).
 */
public abstract class ArchiveIsolationOverrideTestBase {

    private static final String isolationOriginalValue = System.getProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);

    @ArquillianResource
    private Deployer deployer;

    public abstract boolean isArchiveIsolationEnabled();

    @Before
    public void before() {
        System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, Boolean.toString(isArchiveIsolationEnabled()));
        deployer.deploy("_DEFAULT_");
    }

    @After
    public void after() {
        deployer.undeploy("_DEFAULT_");
    }

    @AfterClass
    public static void setIsolationBackToOriginal() {
        if (isolationOriginalValue == null) {
            System.clearProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY);
        } else {
            System.setProperty(Weld.ARCHIVE_ISOLATION_SYSTEM_PROPERTY, isolationOriginalValue);
        }
    }

}
