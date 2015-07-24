/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.weld.environment.util;

import java.io.File;

/**
 *
 * @author Martin Kouba
 */
public final class Files {

    public static final String CLASS_FILE_EXTENSION = ".class";

    private Files() {
    }

    public static boolean isClass(String name) {
        return name.endsWith(CLASS_FILE_EXTENSION);
    }

    public static String filenameToClassname(String filename) {
        return filename.substring(0, filename.lastIndexOf(CLASS_FILE_EXTENSION)).replace('/', '.').replace('\\', '.');
    }

    public static boolean isUsable(File file) {
        return file != null && file.exists() && file.canRead();
    }

}
