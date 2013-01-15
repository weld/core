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
import org.jboss.weld.examples.pastecode.model.Language;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
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
