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

import com.sun.socialsite.TestUtils;
import com.sun.socialsite.pojos.ContextRule;
import java.util.ArrayList;
import java.util.List;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Test Notifications-related business operations.
 */
public class ContextRuleTest extends TestCase {

    private static Log log = LogFactory.getLog(ContextRuleTest.class);

    private ContextRuleManager manager;

    @Override
    public void setUp() throws Exception {
        TestUtils.setupSocialSite();
        manager = Factory.getSocialSite().getContextRuleManager();
    }

    public static Test suite() {
        return new TestSuite(ContextRuleTest.class);
    }

    /*
     * Create and retrieve context rule.
     */
    public void testRuleCRUD() throws Exception {
        log.info("BEGIN");
        try {
            clearRules();
            assertEquals(0, manager.getRules().size());

            ContextRule rule = new ContextRule();
            rule.setSource("*");
            rule.setDirect(false);
            List<String> accept = new ArrayList<String>(1);
            accept.add("*");
            rule.setAccept(accept);

            manager.saveRule(rule);
            TestUtils.endSession(true);

            List<ContextRule> rules = manager.getRules();
            assertEquals(1, rules.size());

            rule = rules.get(0);
            assertEquals("*", rule.getSource());
            assertEquals(false, rule.isDirect());
            assertEquals("*", rule.getAccept().get(0));
            assertEquals(0, rule.getReject().size());
            
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }
        log.info("END");
    }

    /*
     * Test values
     */
    public void testRuleCRUDValues() throws Exception {
        log.info("BEGIN");
        final String source1 = "http://example.com";
        final String source2 = "http://sun.com";
        final String assertion1 = "foo";
        final String assertion2 = "bar";

        try {
            clearRules();

            ContextRule rule1 = new ContextRule();
            rule1.setSource(source1);
            rule1.setDirect(true);
            List<String> reject = new ArrayList<String>(1);
            reject.add("*");
            rule1.setReject(reject);

            ContextRule rule2 = new ContextRule();
            rule2.setSource(source2);
            rule2.setDirect(true);
            List<String> accept = new ArrayList<String>(2);
            accept.add(assertion1);
            accept.add(assertion2);
            rule2.setAccept(accept);
            rule2.setReject(new ArrayList<String>());

            manager.saveRule(rule1);
            manager.saveRule(rule2);
            TestUtils.endSession(true);

            List<ContextRule> rules = manager.getRules();
            assertEquals(2, rules.size());

            for (ContextRule rule : rules) {
                if (source1.equals(rule.getSource())) {
                    assertEquals(true, rule.isDirect());
                    assertTrue(rule.getAccept().isEmpty());
                    assertEquals("*", rule.getReject().get(0));
                } else if (source2.equals(rule.getSource())) {
                    assertEquals(true, rule.isDirect());
                    assertEquals(0, rule.getReject().size());
                    List<String> acs = rule.getAccept();
                    assertEquals(2, acs.size());
                    assertFalse(acs.get(0).equals(acs.get(1)));
                    for (String ac : acs) {
                        assertTrue(assertion1.equals(ac) ||
                            assertion2.equals(ac));
                    }
                } else {
                    fail(String.format("unknown rule: %s", rule.toString()));
                }
            }
        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }
        log.info("END");
    }

    /*
     * Test delete
     */
    public void testRuleCRUDDelete() throws Exception {
        log.info("BEGIN");
        try {
            clearRules();
            assertEquals(0, manager.getRules().size());

            ContextRule rule1 = new ContextRule();
            rule1.setSource("*");
            rule1.setDirect(true);
            List<String> accept = new ArrayList<String>(1);
            accept.add("*");
            rule1.setAccept(accept);

            manager.saveRule(rule1);
            TestUtils.endSession(true);

            List<ContextRule> rules = manager.getRules();
            assertEquals(1, rules.size());

            rule1 = rules.get(0);
            final String id = rule1.getId();
            ContextRule rule2 = manager.getRuleById(id);
            assertNotNull(rule2);
            assertEquals(rule1, rule2);

            manager.removeRule(rule2);
            TestUtils.endSession(true);

            rules = manager.getRules();
            assertEquals(0, rules.size());

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }
        log.info("END");
    }

    /*
     * Test equals
     */
    public void testRuleEquals() throws Exception {
        log.info("BEGIN");
        try {
            clearRules();
            assertEquals(0, manager.getRules().size());

            ContextRule rule1 = new ContextRule();
            rule1.setSource("*");
            rule1.setDirect(true);
            List<String> accept = new ArrayList<String>(1);
            accept.add("*");
            rule1.setAccept(accept);

            manager.saveRule(rule1);
            TestUtils.endSession(true);

            List<ContextRule> rules = manager.getRules();
            assertEquals(1, rules.size());

            rule1 = rules.get(0);
            final String id = rule1.getId();
            ContextRule rule2 = manager.getRuleById(id);
            assertNotNull(rule2);

            // id should be the same (criteria may change)
            assertEquals(rule1, rule2);

            // todo: add more cases with assertions in different order
//            ContextRule rule3 = new ContextRule();
//            rule3.setSource("*");
//            rule3.setDirect(true);
//            List<String> accept3 = new ArrayList<String>(1);
//            accept3.add("*");
//            rule1.setAccept(accept3);
//            assertEquals(rule1, rule3);

        } catch (Exception e) {
            log.error("Unexpected Exception", e);
            throw e;
        }
        log.info("END");
    }

    private void clearRules() throws Exception {
        List<ContextRule> rules = manager.getRules();
        for (ContextRule rule : rules) {
            manager.removeRule(rule);
        }
        TestUtils.endSession(true);
    }
    
}
