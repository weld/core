/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.weld.context.beanstore;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Helper class for bean store creation locking.
 *
 * @author Stuart Douglas
 */
public class LockStore {

    private final Map<String, ReferenceCountedLock> locks = new HashMap<String, ReferenceCountedLock>();

    public LockedBean lock(String id) {
        ReferenceCountedLock refLock;
        synchronized (locks) {
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
        private final String key;
        int count = 1;
        final ReentrantLock lock = new ReentrantLock();

        private ReferenceCountedLock(final String key) {
            this.key = key;
        }

        public void unlock() {
            synchronized (locks) {
                lock.unlock();
                --count;
                if (count == 0) {
                    locks.remove(key);
                }
            }
        }
    }


}
