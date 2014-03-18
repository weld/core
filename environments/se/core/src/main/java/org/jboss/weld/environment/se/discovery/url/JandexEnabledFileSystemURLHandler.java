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
package org.jboss.weld.environment.se.discovery.url;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import org.jboss.jandex.Index;
import org.jboss.jandex.Indexer;
import org.jboss.logging.Logger;
import org.jboss.weld.bootstrap.api.Bootstrap;

/**
 * An implementation of {@link FileSystemURLHandler} that is filling the {@link BeanArchiveBuilder} also with the jandex index.
 *
 * @author Matej Briškár
 */
public class JandexEnabledFileSystemURLHandler extends FileSystemURLHandler {

    private static final String UNABLE_TO_OPEN_STREAM_MESSAGE = "Could not open the stream on the url when adding to the jandex index.";
    private static final String UNABLE_TO_CLOSE_STREAM_MESSAGE = "Could not close the stream on the url when adding to the jandex index.";
    private static final Logger log = Logger.getLogger(JandexEnabledFileSystemURLHandler.class);
    private final Indexer indexer = new Indexer();

    public JandexEnabledFileSystemURLHandler(Bootstrap bootstrap) {
        super(bootstrap);
    }

    private void addToIndex(URL url) {
        InputStream fs = null;
        try {
            fs = url.openStream();
            indexer.index(fs);
        } catch (IOException ex) {
            log.warn(UNABLE_TO_OPEN_STREAM_MESSAGE, ex);
        } finally {
            try {
                if (fs != null) {
                    fs.close();
                }
            } catch (IOException ex) {
                log.warn(UNABLE_TO_CLOSE_STREAM_MESSAGE, ex);
            }
        }
    }

    @Override
    protected void addToDiscovered(String name, URL url) {
        super.addToDiscovered(name, url);
        if (name.endsWith(CLASS_FILE_EXTENSION)) {
            addToIndex(url);
        }
    }

    @Override
    protected BeanArchiveBuilder createBeanArchiveBuilder() {
        return new BeanArchiveBuilder(null, buildJandexIndex(), getDiscoveredClasses(), getDiscoveredBeansXmlUrl(), getBootstrap());
    }

    public Index buildJandexIndex() {
        return indexer.complete();
    }
}
