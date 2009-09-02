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

import com.google.inject.ImplementedBy;
import com.sun.socialsite.business.impl.JPAListenerManagerImpl;


/**
 * Allows objects to listen for entity lifecycle events.
 */
@ImplementedBy(JPAListenerManagerImpl.class)
public interface ListenerManager {

    /**
     * Registers the specified listener object to receive lifecycle
     * events from the specified entityClass.  One or more of the listener
     * object's methods should be annotated with normal JPA lifecycle
     * annotations (such as {@link javax.persistence.PrePersist} or
     * {@link javax.persistence.PostRemove}).  These methods will then be
     * called when an entity of the specified entityClass enters the corresponding
     * lifecycle state.
     * @param entityClass class of entities for which lifecycle events are desired.
     * @param listener the object which will receive matching lifecycle events.
     */
    public void addListener(Class entityClass, Object listener);


    /**
     * Unregisters the specified listener for lifecycle events from the
     * specified entity class.
     * @param entityClass class of entities for for lifecycle events.
     * @param listener the object which will no longer receive matching lifecycle events.
     */
    public void removeListener(Class entityClass, Object listener);

}
