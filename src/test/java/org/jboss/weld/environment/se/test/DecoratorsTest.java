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

import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;

import org.jboss.weld.environment.se.Weld;
import org.jboss.weld.environment.se.WeldContainer;
import org.jboss.weld.environment.se.events.Shutdown;
import org.jboss.weld.environment.se.test.decorators.CarDoor;
import org.jboss.weld.environment.se.test.decorators.Door;
import org.jboss.weld.environment.se.test.decorators.CarDoorAlarm;
import org.jboss.weld.environment.se.test.decorators.HouseDoor;
import org.jboss.weld.environment.se.util.WeldManagerUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

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
        BeanManager manager = weld.getBeanManager();
       
        CarDoor carDoor = WeldManagerUtils.getInstanceByType(manager, CarDoor.class);
        Assert.assertNotNull(carDoor);

        // the car door is alarmed
        CarDoorAlarm.alarmActivated = false;
        Assert.assertFalse(CarDoorAlarm.alarmActivated);
        testDoor(carDoor);
        Assert.assertTrue(CarDoorAlarm.alarmActivated);

        HouseDoor houseDoor = WeldManagerUtils.getInstanceByType(manager, HouseDoor.class);
        Assert.assertNotNull(carDoor);

        // the house door is not alarmed
        CarDoorAlarm.alarmActivated = false;
        Assert.assertFalse(CarDoorAlarm.alarmActivated);
        testDoor(houseDoor);
        Assert.assertFalse(CarDoorAlarm.alarmActivated);

        shutdownManager(manager);
    }

    private void testDoor(Door door)
    {
        Assert.assertTrue(door.open());
        Assert.assertTrue(door.isOpen());
        Assert.assertFalse(door.close());
        Assert.assertFalse(door.isOpen());
        Assert.assertTrue(door.lock());
        Assert.assertTrue(door.isLocked());
        Assert.assertFalse(door.open());
        Assert.assertFalse(door.isOpen());
    }

    private void shutdownManager(BeanManager manager)
    {
        manager.fireEvent(manager, new ShutdownAnnotation());
    }

    private static class ShutdownAnnotation extends AnnotationLiteral<Shutdown>
    {

        public ShutdownAnnotation()
        {
        }
    }
}
