/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap.enablement;

/**
 * A beans.xml record that only affects a single bean archive. This can either be a disabler of a globally enabled record
 * (enabled == false) or an enabler of a record that has been given a default priority.
 *
 * @author Jozef Hartinger
 *
 */
class LocalEnablementRecord extends EnablementRecord {

    private final boolean enabled;

    public LocalEnablementRecord(String location, Class<?> enabledClass, boolean enabled) {
        super(location, enabledClass);
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
