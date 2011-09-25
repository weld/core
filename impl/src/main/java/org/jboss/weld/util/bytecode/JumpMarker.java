/*
 * JBoss, Home of Professional Open Source
 * Copyright 2008, Red Hat, Inc., and individual contributors
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

import javassist.bytecode.Bytecode;

/**
 * Interface that represents a conditional jump or goto in bytecode.
 * <p/>
 * When the mark method is called the jump site is updated to point to the end
 * of the bytecode stream.
 *
 * @author Stuart Douglas
 * @see JumpUtils
 */
public interface JumpMarker {
    /**
     * Changes the jump instructions target to the next bytecode to be added to
     * the {@link Bytecode}
     */
    void mark();
}
