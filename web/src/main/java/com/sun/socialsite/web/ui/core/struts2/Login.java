/*
 * Portions Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.
 */

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  The ASF licenses this file to You
 * under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.  For additional information regarding
 * copyright in this work, please see the NOTICE file in the top level
 * directory of this distribution.
 */

package com.sun.socialsite.web.ui.core.struts2;


/**
 * Login Action.
 */
public class Login extends CustomizedActionSupport {

    private boolean failed = false;

    public Login() {
    }

    @Override
    public String execute() {
        return SUCCESS;
    }

    public boolean isFailed() {
        return failed;
    }

    public void setFailed(boolean failed) {
        this.failed = failed;
    }

    // Having this method keeps struts from printing an error
    public void setJ_username(String j_username) {
    }

    // Having this method keeps struts from printing an error
    public void setJ_password(String j_password) {
    }

}
