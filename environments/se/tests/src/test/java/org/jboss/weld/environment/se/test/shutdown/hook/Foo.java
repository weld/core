/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.shutdown.hook;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class Foo {

    static final String UNDERTOW_TEST_SERVER_CLASS = "org.jboss.weld.environment.se.test.shutdown.hook.UndertowTestServer";

    static final int UNDERTOW_PORT = 8989;

    static final AtomicBoolean IS_FOO_DESTROYED = new AtomicBoolean(false);

    public void ping() {
    }

    @PostConstruct
    public void init() {
    }

    @PreDestroy
    public void destroy() {
        try (InputStream in = new URL("http", "localhost", UNDERTOW_PORT, "test").openStream()) {
            // Sending HTTP GET request
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
