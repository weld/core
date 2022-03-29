/*
 * JBoss, Home of Professional Open Source
 * Copyright 2022, Red Hat, Inc., and individual contributors
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

package org.jboss.weld.tests.resolution.circular.self.modifiers;

public interface SomeInterface {

    // public method, all variations
    Integer selfInvokePublicMethodPublicParamsPublicReturnType(Integer foo);
    Integer selfInvokePublicMethodProtectedParamsPublicReturnType(Integer foo);
    Integer selfInvokePublicMethodPublicParamsProtectedReturnType(Integer foo);
    Integer selfInvokePublicMethodProtectedParamsProtectedReturnType(Integer foo);

    // protected method, all variations
    Integer selfInvokeProtectedMethodProtectedParamsProtectedReturnType(Integer foo);
    Integer selfInvokeProtectedMethodPublicParamsProtectedReturnType(Integer foo);
    Integer selfInvokeProtectedMethodProtectedParamsPublicReturnType(Integer foo);
    Integer selfInvokeProtectedMethodPublicParamsPublicReturnType(Integer foo);
}
