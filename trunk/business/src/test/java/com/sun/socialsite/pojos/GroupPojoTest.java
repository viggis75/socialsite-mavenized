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

package com.sun.socialsite.pojos;

import com.sun.socialsite.Utils;
import com.sun.socialsite.business.*;
import junit.framework.TestCase;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

/**
 * Test Group POJO implementation
 */
public class GroupPojoTest extends TestCase {
    private static Log log = LogFactory.getLog(GroupPojoTest.class);

    @Before
    public void setUp() throws Exception {
        Utils.setupSocialSite();
    }

    /**
     * Test updating group via JSON.
     */
    @Test
    public void testGroupPojoPropertyUpdate() {
        Group gr = null;
        GroupManager mgr = Factory.getSocialSite().getGroupManager();
        try {

            gr = new Group();
            gr.setHandle("mygrpHandle");
            gr.setName("mygrpName");
            gr.setDescription("mygrpDesc");
            mgr.saveGroup(gr);
            Utils.endSession(true);
            assertNotNull(mgr.getGroupByHandle("mygrpHandle"));
            {
                gr = mgr.getGroupByHandle("mygrpHandle");
                JSONObject updates = new JSONObject();
                updates.put("identification_name", "mygrpIdChanged");
                gr.update(updates);
                Factory.getSocialSite().flush();
                Utils.endSession(true);

                gr = mgr.getGroupByHandle("mygrpHandle");
                assertEquals("mygrpIdChanged",
                        gr.getProperty("identification_name").getValue());
                Utils.endSession(true);
            }
            {
                gr = mgr.getGroupByHandle("mygrpHandle");
                JSONObject updates = new JSONObject();
                updates.put("junkname", "junkvalue");
                gr.update(updates);
                Factory.getSocialSite().flush();
                Utils.endSession(true);

                gr = mgr.getGroupByHandle("mygrpHandle");
                assertNull(gr.getProperty("junkname"));
                Utils.endSession(true);
            }
            {
                gr = mgr.getGroupByHandle("mygrpHandle");
                JSONObject updates = new JSONObject();
                updates.put("identification_thumbnailUrl", "http://x.y.z/someurl");
                gr.update(updates);
                Factory.getSocialSite().flush();
                Utils.endSession(true);
            }
            {
                gr = mgr.getGroupByHandle("mygrpHandle");
                JSONObject updates = new JSONObject();
                updates.put("contact_emails_1_address", "e1@sun.com");
                updates.put("contact_emails_1_type", "Home");            
                updates.put("contact_emails_2_address", "e2@sun.com");
                updates.put("contact_emails_2_type", "Work");            
                updates.put("contact_emails_3_address", "e3@sun.com");
                updates.put("contact_emails_3_type", "Other");            
                gr.update(updates);
                Factory.getSocialSite().flush();
                Utils.endSession(true);

                gr = mgr.getGroupByHandle("mygrpHandle");
                assertEquals("e2@sun.com",
                        gr.getProperty("contact_emails_2_address").getValue());
                /*
                JSONObject deletes = new JSONObject();
                updates.put("contact_emails_3_address", "zzz_DELETE_zzz");
                updates.put("contact_emails_3_type", "zzz_DELETE_zzz");            
                gr.update(deletes);
                Factory.getSocialSite().flush();
                Utils.endSession(true);

                gr = mgr.getGroupByHandle("mygrpHandle");
                assertNull(gr.getProperty("contact_emails_3_address"));
                assertNull(gr.getProperty("contact_emails_3_type"));
                 */
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if(gr != null) {
                    mgr.removeGroup(gr);
                    Factory.getSocialSite().flush();
                }
            } catch(Exception x) {}
        }
    }
}
