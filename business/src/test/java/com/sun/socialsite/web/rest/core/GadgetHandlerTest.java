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

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.TestUtils;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.business.ProfileManager;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.AppInstance;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;
import java.io.StringReader;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.RequestItem;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.json.JSONObject;


/**
 * Extend Shindig test to test our extensions to the Shindig handler.
 */
public class GadgetHandlerTest extends TestCase {

    private static Log log = LogFactory.getLog(GadgetHandlerTest.class);

    private BeanJsonConverter converter;

    private GadgetHandler handler;

    protected RequestItem request = null;

    protected HandlerRegistry registry;

    @Override
    protected void setUp() throws Exception {
        Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
        converter = injector.getInstance(BeanJsonConverter.class);

        handler = new GadgetHandler();
        registry = new DefaultHandlerRegistry(null, Sets.<Object>newHashSet(handler), converter,
            new HandlerExecutionListener.NoOpHandlerExecutionListener());

        TestUtils.setupSocialSite();
    }


    /**
     * Verifies that creating and deleting an AppInstance via the JSON interface works
     * as expected.
     * <p>
     *  Specifically, the following sequence is used:
     * </p>
     * <ol>
     *  <li>Creation of an AppInstance</li>
     *  <li>DELETE of the AppInstance with an inappropriate token (should fail)</li>
     *  <li>DELETE of the AppInstance with an appropriate token (should succeed)</li>
     * </ol>
     */
    public void testCreateAndDeleteAppInstance() throws Exception {

        log.info("BEGIN");

        Profile johndoe = null;

        try {

            ProfileManager profileManager = Factory.getSocialSite().getProfileManager();
            assertNotNull(profileManager);

            AppManager appManager = Factory.getSocialSite().getAppManager();
            assertNotNull(appManager);

            johndoe = TestUtils.setupPerson("john.doe", "John", "Doe", "John.Doe@mycompany.com");
            TestUtils.endSession(true);

            // Find an existing App (for later use)
            List<App> apps = appManager.getApps(0, 1);
            assertEquals(1, apps.size());
            App app = apps.iterator().next();
            assertNotNull(app);

            // Add an AppInstance (via the JSON interface)
            FakeSocialSiteGadgetToken token = new FakeSocialSiteGadgetToken();
            token.setViewerId(johndoe.getUserId());
            token.setAppId(app.getId());

            String path = String.format("/gadgets/@user/%s", johndoe.getUserId());
            Map<String, String[]> params = Maps.newHashMap();
            params.put("collection", new String[] {"PROFILE"});
            params.put("gadgetUrl", new String[] {app.getURL().toExternalForm()});
            RestHandler operation = registry.getRestHandler(path, "POST");
            Future<?> future = operation.execute(params,
                    new StringReader(""), token, converter);

            JSONObject result = (JSONObject)(future.get());
            assertEquals(201, result.getInt("code"));
            TestUtils.endSession(true);

            // And make sure it was stored (via the business interface)
            johndoe = profileManager.getProfileByUserId(johndoe.getUserId());
            assertNotNull(johndoe);
            List<AppInstance> appInstances = appManager.getAppInstancesByCollection(johndoe, "PROFILE");
            assertEquals(1, appInstances.size());
            AppInstance appInstance = appInstances.iterator().next();
            assertEquals(app, appInstance.getApp());
            TestUtils.endSession(true);

            // Attempt to delete appInstance (via the JSON interface) without an appropriate token
            path = String.format("/gadgets/%d", appInstance.getId());
            params = Maps.newHashMap();
            operation = registry.getRestHandler(path, "DELETE");
            future = operation.execute(params, new StringReader(""), token, converter);

            Exception expectedException = null;
            try {
                future.get();
            } catch (Exception e) {
                expectedException = e;
            }
            assertNotNull("DELETE with an inappropriate token should cause an exception", expectedException);
            TestUtils.endSession(true);

            // And make sure it's still there (via the business interface)
            johndoe = profileManager.getProfileByUserId(johndoe.getUserId());
            assertNotNull(johndoe);
            appInstances = appManager.getAppInstancesByCollection(johndoe, "PROFILE");
            assertEquals(1, appInstances.size());
            appInstance = appInstances.iterator().next();
            assertEquals(app, appInstance.getApp());
            TestUtils.endSession(true);

            // Delete appInstance (via the JSON interface) with an appropriate token
            token.setModuleId(appInstance.getId());
            path = String.format("/gadgets/@user/%s/%d", johndoe.getUserId(), appInstance.getId());
            params = Maps.newHashMap();
            operation = registry.getRestHandler(path, "DELETE");
            future = operation.execute(params, new StringReader(""), token, converter);

            result = (JSONObject)(future.get());
            int resultCode = result.getInt("code");
            assertTrue("result should have a 2xx status", ((resultCode >= 200) && (resultCode <= 299)));
            TestUtils.endSession(true);

            // Make sure appInstance is now gone (via the business interface)
            johndoe = profileManager.getProfileByUserId(johndoe.getUserId());
            assertNotNull(johndoe);
            appInstances = appManager.getAppInstancesByCollection(johndoe, "PROFILE");
            assertEquals(0, appInstances.size());
            TestUtils.endSession(true);

        } finally {
            if(johndoe != null) TestUtils.teardownPerson(johndoe.getUserId());
        }

        log.info("END");

    }
}
