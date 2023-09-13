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

package org.jboss.weld.contexts.beanstore;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import org.jboss.weld.serialization.spi.BeanIdentifier;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Helper class for bean store creation locking.
 *
 * @author Stuart Douglas
 * @author Marko Luksa
 */
public class LockStore implements Serializable {

    private static final long serialVersionUID = -698649566870070414L;

    @SuppressFBWarnings("SE_TRANSIENT_FIELD_NOT_RESTORED")
    private transient volatile Map<BeanIdentifier, ReferenceCountedLock> locks = new HashMap<BeanIdentifier, ReferenceCountedLock>();

    public LockedBean lock(BeanIdentifier id) {
        ReferenceCountedLock refLock;
        synchronized (this) {
            if (locks == null) {
                locks = new HashMap<BeanIdentifier, ReferenceCountedLock>();
            }
            refLock = locks.get(id);
            if (refLock != null) {
                refLock.count++;
            } else {
                refLock = new ReferenceCountedLock(id);
                locks.put(id, refLock);
            }
        }
        refLock.lock.lock();
        return refLock;
    }

    private class ReferenceCountedLock implements LockedBean {
        private final BeanIdentifier key;
        int count = 1;
        final ReentrantLock lock = new ReentrantLock();

        private ReferenceCountedLock(final BeanIdentifier key) {
            this.key = key;
        }

        public void unlock() {
            synchronized (LockStore.this) {
                lock.unlock();
                --count;
                if (count == 0) {
                    locks.remove(key);
                }
            }
        }
    }

}
