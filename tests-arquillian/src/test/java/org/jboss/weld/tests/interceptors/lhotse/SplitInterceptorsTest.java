/*
* JBoss, Home of Professional Open Source
* Copyright $today.year Red Hat Inc. and/or its affiliates and other
* contributors as indicated by the @author tags. All rights reserved.
* See the copyright.txt in the distribution for a full listing of
* individual contributors.
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

package org.jboss.weld.tests.interceptors.lhotse;

import org.jboss.arquillian.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.BeanArchive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.weld.tests.category.Broken;
import org.jboss.weld.tests.interceptors.lhotse.fst.TDAO;
import org.jboss.weld.tests.interceptors.lhotse.fst.TxInterceptor;
import org.jboss.weld.tests.interceptors.lhotse.snd.CDAO;
import org.jboss.weld.tests.interceptors.lhotse.snd.Client;
import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.categories.Category;
import org.junit.runner.RunWith;

/**
 * @author <a href="mailto:ales.justin@jboss.org">Ales Justin</a>
 */
@RunWith(Arquillian.class)
@Category(Broken.class)
public class SplitInterceptorsTest
{
   @Deployment
   public static Archive<?> deploy()
   {
      WebArchive web = ShrinkWrap.create(WebArchive.class).addPackage(SplitInterceptorsTest.class.getPackage());

      BeanArchive fst = ShrinkWrap.create(BeanArchive.class).intercept(TxInterceptor.class);
      fst.addPackage(TDAO.class.getPackage());
      web.addAsLibrary(fst);

      BeanArchive snd = ShrinkWrap.create(BeanArchive.class).intercept(TxInterceptor.class);
      snd.addPackage(CDAO.class.getPackage());
      web.addAsLibrary(snd);

      return web;
   }

   @Test
   public void testInterceptors(CDAO cdao) throws Exception
   {
      TxInterceptor.used = false;

      Client c = new Client();
      Assert.assertTrue(cdao.save(c));
      Assert.assertTrue(TxInterceptor.used);
   }
}
