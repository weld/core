/**
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc. and/or its affiliates, and individual
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.weld.environment.se.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.jboss.weld.environment.se.ShutdownManager;
import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.test.decorators.AbstractDoor;
import org.jboss.weld.environment.se.test.decorators.CarDoor;
import org.jboss.weld.environment.se.test.decorators.CarDoorAlarm;
import org.jboss.weld.environment.se.test.decorators.HouseDoor;
import org.junit.Test;

/**
 * 
 * @author Peter Royle
 */
public class DecoratorsTest
{

   /**
    * Test that decorators work as expected in SE.
    */
   @Test
   public void testDecorators()
   {
      WeldContainer weld = new Weld().initialize();

      CarDoor carDoor = weld.instance().select(CarDoor.class).get();
      assertNotNull(carDoor);

      // the car door is alarmed
      CarDoorAlarm.alarmActivated = false;
      assertFalse(CarDoorAlarm.alarmActivated);
      testDoor(carDoor);
      assertTrue(CarDoorAlarm.alarmActivated);

      HouseDoor houseDoor = weld.instance().select(HouseDoor.class).get();
      assertNotNull(carDoor);

      // the house door is not alarmed
      CarDoorAlarm.alarmActivated = false;
      assertFalse(CarDoorAlarm.alarmActivated);
      testDoor(houseDoor);
      assertFalse(CarDoorAlarm.alarmActivated);

      shutdownManager(weld);
   }

   private void testDoor(AbstractDoor door)
   {
      assertTrue(door.open());
      assertTrue(door.isOpen());
      assertFalse(door.close());
      assertFalse(door.isOpen());
      assertTrue(door.lock());
      assertTrue(door.isLocked());
      assertFalse(door.open());
      assertFalse(door.isOpen());
   }

   private void shutdownManager(WeldContainer weld)
   {
      ShutdownManager shutdownManager = weld.instance().select(ShutdownManager.class).get();
      shutdownManager.shutdown();
   }
}
