/*
 * JBoss, Home of Professional Open Source
 * Copyright 2017, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.event.enhanced;

import java.util.List;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Observes;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ApplicationScoped
public class ObservingBean {

    private boolean fooObserved = false;
    private boolean barObserved = false;
    private boolean listObserved = false;
    private boolean listObjectObserved = false;
    private boolean someInterfaceObserved = false;
    private boolean someOtherBeanObserved = false;
    private boolean someTypedBeanObserved = false;

    /**
     * @return the barObserved
     */
    public boolean isBarObserved() {
        return barObserved;
    }

    /**
     * @return the fooObserved
     */
    public boolean isFooObserved() {
        return fooObserved;
    }

    /**
     * @return the listObjectObserved
     */
    public boolean isListObjectObserved() {
        return listObjectObserved;
    }

    /**
     * @return the listObserved
     */
    public boolean isListObserved() {
        return listObserved;
    }

    /**
     * @return the someInterfaceObserved
     */
    public boolean isSomeInterfaceObserved() {
        return someInterfaceObserved;
    }

    /**
     * @return the someOtherBeanObserved
     */
    public boolean isSomeOtherBeanObserved() {
        return someOtherBeanObserved;
    }

    /**
     * @return the someTypedBeanObserved
     */
    public boolean isSomeTypedBeanObserved() {
        return someTypedBeanObserved;
    }

    public void reset() {
        // set all booleans to false
        fooObserved = false;
        barObserved = false;
        listObserved = false;
        listObjectObserved = false;
        someInterfaceObserved = false;
        someOtherBeanObserved = false;
        someTypedBeanObserved = false;
    }

    public void observeFoo(@Observes Foo foo) {
        fooObserved = true;
    }

    public void observeBar(@Observes @Dubious Bar bar) {
        barObserved = true;
    }

    public void observeList(@Observes List<Object> list) {
        listObjectObserved = true;
    }

    public void observeListRaw(@Observes List list) {
        listObserved = true;
    }

    public void observeSomeInterface(@Observes SomeInterface some) {
        someInterfaceObserved = true;
    }

    public void observeSomeBean(@Observes SomeOtherBean bean) {
        someOtherBeanObserved = true;
    }
}
