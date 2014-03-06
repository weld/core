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
package org.jboss.weld.environment.se.test.decorators;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import javax.inject.Inject;

import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * @author Peter Royle
 */
@RunWith(Arquillian.class)
public class DecoratorsTest {

    @Inject
    private CarDoor carDoor;

    @Inject
    private HouseDoor houseDoor;

    @Deployment
    public static Archive<?> getDeployment() {
        return ShrinkWrap.create(BeanArchive.class).decorate(CarDoorAlarm.class).addPackage(DecoratorsTest.class.getPackage());
    }

    /**
     * Test that decorators work as expected in SE.
     */
    @Test
    public void testDecorators() {

        assertNotNull(carDoor);

        // the car door is alarmed
        CarDoorAlarm.alarmActivated = false;
        assertFalse(CarDoorAlarm.alarmActivated);
        testDoor(carDoor);
        assertTrue(CarDoorAlarm.alarmActivated);

        assertNotNull(carDoor);

        // the house door is not alarmed
        CarDoorAlarm.alarmActivated = false;
        assertFalse(CarDoorAlarm.alarmActivated);
        testDoor(houseDoor);
        assertFalse(CarDoorAlarm.alarmActivated);
    }

    private void testDoor(AbstractDoor door) {
        assertTrue(door.open());
        assertTrue(door.isOpen());
        assertFalse(door.close());
        assertFalse(door.isOpen());
        assertTrue(door.lock());
        assertTrue(door.isLocked());
        assertFalse(door.open());
        assertFalse(door.isOpen());
    }

}
