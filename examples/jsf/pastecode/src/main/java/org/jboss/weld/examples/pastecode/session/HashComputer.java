/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
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
