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

import org.jboss.weld.config.ConfigurationKey;

/**
 *
 * @author <a href="mailto:manovotn@redhat.com">Matej Novotny</a>
 */
class BeanDeployments {

    // see org.jboss.as.weld.deployment.processors.ExternalBeanArchiveProcessor
    private static final String EXTERNAL_ARCHIVE_DENOMINATOR = ".external.";

    /**
     *
     * @param beanArchiveId
     * @param delimiter
     * @return the abbreviated bean archive id
     * @see ConfigurationKey#ROLLING_UPGRADES_ID_DELIMITER
     */
    static String getFinalId(String beanArchiveId, String delimiter) {
        // if delimiter is empty or if the archive is an external archive, return original ID
        // NOTE: the way we recognize external archive is WFLY-specific
        if (delimiter.isEmpty() || beanArchiveId.contains(EXTERNAL_ARCHIVE_DENOMINATOR)) {
            return beanArchiveId;
        }
        int idx = beanArchiveId.indexOf(delimiter);
        if (idx < 0) {
            return beanArchiveId;
        }
        String beforeDelimiter = beanArchiveId.substring(0, idx);
        int suffixIdx = beanArchiveId.lastIndexOf(".");
        // if there is no archive suffix, and it clashes with the delimiter, ignore it
        if (suffixIdx + 1 == idx + delimiter.length()) {
            suffixIdx = -1;
        }
        return suffixIdx < 0 ? beforeDelimiter : beforeDelimiter + beanArchiveId.substring(suffixIdx, beanArchiveId.length());
    }

    private BeanDeployments() {
    }
}
