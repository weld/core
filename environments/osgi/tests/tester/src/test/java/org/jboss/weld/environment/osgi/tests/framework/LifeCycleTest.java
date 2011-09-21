package org.jboss.weld.environment.osgi.tests.framework;

import java.lang.reflect.Field;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.ops4j.pax.exam.junit.Configuration;
import org.jboss.weld.environment.osgi.tests.util.Environment;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ops4j.pax.exam.Option;
import org.ops4j.pax.exam.junit.JUnit4TestRunner;

import static org.ops4j.pax.exam.CoreOptions.options;
import org.osgi.framework.BundleException;

@RunWith(JUnit4TestRunner.class)
public class LifeCycleTest
{
   @Configuration
   public static Option[] configure()
   {
      return options(Environment.CDIOSGiEnvironment());
   }

   @Test
   //@Ignore
   public void bundleLifeCycleTest(BundleContext context) throws InterruptedException, BundleException, ClassNotFoundException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException
   {
      Environment.waitForEnvironment(context);

      Bundle bundle = context.installBundle("mvn:org.jboss.weld.osgi.tests/weld-osgi-life-cycle/1.1.3-SNAPSHOT");
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

      bundle.start();
      Environment.waitForState(bundle, Bundle.ACTIVE);
      Assert.assertEquals("Wrong value (2) for field osgiStartEntrance", 0, osgiStartEntrance.getInt(flagFarm));
      Assert.assertEquals("Wrong value (2) for field osgiStartExit", 1, osgiStartExit.getInt(flagFarm));
      Assert.assertTrue("Wrong value (2) for field synchronousStartedEntrance", synchronousStartedEntrance.getInt(flagFarm) == 2 ||  synchronousStartedEntrance.getInt(flagFarm) == 4);
      Assert.assertTrue("Wrong value (2) for field cdiStartEntrance", cdiStartEntrance.getInt(flagFarm) == 2 ||  cdiStartEntrance.getInt(flagFarm) == 4);
      Assert.assertTrue("Wrong value (2) for field synchronousStartedExit", synchronousStartedExit.getInt(flagFarm) == 3 ||  synchronousStartedExit.getInt(flagFarm) == 5);
      Assert.assertTrue("Wrong value (2) for field cdiStartExit", synchronousStartedExit.getInt(flagFarm) == 3 ||  synchronousStartedExit.getInt(flagFarm) == 5);
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
      Assert.assertEquals("Wrong value (2) for field osgiStartEntrance", 0, osgiStartEntrance.getInt(flagFarm));
      Assert.assertEquals("Wrong value (2) for field osgiStartExit", 1, osgiStartExit.getInt(flagFarm));
      Assert.assertEquals("Wrong value (2) for field synchronousStartedEntrance", 2, synchronousStartedEntrance.getInt(flagFarm));
      Assert.assertTrue("Wrong value (2) for field cdiStartEntrance", cdiStartEntrance.getInt(flagFarm) == 3 ||  cdiStartEntrance.getInt(flagFarm) == 4);
      Assert.assertTrue("Wrong value (2) for field synchronousStartedExit", synchronousStartedExit.getInt(flagFarm) == 3 ||  synchronousStartedExit.getInt(flagFarm) == 4);
      Assert.assertEquals("Wrong value (2) for field cdiStartExit", 5, cdiStartExit.getInt(flagFarm));
      Assert.assertEquals("Wrong value (2) for field asynchronousStartedEntrance", 6, asynchronousStartedEntrance.getInt(flagFarm));

      Assert.assertTrue("Wrong value (3) for field synchronousStoppingEntrance", synchronousStoppingEntrance.getInt(flagFarm) == 7 ||  synchronousStoppingEntrance.getInt(flagFarm) == 9);
      Assert.assertTrue("Wrong value (3) for field cdiStopEntrance", cdiStopEntrance.getInt(flagFarm) == 7 ||  cdiStopEntrance.getInt(flagFarm) == 9);
      Assert.assertTrue("Wrong value (3) for field synchronousStoppingExit", synchronousStoppingExit.getInt(flagFarm) == 8 ||  synchronousStoppingExit.getInt(flagFarm) == 10);
      Assert.assertTrue("Wrong value (3) for field cdiStopExit", cdiStopExit.getInt(flagFarm) == 8 ||  cdiStopExit.getInt(flagFarm) == 10);
      Assert.assertEquals("Wrong value (3) for field osgiStopEntrance", 11, osgiStopEntrance.getInt(flagFarm));
      Assert.assertEquals("Wrong value (3) for field asynchronousStartedExit", 12, asynchronousStartedExit.getInt(flagFarm));
      Assert.assertEquals("Wrong value (3) for field osgiStopExit", 13, osgiStopExit.getInt(flagFarm));

   }

}
