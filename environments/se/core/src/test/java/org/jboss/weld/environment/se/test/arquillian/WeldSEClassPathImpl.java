/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.environment.se.test.arquillian;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.impl.base.spec.JavaArchiveImpl;

public class WeldSEClassPathImpl extends JavaArchiveImpl implements WeldSEClassPath {

    private final List<JavaArchive> archives = new LinkedList<JavaArchive>();

    public WeldSEClassPathImpl(Archive<?> delegate) {
        super(delegate);
    }

    @Override
    public WeldSEClassPath add(JavaArchive archive) {
        this.archives.add(archive);
        return this;
    }

    @Override
    public WeldSEClassPath add(JavaArchive... archives) {
        this.archives.addAll(Arrays.asList(archives));
        return this;
    }

    public List<JavaArchive> getArchives() {
        return archives;
    }

    @Override
    public String toString() {
        return "WeldSEClassPathImpl [archives=" + archives + "]";
    }
}
