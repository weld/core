package org.jboss.weld.environment.osgi.tests.framework;

import java.lang.reflect.Field;

import javax.inject.Inject;

import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.Configuration;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleException;

import static org.ops4j.pax.exam.CoreOptions.options;

@RunWith(JUnit4TestRunner.class)
public class LifeCycleTest {
    @Configuration
    public static Option[] configure() {
        return options(Environment.toCDIOSGiEnvironment());
    }

    @Inject
    private BundleContext context;

    @Test
    public void bundleLifeCycleTest() throws InterruptedException, BundleException, ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Environment.waitForEnvironment(context);

        Bundle bundle = context.installBundle("mvn:org.jboss.weld.osgi.tests/weld-osgi-life-cycle/1.2.0-SNAPSHOT");
        Environment.waitForState(bundle, Bundle.INSTALLED);

        Class flagFarm = bundle.loadClass("org.jboss.weld.osgi.tests.lifecycle.FlagFarm");
        Assert.assertNotNull("Unable to retreive FlagFarm class", flagFarm);
        Field osgiStartEntrance = flagFarm.getField("osgiStartEntrance");
        Assert.assertNotNull("Unable to retreive osgiStartEntrance field", osgiStartEntrance);
        Assert.assertEquals("Wrong value for field osgiStartEntrance", -1, osgiStartEntrance.getInt(flagFarm));
        Field osgiStartExit = flagFarm.getField("osgiStartExit");
        Assert.assertNotNull("Unable to retreive osgiStartExit field", osgiStartExit);
        Assert.assertEquals("Wrong value for field osgiStartExit", -1, osgiStartExit.getInt(flagFarm));
        Field osgiStopEntrance = flagFarm.getField("osgiStopEntrance");
        Assert.assertNotNull("Unable to retreive osgiStopEntrance field", osgiStopEntrance);
        Assert.assertEquals("Wrong value for field osgiStopEntrance", -1, osgiStopEntrance.getInt(flagFarm));
        Field osgiStopExit = flagFarm.getField("osgiStopExit");
        Assert.assertNotNull("Unable to retreive osgiStopExit field", osgiStopExit);
        Assert.assertEquals("Wrong value for field osgiStopExit", -1, osgiStopExit.getInt(flagFarm), -1);
        Field cdiStartEntrance = flagFarm.getField("cdiStartEntrance");
        Assert.assertNotNull("Unable to retreive cdiStartEntrance field", cdiStartEntrance);
        Assert.assertEquals("Wrong value for field cdiStartEntrance", -1, cdiStartEntrance.getInt(flagFarm));
        Field cdiStartExit = flagFarm.getField("cdiStartExit");
        Assert.assertNotNull("Unable to retreive cdiStartExit field", cdiStartExit);
        Assert.assertEquals("Wrong value for field cdiStartExit", -1, cdiStartExit.getInt(flagFarm));
        Field cdiStopEntrance = flagFarm.getField("cdiStopEntrance");
        Assert.assertNotNull("Unable to retreive cdiStopEntrance field", cdiStopEntrance);
        Assert.assertEquals("Wrong value for field cdiStopEntrance", -1, cdiStopEntrance.getInt(flagFarm));
        Field cdiStopExit = flagFarm.getField("cdiStopExit");
        Assert.assertNotNull("Unable to retreive cdiStopExit field", cdiStopExit);
        Assert.assertEquals("Wrong value for field cdiStopExit", -1, cdiStopExit.getInt(flagFarm));
        Field asynchronousStartedEntrance = flagFarm.getField("asynchronousStartedEntrance");
        Assert.assertNotNull("Unable to retreive asynchronousStartedEntrance field", asynchronousStartedEntrance);
        Assert.assertEquals("Wrong value for field asynchronousStartedEntrance", -1, asynchronousStartedEntrance.getInt(flagFarm));
        Field asynchronousStartedExit = flagFarm.getField("asynchronousStartedExit");
        Assert.assertNotNull("Unable to retreive asynchronousStartedExit field", asynchronousStartedExit);
        Assert.assertEquals("Wrong value for field asynchronousStartedExit", -1, asynchronousStartedExit.getInt(flagFarm));
        Field synchronousStartedEntrance = flagFarm.getField("synchronousStartedEntrance");
        Assert.assertNotNull("Unable to retreive synchronousStartedEntrance field", synchronousStartedEntrance);
        Assert.assertEquals("Wrong value for field synchronousStartedEntrance", -1, synchronousStartedEntrance.getInt(flagFarm));
        Field synchronousStartedExit = flagFarm.getField("synchronousStartedExit");
        Assert.assertNotNull("Unable to retreive synchronousStartedExit field", synchronousStartedExit);
        Assert.assertEquals("Wrong value for field synchronousStartedExit", -1, synchronousStartedExit.getInt(flagFarm));
        Field synchronousStoppingEntrance = flagFarm.getField("synchronousStoppingEntrance");
        Assert.assertNotNull("Unable to retreive synchronousStoppingEntrance field", synchronousStoppingEntrance);
        Assert.assertEquals("Wrong value for field synchronousStoppingEntrance", -1, synchronousStoppingEntrance.getInt(flagFarm));
        Field synchronousStoppingExit = flagFarm.getField("synchronousStoppingExit");
        Assert.assertNotNull("Unable to retreive synchronousStoppingExit field", synchronousStoppingExit);
        Assert.assertEquals("Wrong value for field synchronousStoppingExit", -1, synchronousStoppingExit.getInt(flagFarm));

