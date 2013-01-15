/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat, Inc., and individual contributors
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
package org.jboss.weld.examples.pastecode.session;

import org.jboss.weld.examples.pastecode.model.CodeFragment;

import javax.ejb.Stateless;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Compute the hash for a {@link CodeFragment}
 *
 * @author Martin Gencur
 * @author Pete Muir
 */
@Stateless
public class HashComputer {

    public String getHashValue(CodeFragment code) throws NoSuchAlgorithmException {
        String hashValue;
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        String combinedValue = code.getText() + code.getDatetime();
        md.update(combinedValue.getBytes());
        hashValue = asHex(md.digest());
        return hashValue;
    }

    private String asHex(byte[] buf) {
        StringBuilder strBuf = new StringBuilder(buf.length * 2);

        // make sure it contains a letter!
        strBuf.append("h");

        for (int i = 0; i < buf.length; i++) {
            if ((buf[i] & 0xff) < 0x10) {
                strBuf.append("0");
            }
            strBuf.append(Long.toString(buf[i] & 0xff, 16));
        }
        if (strBuf.length() <= 6) {
            while (strBuf.length() <= 6) {
                strBuf.append("0");
            }
        }
        return strBuf.toString().substring(0, 6);
    }

}
