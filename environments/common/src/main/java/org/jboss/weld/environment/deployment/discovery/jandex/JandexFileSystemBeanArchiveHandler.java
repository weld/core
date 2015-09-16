/*
 * JBoss, Home of Professional Open Source
 * Copyright 2014, Red Hat, Inc. and/or its affiliates, and individual
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
package org.jboss.weld.environment.deployment.discovery.jandex;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.weld.environment.deployment.discovery.BeanArchiveBuilder;
import org.jboss.weld.environment.deployment.discovery.FileSystemBeanArchiveHandler;
import org.jboss.weld.environment.logging.CommonLogger;

/**
 * Builds and attaches a jandex index to each bean archive.
 *
 * @author Matej Briškár
 * @author Martin Kouba
 */
public class JandexFileSystemBeanArchiveHandler extends FileSystemBeanArchiveHandler {

    private final Indexer indexer = new Indexer();

    @Override
    public BeanArchiveBuilder handle(String path) {
        BeanArchiveBuilder builder = super.handle(path);
        if (builder == null) {
            return null;
        }
        builder.setAttribute(JandexDiscoveryStrategy.INDEX_ATTRIBUTE_NAME, buildIndex());
        return builder;
    }

    @Override
    protected void add(Entry entry, BeanArchiveBuilder builder) throws MalformedURLException {
        super.add(entry, builder);
        if (isClass(entry.getName())) {
            addToIndex(entry.getUrl());
        }
    }

    private void addToIndex(URL url) {
        InputStream fs = null;
        try {
            fs = url.openStream();
            indexer.index(fs);
        } catch (IOException ex) {
            CommonLogger.LOG.couldNotOpenStreamForURL(url, ex);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ex) {
                CommonLogger.LOG.couldNotCloseStreamForURL(url, ex);
            }
        }
    }

    private Index buildIndex() {
        return indexer.complete();
    }
}
