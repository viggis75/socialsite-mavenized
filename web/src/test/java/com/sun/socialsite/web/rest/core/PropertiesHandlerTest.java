/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common Development
 * and Distribution License("CDDL") (collectively, the "License").  You
 * may not use this file except in compliance with the License. You can obtain
 * a copy of the License at https://socialsite.dev.java.net/legal/CDDL+GPL.html
 * or legal/LICENSE.txt.  See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing the software, include this License Header Notice in each
 * file and include the License file at legal/LICENSE.txt.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided by Sun
 * in the GPL Version 2 section of the License file that accompanied this code.
 * If applicable, add the following below the License Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * If you wish your version of this file to be governed by only the CDDL or
 * only the GPL Version 2, indicate your decision by adding "[Contributor]
 * elects to include this software in this distribution under the [CDDL or GPL
 * Version 2] license."  If you don't indicate a single choice of license, a
 * recipient has the option to distribute your version of this file under
 * either the CDDL, the GPL Version 2 or to extend the choice of license to
 * its licensees as provided above.  However, if you add GPL Version 2 code
 * and therefore, elected the GPL Version 2 license, then the option applies
 * only if the new code is made subject to such option by the copyright
 * holder.
 */

package com.sun.socialsite.web.rest.core;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.config.Config;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.json.JSONObject;

import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

/**
 * Extend Shindig test to test our extensions to the Shindig handler.
 */
public class PropertiesHandlerTest extends TestCase {

    public static Log log = LogFactory.getLog(PropertiesHandlerTest.class);

    private BeanJsonConverter converter;
    protected PropertiesHandler handler;
    protected FakeSocialSiteGadgetToken token;
    protected HandlerRegistry registry;


    @Override
    protected void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        converter = injector.getInstance(BeanJsonConverter.class);

        Utils.setupSocialSite();
        List<App> apps = Factory.getSocialSite().getAppManager().getApps(0, 1);
        token = new FakeSocialSiteGadgetToken();
        token.setAppId(apps.get(0).getId());

        handler = new PropertiesHandler();
        registry = new DefaultHandlerRegistry(injector, converter,
            new HandlerExecutionListener.NoOpHandler());
        registry.addHandlers(ImmutableSet.<Object> of(handler));
    }


    public void testGetProperties() throws Exception {

        log.info("BEGIN");

        Profile johndoe = Utils.setupPerson("john.doe", "John", "Doe", "John.Doe@mycompany.com");
        try {

            // Find an existing App (for later use)
            AppManager appManager = Factory.getSocialSite().getAppManager();
            assertNotNull(appManager);
            List<App> apps = appManager.getApps(0, 1);
            assertEquals(1, apps.size());
            App app = apps.iterator().next();
            assertNotNull(app);

            token.setViewerId(johndoe.getUserId());
            token.setAppId(app.getId());

            // fetch it via handler
            String path = "/properties";
            Map<String, String[]> params = Maps.newHashMap();
            RestHandler operation = registry.getRestHandler(path, "GET");
            Future<?> future = operation.execute(params, new StringReader(""), token, converter);

            JSONObject response = (JSONObject)future.get();
            assertNotNull(response);
            assertEquals(Config.getProperty("socialsite.base.url"), response.getString("socialsite.base.url"));

        } finally {
            if(johndoe != null) Utils.teardownPerson(johndoe.getUserId());
        }

        log.info("END");

    }
    
}
