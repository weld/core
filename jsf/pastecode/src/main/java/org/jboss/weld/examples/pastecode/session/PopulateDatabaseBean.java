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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.StringTokenizer;

import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.jboss.weld.examples.pastecode.model.CodeFragment;
import org.jboss.weld.examples.pastecode.model.Language;

/**
 * This bean only populates database with preformatted data. This is due to need
 * for Hypersonic database which doesn't allow multi-line inserts. Hypersonic
 * database is embedded in JBoss AS and so there is no need to configure any
 * external database to run this example.
 * 
 */
// TODO Make into an EJB Singleton which executes at startup
@ApplicationScoped
@Named("database")
//TODO @Singleton @Startup
public class PopulateDatabaseBean
{
   
   private static final String DATA_FILE_NAME = "data.sql";

   @Inject
   private CodeFragmentManager codeFragmentManager;
   
   private boolean populated;

   // TODO @PostConstruct
   public void populate()
   {
      if (populated)
      {
         return;
      }

      try
      {
         String fileContent = readFileData(DATA_FILE_NAME);
         StringTokenizer st = new StringTokenizer(fileContent, "'");
         SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

         while (st.countTokens() > 1)
         {
            CodeFragment c = new CodeFragment();
            st.nextToken();
            c.setDatetime(formatter.parse(st.nextToken()));
            st.nextToken();
            c.setLanguage(Language.valueOf(st.nextToken()));
            st.nextToken();
            c.setNote(st.nextToken());
            st.nextToken();
            c.setUser(st.nextToken());
            st.nextToken();
            c.setText(st.nextToken());

            codeFragmentManager.addCodeFragment(c, false);
         }
      }
      catch (Exception e)
      {
         System.err.println("Unable to read all records from " + DATA_FILE_NAME + " file");
      }

      populated = true;
   }

   private String readFileData(String fileName) throws IOException
   {
      InputStream is = this.getClass().getClassLoader().getResourceAsStream(fileName);
      BufferedReader br = new BufferedReader(new InputStreamReader(is));

      String line;
      StringBuilder sb = new StringBuilder();

      while ((line = br.readLine()) != null)
      {
         sb.append(line).append("\n");
      }

      return sb.toString();
   }
}
