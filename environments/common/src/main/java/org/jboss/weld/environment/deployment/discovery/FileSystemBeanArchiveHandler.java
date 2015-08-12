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
package org.jboss.weld.environment.deployment.discovery;

import static org.jboss.weld.environment.util.URLUtils.PROCOTOL_JAR;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jboss.logging.Logger;
import org.jboss.weld.environment.logging.CommonLogger;
import org.jboss.weld.environment.util.Files;

/**
 * Handles JAR files and directories.
 *
 * @author Pete Muir
 * @author Marko Luksa
 * @author Martin Kouba
 */
public class FileSystemBeanArchiveHandler implements BeanArchiveHandler {

    private static final Logger log = Logger.getLogger(FileSystemBeanArchiveHandler.class);

    public static final String CLASS_FILE_EXTENSION = Files.CLASS_FILE_EXTENSION;

    @Override
    public BeanArchiveBuilder handle(String path) {

        File file = new File(path);

        if(!file.canRead()) {
            return null;
        }

        BeanArchiveBuilder builder = new BeanArchiveBuilder();

        try {
            log.debugv("Handle path: {0}", path);

            if (file.isDirectory()) {
                handleDirectory(new DirectoryEntry().setFile(file), builder);
            } else {
                handleFile(file, builder);
            }
        } catch (IOException e) {
            log.warn("Could not handle path: "+path , e);
        }
        return builder;
    }

    protected void handleFile(File file, BeanArchiveBuilder builder) throws IOException {

        log.debugv("Handle archive file: {0}", file);

        try {
            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            ZipFileEntry entry = new ZipFileEntry(PROCOTOL_JAR + ":" + file.toURI().toURL().toExternalForm() + "!/");
            while (entries.hasMoreElements()) {
                add(entry.setName(entries.nextElement().getName()), builder);
            }
            zip.close();
        } catch (ZipException e) {
            throw CommonLogger.LOG.cannotHandleFile(file, e);
        }
    }

    protected void handleDirectory(DirectoryEntry entry, BeanArchiveBuilder builder) throws IOException {

        log.debugv("Handle directory: {0}", entry.getFile());

        File[] files = entry.getFile().listFiles();

        if(files == null) {
            log.warnv("Unable to list directory files: {0}", entry.getFile());
        }
        String parentPath = entry.getName();

        for (File child : files) {

            if(entry.getName() != null ) {
                entry.setPath(entry.getName() + "/" + child.getName());
            } else {
                entry.setPath(child.getName());
            }
            entry.setFile(child);

            if (child.isDirectory()) {
                handleDirectory(entry, builder);
            } else {
                add(entry, builder);
            }
            entry.setPath(parentPath);
        }
    }

    protected void add(Entry entry, BeanArchiveBuilder builder) throws MalformedURLException {
        if (Files.isClass(entry.getName())) {
            builder.addClass(Files.filenameToClassname(entry.getName()));
        }
    }

    /**
     * An abstraction of a bean archive entry.
     */
    protected interface Entry {

        String getName();

        /**
         *
         * @return the URL, most probably lazily created
         * @throws MalformedURLException
         */
        URL getUrl() throws MalformedURLException;

    }

    private static class ZipFileEntry implements Entry {

        private String name;

        private String archiveUrl;

        ZipFileEntry(String archiveUrl) {
            this.archiveUrl = archiveUrl;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public URL getUrl() throws MalformedURLException {
            return new URL(archiveUrl + name);
        }

        ZipFileEntry setName(String name) {
            this.name = name;
            return this;
        }

    }

    private static class DirectoryEntry implements Entry {

        private String path;

        private File file;

        @Override
        public String getName() {
            return path;
        }

        @Override
        public URL getUrl() throws MalformedURLException {
            return file.toURI().toURL();
        }

        public DirectoryEntry setPath(String path) {
            this.path = path;
            return this;
        }

        public File getFile() {
            return file;
        }

        public DirectoryEntry setFile(File dir) {
            this.file = dir;
            return this;
        }

    }

}