        Field isCDIUnableInOSGiStart = flagFarm.getField("isCDIUnableInOSGiStart");
        Assert.assertNotNull("Unable to retreive isCDIUnableInOSGiStart field", isCDIUnableInOSGiStart);
        Assert.assertEquals("Wrong value for field isCDIUnableInOSGiStart", -1, isCDIUnableInOSGiStart.getInt(flagFarm));
        Field isCDIUnableInOSGiStop = flagFarm.getField("isCDIUnableInOSGiStop");
        Assert.assertNotNull("Unable to retreive isCDIUnableInOSGiStop field", isCDIUnableInOSGiStop);
        Assert.assertEquals("Wrong value for field isCDIUnableInOSGiStop", -1, isCDIUnableInOSGiStop.getInt(flagFarm));
        Field isCDIUnableInCDIStart = flagFarm.getField("isCDIUnableInCDIStart");
        Assert.assertNotNull("Unable to retreive isCDIUnableInCDIStart field", isCDIUnableInCDIStart);
        Assert.assertEquals("Wrong value for field isCDIUnableInCDIStart", -1, isCDIUnableInCDIStart.getInt(flagFarm));
        Field isCDIUnableInCDIStop = flagFarm.getField("isCDIUnableInCDIStop");
        Assert.assertNotNull("Unable to retreive isCDIUnableInCDIStop field", isCDIUnableInCDIStop);
        Assert.assertEquals("Wrong value for field isCDIUnableInCDIStop", -1, isCDIUnableInCDIStop.getInt(flagFarm));
        Field isOSGiUnableInOSGiStart = flagFarm.getField("isOSGiUnableInOSGiStart");
        Assert.assertNotNull("Unable to retreive isOSGiUnableInOSGiStart field", isOSGiUnableInOSGiStart);
        Assert.assertEquals("Wrong value for field isOSGiUnableInOSGiStart", -1, isOSGiUnableInOSGiStart.getInt(flagFarm));
        Field isOSGiUnableInOSGiStop = flagFarm.getField("isOSGiUnableInOSGiStop");
        Assert.assertNotNull("Unable to retreive isOSGiUnableInOSGiStop field", isOSGiUnableInOSGiStop);
        Assert.assertEquals("Wrong value for field isOSGiUnableInOSGiStop", -1, isOSGiUnableInOSGiStop.getInt(flagFarm));
        Field isOSGiUnableInCDIStart = flagFarm.getField("isOSGiUnableInCDIStart");
        Assert.assertNotNull("Unable to retreive isOSGiUnableInCDIStart field", isOSGiUnableInCDIStart);
        Assert.assertEquals("Wrong value for field isOSGiUnableInCDIStart", -1, isOSGiUnableInCDIStart.getInt(flagFarm));
        Field isOSGiUnableInCDIStop = flagFarm.getField("isOSGiUnableInCDIStop");
        Assert.assertNotNull("Unable to retreive isOSGiUnableInCDIStop field", isOSGiUnableInCDIStop);
        Assert.assertEquals("Wrong value for field isOSGiUnableInCDIStop", -1, isOSGiUnableInCDIStop.getInt(flagFarm));
        Field isOSGiForAutoPublishedServiceUnableInOSGiStart = flagFarm.getField("isOSGiForAutoPublishedServiceUnableInOSGiStart");
        Assert.assertNotNull("Unable to retreive isOSGiForAutoPublishedServiceUnableInOSGiStart field", isOSGiForAutoPublishedServiceUnableInOSGiStart);
        Assert.assertEquals("Wrong value for field isOSGiForAutoPublishedServiceUnableInOSGiStart", -1, isOSGiForAutoPublishedServiceUnableInOSGiStart.getInt(flagFarm));
        Field isOSGiForAutoPublishedServiceUnableInOSGiStop = flagFarm.getField("isOSGiForAutoPublishedServiceUnableInOSGiStop");
        Assert.assertNotNull("Unable to retreive isOSGiForAutoPublishedServiceUnableInOSGiStop field", isOSGiForAutoPublishedServiceUnableInOSGiStop);
        Assert.assertEquals("Wrong value for field isOSGiForAutoPublishedServiceUnableInOSGiStop", -1, isOSGiForAutoPublishedServiceUnableInOSGiStop.getInt(flagFarm));
        Field isOSGiForAutoPublishedServiceUnableInCDIStart = flagFarm.getField("isOSGiForAutoPublishedServiceUnableInCDIStart");
        Assert.assertNotNull("Unable to retreive isOSGiForAutoPublishedServiceUnableInCDIStart field", isOSGiForAutoPublishedServiceUnableInCDIStart);
        Assert.assertEquals("Wrong value for field isOSGiForAutoPublishedServiceUnableInCDIStart", -1, isOSGiForAutoPublishedServiceUnableInCDIStart.getInt(flagFarm));
        Field isOSGiForAutoPublishedServiceUnableInCDIStop = flagFarm.getField("isOSGiForAutoPublishedServiceUnableInCDIStop");
        Assert.assertNotNull("Unable to retreive isOSGiForAutoPublishedServiceUnableInCDIStop field", isOSGiForAutoPublishedServiceUnableInCDIStop);
        Assert.assertEquals("Wrong value for field isOSGiForAutoPublishedServiceUnableInCDIStop", -1, isOSGiForAutoPublishedServiceUnableInCDIStop.getInt(flagFarm));
        Field isWeldOSGiUnableInOSGiStart = flagFarm.getField("isWeldOSGiUnableInOSGiStart");
        Assert.assertNotNull("Unable to retreive isWeldOSGiUnableInOSGiStart field", isWeldOSGiUnableInOSGiStart);
        Assert.assertEquals("Wrong value for field isWeldOSGiUnableInOSGiStart", -1, isWeldOSGiUnableInOSGiStart.getInt(flagFarm));
        Field isWeldOSGiUnableInOSGiStop = flagFarm.getField("isWeldOSGiUnableInOSGiStop");
        Assert.assertNotNull("Unable to retreive isWeldOSGiUnableInOSGiStop field", isWeldOSGiUnableInOSGiStop);
        Assert.assertEquals("Wrong value for field isWeldOSGiUnableInOSGiStop", -1, isWeldOSGiUnableInOSGiStop.getInt(flagFarm));
        Field isWeldOSGiUnableInCDIStart = flagFarm.getField("isWeldOSGiUnableInCDIStart");
        Assert.assertNotNull("Unable to retreive isWeldOSGiUnableInCDIStart field", isWeldOSGiUnableInCDIStart);
        Assert.assertEquals("Wrong value for field isWeldOSGiUnableInCDIStart", -1, isWeldOSGiUnableInCDIStart.getInt(flagFarm));
        Field isWeldOSGiUnableInCDIStop = flagFarm.getField("isWeldOSGiUnableInCDIStop");
        Assert.assertNotNull("Unable to retreive isWeldOSGiUnableInCDIStop field", isWeldOSGiUnableInCDIStop);
        Assert.assertEquals("Wrong value for field isWeldOSGiUnableInCDIStop", -1, isWeldOSGiUnableInCDIStop.getInt(flagFarm));
        Field isWeldOSGiForAutoPublishedServiceUnableInOSGiStart = flagFarm.getField("isWeldOSGiForAutoPublishedServiceUnableInOSGiStart");
        Assert.assertNotNull("Unable to retreive isWeldOSGiForAutoPublishedServiceUnableInOSGiStart field", isWeldOSGiForAutoPublishedServiceUnableInOSGiStart);
        Assert.assertEquals("Wrong value for field isWeldOSGiForAutoPublishedServiceUnableInOSGiStart", -1, isWeldOSGiForAutoPublishedServiceUnableInOSGiStart.getInt(flagFarm));
        Field isWeldOSGiForAutoPublishedServiceUnableInOSGiStop = flagFarm.getField("isWeldOSGiForAutoPublishedServiceUnableInOSGiStop");
        Assert.assertNotNull("Unable to retreive isWeldOSGiForAutoPublishedServiceUnableInOSGiStop field", isWeldOSGiForAutoPublishedServiceUnableInOSGiStop);
        Assert.assertEquals("Wrong value for field isWeldOSGiForAutoPublishedServiceUnableInOSGiStop", -1, isWeldOSGiForAutoPublishedServiceUnableInOSGiStop.getInt(flagFarm));
        Field isWeldOSGiForAutoPublishedServiceUnableInCDIStart = flagFarm.getField("isWeldOSGiForAutoPublishedServiceUnableInCDIStart");
        Assert.assertNotNull("Unable to retreive isWeldOSGiForAutoPublishedServiceUnableInCDIStart field", isWeldOSGiForAutoPublishedServiceUnableInCDIStart);
        Assert.assertEquals("Wrong value for field isWeldOSGiForAutoPublishedServiceUnableInCDIStart", -1, isWeldOSGiForAutoPublishedServiceUnableInCDIStart.getInt(flagFarm));
        Field isWeldOSGiForAutoPublishedServiceUnableInCDIStop = flagFarm.getField("isWeldOSGiForAutoPublishedServiceUnableInCDIStop");
        Assert.assertNotNull("Unable to retreive isWeldOSGiForAutoPublishedServiceUnableInCDIStop field", isWeldOSGiForAutoPublishedServiceUnableInCDIStop);
        Assert.assertEquals("Wrong value for field isWeldOSGiForAutoPublishedServiceUnableInCDIStop", -1, isWeldOSGiForAutoPublishedServiceUnableInCDIStop.getInt(flagFarm));

