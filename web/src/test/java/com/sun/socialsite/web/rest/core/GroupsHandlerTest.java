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

import java.io.StringReader;
import java.util.List;
import java.util.Map;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.protocol.DefaultHandlerRegistry;
import org.apache.shindig.protocol.HandlerExecutionListener;
import org.apache.shindig.protocol.HandlerRegistry;
import org.apache.shindig.protocol.RestHandler;
import org.apache.shindig.protocol.RestfulCollection;
import org.apache.shindig.protocol.conversion.BeanJsonConverter;
import org.apache.shindig.social.opensocial.service.FakeSocialSiteGadgetToken;
import org.json.JSONObject;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.sun.socialsite.Utils;
import com.sun.socialsite.business.AppManager;
import com.sun.socialsite.business.Factory;
import com.sun.socialsite.pojos.App;
import com.sun.socialsite.pojos.Group;
import com.sun.socialsite.pojos.Profile;
import com.sun.socialsite.web.rest.config.SocialSiteGuiceModule;

/**
 * Extend Shindig test to test our extensions to the Shindig handler.
 */
public class GroupsHandlerTest extends TestCase {

	public static Log log = LogFactory.getLog(GroupsHandlerTest.class);

	private BeanJsonConverter converter;
	protected GroupsHandler handler;
	protected FakeSocialSiteGadgetToken token;
	protected HandlerRegistry registry;

	@Override
	protected void setUp() throws Exception {
		Injector injector = Guice.createInjector(new SocialSiteGuiceModule());
		converter = injector.getInstance(BeanJsonConverter.class);

		Utils.setupSocialSite();
		AppManager appManager = Factory.getSocialSite().getAppManager();
		appManager.initialize();
		List<App> apps = appManager.getApps(0, 1);
		token = new FakeSocialSiteGadgetToken();
		token.setAppId(apps.get(0).getId());

		handler = new GroupsHandler(converter);
		registry = new DefaultHandlerRegistry(injector, converter,
				new HandlerExecutionListener.NoOpHandler());
		registry.addHandlers(ImmutableSet.<Object> of(handler));
	}

	public void testGetPublicGroups() throws Exception {

		log.info("BEGIN");

		Profile johndoe = Utils.setupPerson("john.doe", "John", "Doe",
				"John.Doe@mycompany.com");
		Group group1 = null;
		Group group2 = null;
		Group group3 = null;

		try {
			token.setViewerId(johndoe.getUserId());

			String path = "/groups/@public";
			Map<String, String[]> params = Maps.newHashMap();
			RestHandler operation = registry.getRestHandler(path, "GET");
			RestfulCollection collection = (RestfulCollection) operation
					.execute(params, new StringReader(""), token, converter)
					.get();

			assertEquals(0, collection.getTotalResults());

			group1 = Utils.setupGroup("testgroup1");
			group2 = Utils.setupGroup("testgroup2");
			group3 = Utils.setupGroup("testgroup3");
			Utils.endSession(true);

			collection = (RestfulCollection) operation.execute(params,
					new StringReader(""), token, converter).get();
			assertNotNull(collection);

			assertEquals(3, collection.getTotalResults());
		} finally {
			if (group1 != null)
				Utils.teardownGroup(group1.getHandle());
			if (group2 != null)
				Utils.teardownGroup(group2.getHandle());
			if (group3 != null)
				Utils.teardownGroup(group3.getHandle());
			if (johndoe != null)
				Utils.teardownPerson(johndoe.getUserId());
		}

		log.info("END");

	}

	public void testGetAPublicGroup() throws Exception {

		log.info("BEGIN");

		Profile johndoe = Utils.setupPerson("john.doe", "John", "Doe",
				"John.Doe@mycompany.com");
		Group group1 = null;

		try {
			// setup path to group (that doesn't exist yet)
			token.setViewerId(johndoe.getUserId());

			String path = "/groups/@public/testgroup1";
			Map<String, String[]> params = Maps.newHashMap();
			params.put("startIndex", new String[] { "0" });
			params.put("count", new String[] { "-1" });
			RestHandler operation = registry.getRestHandler(path, "GET");

			// request group, should throw error
			boolean notFoundError = false;
			try {
				operation.execute(params, new StringReader(""), token,
						converter).get();
			} catch (Throwable ex) {
				notFoundError = true;
			}
			assertTrue(notFoundError);

			// setup group
			group1 = Utils.setupGroup("testgroup1");
			Utils.endSession(true);

			// fetch it via handler
			JSONObject response = (JSONObject) operation.execute(params,
					new StringReader(""), token, converter).get();
			assertNotNull(response);
			assertEquals(group1.getHandle(), response.getString("handle"));
			assertEquals(group1.toJSON(Group.Format.OPENSOCIAL,
					token.getViewerId()).toString(), response.toString());
		} finally {
			if (group1 != null)
				Utils.teardownGroup(group1.getHandle());
			if (johndoe != null)
				Utils.teardownPerson(johndoe.getUserId());
		}

		log.info("END");

	}

	/*
	 * public void testGetUsersGroups() throws Exception {
	 * 
	 * log.info("BEGIN");
	 * 
	 * Profile johndoe = Utils.setupPerson("john.doe", "John", "Doe",
	 * "John.Doe@mycompany.com"); Profile janedoe =
	 * Utils.setupPerson("jane.doe", "Jane", "Doe", "Jane.Doe@mycompany.com");
	 * Utils.endSession(true); Group group1 = null; Group group2 = null; Group
	 * group3 = null; Group group4 = null;
	 * 
	 * try { token.setViewerId(johndoe.getUserId());
	 * setPathOperationAndParams("/groups/john.doe", "GET", null, token,
	 * converter); assertNull(handler.handleItem(request).get());
	 * token.setViewerId(johndoe.getUserId());
	 * setPathOperationAndParams("/groups/jane.doe", "GET", null, token,
	 * converter); assertNull(handler.handleItem(request).get());
	 * token.setViewerId(johndoe.getUserId()); group1 =
	 * Utils.setupGroup("testgroup1"); Utils.endSession(true);
	 * token.setViewerId(janedoe.getUserId()); group2 =
	 * Utils.setupGroup("testgroup2"); group3 = Utils.setupGroup("testgroup3");
	 * group4 = Utils.setupGroup("testgroup4");
	 * token.setViewerId(johndoe.getUserId());
	 * setPathOperationAndParams("/groups/john.doe", "GET", null, token,
	 * converter); JSONObject collection = (JSONObject)
	 * handler.handleItem(request) .get(); assertNotNull(collection); JSONArray
	 * groupList = collection.getJSONArray("group"); assertEquals(1,
	 * groupList.length()); assertEquals("testgroup1",
	 * groupList.getJSONObject(0).getString( "handle"));
	 * token.setViewerId(janedoe.getUserId());
	 * setPathOperationAndParams("/groups/jane.doe", "GET", null, token,
	 * converter); collection = (JSONObject) handler.handleItem(request).get();
	 * assertNotNull(collection); groupList = collection.getJSONArray("group");
	 * assertEquals(3, groupList.length()); } finally { if (group1 != null)
	 * Utils.teardownGroup(group1.getHandle()); if (group2 != null)
	 * Utils.teardownGroup(group2.getHandle()); if (group3 != null)
	 * Utils.teardownGroup(group3.getHandle()); if (group4 != null)
	 * Utils.teardownGroup(group4.getHandle()); if (johndoe != null)
	 * Utils.teardownPerson(johndoe.getUserId()); if (janedoe != null)
	 * Utils.teardownPerson(janedoe.getUserId()); }
	 * 
	 * log.info("END"); }
	 */

}
