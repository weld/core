/*
 * JBoss, Home of Professional Open Source
 * Copyright 2016, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.tests.producer.disposer.decorated;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Disposes;
import jakarta.enterprise.inject.Produces;

import org.jboss.weld.test.util.ActionSequence;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
@ApplicationScoped
public class PackagePrivateWorker implements Worker {

    private int hiddenField = 0;

    @Override
    public void doStuff() {
        ActionSequence.addAction(PackagePrivateWorker.class.getName());
        hiddenField++;
    }

    @Produces
    @Lazy
    PackagePrivateWorker producePackagePrivate() {
        ActionSequence.addAction(PackagePrivateWorker.class.getName() + "-" + hiddenField);
        return new PackagePrivateWorker();
    }

    void dispose(@Disposes @Lazy PackagePrivateWorker worker) {
        ActionSequence.addAction(PackagePrivateWorker.class.getName() + "-" + hiddenField);
    }
}
