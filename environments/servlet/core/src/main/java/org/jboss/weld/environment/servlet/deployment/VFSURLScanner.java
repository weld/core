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

import org.jboss.virtual.VFS;
import org.jboss.virtual.VirtualFile;
import org.jboss.virtual.VirtualFileVisitor;
import org.jboss.virtual.VisitorAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Set;

/**
 * This class provides JBoss VFS orientated scanning
 *
 * @author Ales Justin
 */
public class VFSURLScanner extends URLScanner {
    private static final Logger log = LoggerFactory.getLogger(VFSURLScanner.class);

    public VFSURLScanner(ClassLoader classLoader) {
        super(classLoader);
    }

    @Override
    protected void handleArchiveByFile(File file, final Set<String> classes, final Set<URL> urls) throws IOException {
        log.trace("archive: " + file);
        //noinspection deprecation
        handleURL(file.toURL(), classes, urls);
    }

    @Override
    protected void handleURL(URL url, final Set<String> classes, final Set<URL> urls) {
        try {
            final VirtualFile archive = VFS.getRoot(url.toURI());
            archive.visit(new VirtualFileVisitor() {
                public VisitorAttributes getAttributes() {
                    return VisitorAttributes.RECURSE_LEAVES_ONLY;
                }

                public void visit(VirtualFile vf) {
                    try {
                        String name = getRelativePath(archive, vf);
                        URL url = vf.toURL();
                        handle(name, url, classes, urls);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        } catch (Exception e) {
            throw new RuntimeException("Error handling url " + url, e);
        }
    }

    /**
     * Get the relative path between two virtual files
     *
     * @param parent the parent
     * @param child  the child
     * @return the relative path
     */
    static String getRelativePath(VirtualFile parent, VirtualFile child) {
        if (child == null)
            throw new IllegalArgumentException("Null child");

        String childPath = child.getPathName();
        if (parent != null) {
            String parentPath = parent.getPathName();

            if (parentPath.length() == childPath.length())
                return "";

            // Not sure about this? It is obviously not a direct child if it is shorter?
            if (parentPath.length() < childPath.length()) {
                if (parentPath.endsWith("/") == false)
                    parentPath = parentPath + "/";
                if (childPath.startsWith(parentPath))
                    return childPath.substring(parentPath.length());
            }
        }

        if (childPath.endsWith("/"))
            childPath = childPath.substring(0, childPath.length() - 1);

        return childPath;
    }
}
