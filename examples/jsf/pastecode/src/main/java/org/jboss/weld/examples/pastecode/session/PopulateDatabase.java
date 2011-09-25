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
import org.jboss.weld.examples.pastecode.model.Language;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.UserTransaction;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Populate the database with data.sql. Needed because import.sql doesn't
 * support multi-line inserts
 *
 * @author Pete Muir
 * @author Martin Gencur
 */
@Startup
@Singleton
public class PopulateDatabase {

    private static final String DATA_FILE_NAME = "data.sql";

    @Inject
    private Logger log;

    @PersistenceContext
    private EntityManager entityManager;

    @Inject
    private UserTransaction utx;

    @PostConstruct
    public void startup() {

        try {
            String fileContent = readFileData(DATA_FILE_NAME);
            StringTokenizer st = new StringTokenizer(fileContent, "'");
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            while (st.countTokens() > 1) {
                CodeFragment c = new CodeFragment();
                st.nextToken();
                c.setDatetime(formatter.parse(st.nextToken()));
                st.nextToken();
                c.setLanguage(Language.valueOf(st.nextToken()));
                st.nextToken();
                st.nextToken();
                st.nextToken();
                c.setUser(st.nextToken());
                st.nextToken();
                c.setText(st.nextToken());

                // Manual TX control, commit each record independently
                entityManager.persist(c);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Unable to read all records from " + DATA_FILE_NAME + " file", e);
        }

        log.info("Successfully imported data!");
    }

    private static String readFileData(String fileName) throws IOException {
        InputStream is = PopulateDatabase.class.getClassLoader().getResourceAsStream(fileName);
        BufferedReader br = new BufferedReader(new InputStreamReader(is));

        String line;
        StringBuilder sb = new StringBuilder();

        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }

        return sb.toString();
    }
}
