/**
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.servlet.deployment;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;

import org.jboss.logging.Logger;

/**
 * This class provides file-system orientated scanning
 *
 * @author Pete Muir
 * @author Ales Justin
 * @author Marko Luksa
 */
public class URLScanner {
    private static final Logger log = Logger.getLogger(URLScanner.class);

    private static final String CLASS_FILENAME_EXTENSION = ".class";
    private static final String COULD_NOT_READ = "could not read: ";
    // according to JarURLConnection api doc, the separator is "!/"
    private static final String SEPARATOR = "!/";

    private final ClassLoader classLoader;

    public URLScanner(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    protected ClassLoader getClassLoader() {
        return classLoader;
    }

    protected void handle(String name, URL url, Set<String> classes, Set<URL> urls) {
        if (name.endsWith(CLASS_FILENAME_EXTENSION)) {
            classes.add(filenameToClassname(name));
        } else if (name.equals(WebAppBeanDeploymentArchive.META_INF_BEANS_XML)) {
            urls.add(url);
        }
    }

    public void scanDirectories(File[] directories, Set<String> classes, Set<URL> urls) {
        for (File directory : directories) {
            handleDirectory(directory, null, classes, urls);
        }
    }

    public void scanURLs(URL[] resources, Set<String> classes, Set<URL> urls) {
        for (URL res : resources) {
            handleURL(res, classes, urls);
        }
    }

    public void scanResources(String[] resources, Set<String> classes, Set<URL> urls) {
        Set<String> paths = new HashSet<String>();

        for (String resourceName : resources) {
            try {
                Enumeration<URL> urlEnum = classLoader.getResources(resourceName);

                while (urlEnum.hasMoreElements()) {
                    URL url = urlEnum.nextElement();
                    String urlPath = url.toURI().getSchemeSpecificPart();

                    final String fileUrlType = "file:";
                    if (urlPath.startsWith(fileUrlType)) {
                        urlPath = urlPath.substring(fileUrlType.length());
                    }

                    if (urlPath.indexOf(SEPARATOR) > 0) {
                        urlPath = urlPath.substring(0, urlPath.indexOf(SEPARATOR));
                    } else {
                        File dirOrArchive = new File(urlPath);

                        if ((resourceName != null) && (resourceName.lastIndexOf('/') > 0)) {
                            // for META-INF/beans.xml
                            dirOrArchive = dirOrArchive.getParentFile();
                        }

                        urlPath = dirOrArchive.getParent();
                    }

                    paths.add(urlPath);
                }
            } catch (IOException ioe) {
                log.warn(COULD_NOT_READ + resourceName, ioe);
            } catch (URISyntaxException e) {
                log.warn(COULD_NOT_READ + resourceName, e);
            }
        }

        handle(paths, classes, urls);
    }

    protected void handle(Set<String> paths, Set<String> classes, Set<URL> urls) {
        for (String urlPath : paths) {
            try {
                log.tracev("scanning: {0}", urlPath);

                File file = new File(urlPath);

                if (file.isDirectory()) {
                    handleDirectory(file, null, classes, urls);
                } else {
                    handleArchiveByFile(file, classes, urls);
                }
            } catch (IOException ioe) {
                log.warn("could not read entries", ioe);
            }
        }
    }

    protected void handleArchiveByFile(File file, Set<String> classes, Set<URL> urls) throws IOException {
        try {
            log.tracev("archive: {0}", file);

            ZipFile zip = new ZipFile(file);
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                String name = entry.getName();
                // By using File, the correct URL chars are escaped (such as space) and others are not (such as / and $)
                String entryUrlString = "jar:" + new File(file.getPath() + SEPARATOR + name).toURI().toURL().toExternalForm();
                if (name.endsWith("/") && !entryUrlString.endsWith("/")) {
                    entryUrlString += "/";
                }
                URL entryUrl = new URL(entryUrlString);
                handle(name, entryUrl, classes, urls);
            }
            zip.close();
        } catch (ZipException e) {
            throw new RuntimeException("Error handling file " + file, e);
        }
    }

    private void handleDirectory(File file, String path, Set<String> classes, Set<URL> urls) {
        handleDirectory(file, path, new File[0], classes, urls);
    }

    private void handleDirectory(File file, String path, File[] excludedDirectories, Set<String> classes, Set<URL> urls) {
        for (File excludedDirectory : excludedDirectories) {
            if (file.equals(excludedDirectory)) {
                log.tracev("skipping excluded directory: {0}", file);

                return;
            }
        }

        log.tracev("handling directory: {0}", file);

        for (File child : file.listFiles()) {
            String newPath = (path == null) ? child.getName() : (path + '/' + child.getName());

            if (child.isDirectory()) {
                handleDirectory(child, newPath, excludedDirectories, classes, urls);
            } else {
                try {
                    handle(newPath, child.toURI().toURL(), classes, urls);
                } catch (MalformedURLException e) {
                    log.errorv("Error loading file {0}", newPath);
                }
            }
        }
    }

    protected void handleURL(URL url, Set<String> classes, Set<URL> urls) {
        log.warn("Not implemented.");
    }

    /**
     * Convert a path to a class file to a class name
     *
     * @param filename the file name
     * @return classname
     */
    public static String filenameToClassname(String filename) {
        return filename.substring(0, filename.lastIndexOf(CLASS_FILENAME_EXTENSION)).replace('/', '.').replace('\\', '.');
    }
}
