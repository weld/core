/*
 * JBoss, Home of Professional Open Source
 * Copyright 2015, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.bootstrap;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
class BeanDeployments {

    /** Takes archiveId and removes the delimiter and anything beyond that.
     * Used while creating archives if there was a delimiter specified.
     *
     * @param archiveId The ID of archive to be stripped of its affix
     * @param delimiter Delimiter defined via ConfigurationKey, empty String otherwise
     * @return archiveId if the delimiter was empty String, a substring of archiveId from index zero to the first occurrence of delimiter
     */
    static String getFinalId(String archiveId, String delimiter) {
        if (delimiter.isEmpty()) {
            return archiveId;
        } else {
            int idx = archiveId.indexOf(delimiter);
            return archiveId.substring(0, idx >= 0 ? idx : archiveId.length());
        }
    }

    private BeanDeployments() {
    }
}
