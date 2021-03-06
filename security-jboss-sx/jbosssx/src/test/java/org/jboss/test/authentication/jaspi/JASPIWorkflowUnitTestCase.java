/*
 * JBoss, Home of Professional Open Source
 * Copyright 2007, JBoss Inc., and individual contributors as indicated
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
package org.jboss.test.authentication.jaspi;

import java.net.URL;
import java.util.HashMap;

import javax.security.auth.Subject;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.AppConfigurationEntry.LoginModuleControlFlag;
import javax.security.auth.message.AuthException;
import javax.security.auth.message.AuthStatus;
import javax.security.auth.message.MessageInfo;
import javax.security.auth.message.config.AuthConfigFactory;
import javax.security.auth.message.config.AuthConfigProvider;
import javax.security.auth.message.config.ServerAuthConfig;
import javax.security.auth.message.config.ServerAuthContext;

import junit.framework.TestCase;

import org.jboss.security.SecurityConstants;
import org.jboss.security.SecurityContext;
import org.jboss.security.SecurityContextAssociation;
import org.jboss.security.auth.callback.AppCallbackHandler;
import org.jboss.security.auth.login.XMLLoginConfigImpl;
import org.jboss.security.auth.message.GenericMessageInfo;
import org.jboss.security.auth.message.config.JBossAuthConfigProvider;
import org.jboss.security.plugins.JBossSecurityContext;
import org.jboss.test.SecurityActions;

/**
 * Test the Server side workflow for JASPI
 * 
 * @author Anil.Saldhana@redhat.com
 * @since Jul 16, 2007
 * @version $Revision$
 */
public class JASPIWorkflowUnitTestCase extends TestCase
{
   AuthConfigFactory factory = null;

   String layer = SecurityConstants.SERVLET_LAYER;

   String appId = "localhost /petstore";

   String configFile = "config/jaspi-config.xml";

   @SuppressWarnings("unchecked")
   @Override
   protected void setUp() throws Exception
   {
      factory = AuthConfigFactory.getFactory();
      factory.registerConfigProvider(new JBossAuthConfigProvider(new HashMap(), null), layer, appId,
            "Test Config Provider");

      SecurityContext jsc = new JBossSecurityContext("conf-jaspi");
      SecurityContextAssociation.setSecurityContext(jsc);

      XMLLoginConfigImpl xli = XMLLoginConfigImpl.getInstance();
      SecurityActions.setJAASConfiguration(xli);

      URL configURL = Thread.currentThread().getContextClassLoader().getResource(configFile);
      assertNotNull("Config URL", configURL);

      xli.setConfigURL(configURL);
      xli.loadConfig();
   }

   @SuppressWarnings("unchecked")
   public void testSuccessfulJASPI() throws Exception
   {
      AuthConfigProvider provider = factory.getConfigProvider(layer, appId, null);
      ServerAuthConfig serverConfig = provider.getServerAuthConfig(layer, appId, new AppCallbackHandler("anil",
            "anilpwd".toCharArray()));
      assertNotNull("ServerAuthConfig is not null", serverConfig);

      MessageInfo mi = new GenericMessageInfo(new Object(), new Object());
      String authContextID = serverConfig.getAuthContextID(mi);
      assertNotNull("AuthContext ID != null", authContextID);
      ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, new Subject(), new HashMap());
      assertNotNull("ServerAuthContext != null", sctx);
      Subject clientSubject = new Subject();
      Subject serviceSubject = new Subject();
      AuthStatus status = sctx.validateRequest(mi, clientSubject, serviceSubject);
      assertEquals(AuthStatus.SUCCESS, status);
   }

   @SuppressWarnings("unchecked")
   public void testUnSuccessfulJASPI() throws Exception
   {
      AuthConfigProvider provider = factory.getConfigProvider(layer, appId, null);
      ServerAuthConfig serverConfig = provider.getServerAuthConfig(layer, appId, new AppCallbackHandler("anil",
            "badpwd".toCharArray()));
      assertNotNull("ServerAuthConfig is not null", serverConfig);

      validateJAASConfiguration();
      MessageInfo mi = new GenericMessageInfo(new Object(), new Object());
      String authContextID = serverConfig.getAuthContextID(mi);
      assertNotNull("AuthContext ID != null", authContextID);
      ServerAuthContext sctx = serverConfig.getAuthContext(authContextID, new Subject(), new HashMap());
      assertNotNull("ServerAuthContext != null", sctx);
      Subject clientSubject = new Subject();
      Subject serviceSubject = new Subject();
      try
      {
         AuthStatus status = sctx.validateRequest(mi, clientSubject, serviceSubject);
         assertEquals(AuthStatus.FAILURE, status);
      }
      catch (AuthException ae)
      {
         //Ignore - we are fine
      }
   }

   private void validateJAASConfiguration()
   {
      //Lets validate the configuration
      Configuration config = Configuration.getConfiguration();
      AppConfigurationEntry[] appConfigEntries = config.getAppConfigurationEntry("conf-jaspi");
      assertTrue(appConfigEntries.length > 0);
      for (AppConfigurationEntry appConfigEntry : appConfigEntries)
      {
         assertEquals("org.jboss.test.authentication.jaspi.TestLoginModule", appConfigEntry.getLoginModuleName());
         assertEquals(LoginModuleControlFlag.OPTIONAL, appConfigEntry.getControlFlag());
      }

      appConfigEntries = config.getAppConfigurationEntry("lm-stack");
      assertTrue(appConfigEntries.length > 0);
      for (AppConfigurationEntry appConfigEntry : appConfigEntries)
      {
         assertEquals("org.jboss.test.authentication.jaspi.TestLoginModule", appConfigEntry.getLoginModuleName());
         assertEquals(LoginModuleControlFlag.OPTIONAL, appConfigEntry.getControlFlag());
      }
   }
}