/*
 * JBoss, Home of Professional Open Source
 * Copyright 2011, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.observers.validation;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Event;
import javax.enterprise.event.Observes;
import javax.inject.Inject;

/**
 * @author Mike Brock .
 */
@ApplicationScoped
public class HelloWorldService {
    private  Event<ResponseEvent> responseEvent;

    @Inject
    public HelloWorldService(Event<ResponseEvent> responseEvent) {
        this.responseEvent = responseEvent;
    }

    public void handleMessage(@Observes MessageEvent event) {
        System.out.println("Received Message: " + event.getMessage());
        responseEvent.fire(new ResponseEvent(event.getMessage()));
    }
}