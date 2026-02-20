/*
 * JBoss, Home of Professional Open Source
 * Copyright 2025, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.util.bytecode;

/**
 * JVM access flag constants.
 * Provides lightweight replacements for org.jboss.classfilewriter.AccessFlag functionality.
 * Values match the JVM specification (JVMS §4.1, §4.5, §4.6).
 *
 * @author Claude (Gizmo 2 migration)
 */
public final class AccessFlags {

    private AccessFlags() {
    }

    public static final int PUBLIC = 0x0001;
    public static final int PRIVATE = 0x0002;
    public static final int PROTECTED = 0x0004;
    public static final int STATIC = 0x0008;
    public static final int FINAL = 0x0010;
    public static final int SUPER = 0x0020; // Class access flag
    public static final int SYNCHRONIZED = 0x0020; // Method access flag (same value as SUPER)
    public static final int VOLATILE = 0x0040; // Field access flag
    public static final int BRIDGE = 0x0040; // Method access flag (same value as VOLATILE)
    public static final int TRANSIENT = 0x0080; // Field access flag
    public static final int VARARGS = 0x0080; // Method access flag (same value as TRANSIENT)
    public static final int NATIVE = 0x0100;
    public static final int INTERFACE = 0x0200;
    public static final int ABSTRACT = 0x0400;
    public static final int STRICT = 0x0800;
    public static final int SYNTHETIC = 0x1000;
    public static final int ANNOTATION = 0x2000;
    public static final int ENUM = 0x4000;
}
