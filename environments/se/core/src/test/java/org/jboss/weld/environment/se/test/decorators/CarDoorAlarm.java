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

import javax.decorator.Decorator;
import javax.decorator.Delegate;
import javax.inject.Inject;

/**
 * Decorates a door so that an alarm is activated if the door is attempted
 * to be opened while it is locked.
 *
 * @author Peter Royle
 */
@Decorator
public abstract class CarDoorAlarm implements Door {

    @Inject
    @Delegate
    CarDoor door;
    public static boolean alarmActivated = false;

    public boolean open() {
        if (door.isLocked()) {
            alarmActivated = true;
            System.out.println("AWOOGA"); // (there's an emergency going on)
        }
        return door.open(); // lock will still be used
    }
}