        bundle.start();
        Environment.waitForState(bundle, Bundle.ACTIVE);
        Assert.assertEquals("Wrong value (2) for field osgiStartEntrance", 0, osgiStartEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field osgiStartExit", 1, osgiStartExit.getInt(flagFarm));
        Assert.assertTrue("Wrong value (2) for field synchronousStartedEntrance", synchronousStartedEntrance.getInt(flagFarm) == 2 || synchronousStartedEntrance.getInt(flagFarm) == 4);
        Assert.assertTrue("Wrong value (2) for field cdiStartEntrance", cdiStartEntrance.getInt(flagFarm) == 2 || cdiStartEntrance.getInt(flagFarm) == 4);
        Assert.assertTrue("Wrong value (2) for field synchronousStartedExit", synchronousStartedExit.getInt(flagFarm) == 3 || synchronousStartedExit.getInt(flagFarm) == 5);
        Assert.assertTrue("Wrong value (2) for field cdiStartExit", synchronousStartedExit.getInt(flagFarm) == 3 || synchronousStartedExit.getInt(flagFarm) == 5);
        Assert.assertEquals("Wrong value (2) for field asynchronousStartedEntrance", 6, asynchronousStartedEntrance.getInt(flagFarm));

        Assert.assertEquals("Wrong value (2) for field osgiStopEntrance", -1, osgiStopEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field osgiStopExit", -1, osgiStopExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field cdiStopEntrance", -1, cdiStopEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field cdiStopExit", -1, cdiStopExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field asynchronousStartedExit", -1, asynchronousStartedExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field synchronousStoppingEntrance", -1, synchronousStoppingEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field synchronousStoppingExit", -1, synchronousStoppingExit.getInt(flagFarm));

        bundle.stop();
        Environment.waitForState(bundle, Bundle.RESOLVED);
        Assert.assertEquals("Wrong value (3) for field osgiStartEntrance", 0, osgiStartEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (3) for field osgiStartExit", 1, osgiStartExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (3) for field synchronousStartedEntrance", 2, synchronousStartedEntrance.getInt(flagFarm));
        Assert.assertTrue("Wrong value (3) for field cdiStartEntrance", cdiStartEntrance.getInt(flagFarm) == 3 || cdiStartEntrance.getInt(flagFarm) == 4);
        Assert.assertTrue("Wrong value (3) for field synchronousStartedExit", synchronousStartedExit.getInt(flagFarm) == 3 || synchronousStartedExit.getInt(flagFarm) == 4);
        Assert.assertEquals("Wrong value (3) for field cdiStartExit", 5, cdiStartExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (3) for field asynchronousStartedEntrance", 6, asynchronousStartedEntrance.getInt(flagFarm));

        Assert.assertTrue("Wrong value (3) for field synchronousStoppingEntrance", synchronousStoppingEntrance.getInt(flagFarm) == 7 || synchronousStoppingEntrance.getInt(flagFarm) == 9);
        Assert.assertTrue("Wrong value (3) for field cdiStopEntrance", cdiStopEntrance.getInt(flagFarm) == 7 || cdiStopEntrance.getInt(flagFarm) == 9);
        Assert.assertTrue("Wrong value (3) for field synchronousStoppingExit", synchronousStoppingExit.getInt(flagFarm) == 8 || synchronousStoppingExit.getInt(flagFarm) == 10);
        Assert.assertTrue("Wrong value (3) for field cdiStopExit", cdiStopExit.getInt(flagFarm) == 8 || cdiStopExit.getInt(flagFarm) == 10);
        Assert.assertEquals("Wrong value (3) for field osgiStopEntrance", 11, osgiStopEntrance.getInt(flagFarm));
        Assert.assertEquals("Wrong value (3) for field asynchronousStartedExit", 12, asynchronousStartedExit.getInt(flagFarm));
        Assert.assertEquals("Wrong value (3) for field osgiStopExit", 13, osgiStopExit.getInt(flagFarm));

        Assert.assertEquals("Wrong value (2) for field isCDIUnableInOSGiStart", 0, isCDIUnableInOSGiStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isCDIUnableInOSGiStop", 0, isCDIUnableInOSGiStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isCDIUnableInCDIStart", 1, isCDIUnableInCDIStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isCDIUnableInCDIStop", 1, isCDIUnableInCDIStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiUnableInOSGiStart", 2, isOSGiUnableInOSGiStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiUnableInOSGiStop", 2, isOSGiUnableInOSGiStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiUnableInCDIStart", 2, isOSGiUnableInCDIStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiUnableInCDIStop", 2, isOSGiUnableInCDIStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiForAutoPublishedServiceUnableInOSGiStart", 0, isOSGiForAutoPublishedServiceUnableInOSGiStart.getInt(flagFarm));
//      Assert.assertEquals("Wrong value (2) for field isOSGiForAutoPublishedServiceUnableInOSGiStop", 0, isOSGiForAutoPublishedServiceUnableInOSGiStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiForAutoPublishedServiceUnableInCDIStart", 2, isOSGiForAutoPublishedServiceUnableInCDIStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isOSGiForAutoPublishedServiceUnableInCDIStop", 2, isOSGiForAutoPublishedServiceUnableInCDIStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiUnableInOSGiStart", -1, isWeldOSGiUnableInOSGiStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiUnableInOSGiStop", -1, isWeldOSGiUnableInOSGiStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiUnableInCDIStart", -1, isWeldOSGiUnableInCDIStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiUnableInCDIStop", -1, isWeldOSGiUnableInCDIStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiForAutoPublishedServiceUnableInOSGiStart", -1, isWeldOSGiForAutoPublishedServiceUnableInOSGiStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiForAutoPublishedServiceUnableInOSGiStop", -1, isWeldOSGiForAutoPublishedServiceUnableInOSGiStop.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiForAutoPublishedServiceUnableInCDIStart", -1, isWeldOSGiForAutoPublishedServiceUnableInCDIStart.getInt(flagFarm));
        Assert.assertEquals("Wrong value (2) for field isWeldOSGiForAutoPublishedServiceUnableInCDIStop", -1, isWeldOSGiForAutoPublishedServiceUnableInCDIStop.getInt(flagFarm));

    }

}