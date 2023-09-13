/*
 * JBoss, Home of Professional Open Source
 * Copyright 2012, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.annotated.slim.unbacked;

import java.io.ObjectStreamException;
import java.io.Serializable;

import org.jboss.weld.Container;
import org.jboss.weld.resources.MemberTransformer;

/**
 * A serialization proxy for {@link UnbackedAnnotatedMember}s. A calculated id of a member is stored. Based on the id, the
 * {@link UnbackedAnnotatedMember} instance is looked up from {@link MemberTransformer} on deserialization.
 *
 * @author jharting
 *
 * @param <X>
 */
public class UnbackedMemberIdentifier<X> implements Serializable {

    private static final long serialVersionUID = -8031539817026460998L;
    private final UnbackedAnnotatedType<X> type;
    private final String memberId;

    public UnbackedMemberIdentifier(UnbackedAnnotatedType<X> type, String memberId) {
        this.type = type;
        this.memberId = memberId;
    }

    public UnbackedAnnotatedType<X> getType() {
        return type;
    }

    public String getMemberId() {
        return memberId;
    }

    private Object readResolve() throws ObjectStreamException {
        return Container.instance(type.getIdentifier()).services().get(MemberTransformer.class).getUnbackedMember(this);
    }
}
