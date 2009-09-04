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

package com.sun.socialsite.business;

import com.sun.socialsite.Utils;
import com.sun.socialsite.business.impl.JPAOAuthStore;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerIndex;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreConsumerKeyAndSecret;
import org.apache.shindig.gadgets.oauth.BasicOAuthStoreTokenIndex;
import org.apache.shindig.gadgets.oauth.OAuthStore.TokenInfo;


/**
 * Test App-related business operations.
 */
public class OAuthStoreTest extends TestCase {
    public static Log log = LogFactory.getLog(OAuthStoreTest.class);

    public void setUp() throws Exception {
        Utils.setupSocialSite();
    }

    public static Test suite() {
        return new TestSuite(OAuthStoreTest.class);
    }

    public void tearDown() throws Exception {
    }

    public void testOAuthStoreCRUD() throws Exception {

        try {
            JPAOAuthStore store =
                (JPAOAuthStore)Factory.getSocialSite().getOAuthStore();

            BasicOAuthStoreConsumerIndex providerKey =
                new BasicOAuthStoreConsumerIndex();
            providerKey.setGadgetUri("http://example.com/gadget.xml");
            providerKey.setServiceName("testservice");

            BasicOAuthStoreConsumerKeyAndSecret keyAndSecret =
                new BasicOAuthStoreConsumerKeyAndSecret("aaa", "bbb",
                BasicOAuthStoreConsumerKeyAndSecret.KeyType.HMAC_SYMMETRIC, "keyname");

            store.consumerInfosPut(providerKey, keyAndSecret);

            assertNotNull(store.consumerInfosGet(providerKey));


            BasicOAuthStoreTokenIndex index = new BasicOAuthStoreTokenIndex();
            TokenInfo info = new TokenInfo("aaa", "bbb", "ccc", 0L);

            store.tokensPut(index, info);

            assertNotNull(store.tokensGet(index));
            

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;

        } finally {
            Utils.endSession(false);
        }

        log.info("END");
    }
}
