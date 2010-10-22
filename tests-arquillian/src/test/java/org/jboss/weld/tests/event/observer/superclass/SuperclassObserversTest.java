/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat, Inc., and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
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
package org.jboss.weld.tests.event.observer.superclass;

import javax.enterprise.event.Event;
import javax.inject.Inject;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(Arquillian.class)
public class SuperclassObserversTest {
	@Deployment
	public static Archive<?> deploy() {
		return ShrinkWrap.create(BeanArchive.class).addPackage(
				SuperclassObserversTest.class.getPackage());
	}

	@Inject
	Event<TestEvent> event;

	@Inject
	private TestObserver observer;

	@Inject
	@Disabled
	private DisabledTestObserver disabled;

	@Inject
	@ReEnabled
	private ReEnabledTestObserver reenabled;

	@Test
	public void testObserverMethodFromSuperclassInvoked() {
		observer.reset();

		assert observer.getTestEvent() == null;
		event.fire(new TestEvent());
		assert observer.getTestEvent() != null;
	}

	@Test
	public void testObserverMethodOnOverridesWithoutAnnotNotInvoked() {
		disabled.reset();

		assert disabled.getTestEvent() == null;
		event.fire(new TestEvent());
		assert disabled.getTestEvent() == null;
	}

	@Test
	public void testObserverMethodOnOverridesWithAnnotAreInvoked() {
		reenabled.reset();

		assert reenabled.getTestEvent() == null;
		event.fire(new TestEvent());
		assert reenabled.getTestEvent() != null;
	}

}