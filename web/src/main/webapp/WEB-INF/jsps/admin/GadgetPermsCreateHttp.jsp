<%--
 DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.

 Copyright 2007-2008 Sun Microsystems, Inc. All rights reserved.

 The contents of this file are subject to the terms of either the GNU
 General Public License Version 2 only ("GPL") or the Common Development
 and Distribution License("CDDL") (collectively, the "License").  You
 may not use this file except in compliance with the License. You can obtain
 a copy of the License at https://socialsite.dev.java.net/legal/CDDL+GPL.html
 or legal/LICENSE.txt.  See the License for the specific language governing
 permissions and limitations under the License.

 When distributing the software, include this License Header Notice in each
 file and include the License file at legal/LICENSE.txt.  Sun designates this
 particular file as subject to the "Classpath" exception as provided by Sun
 in the GPL Version 2 section of the License file that accompanied this code.
 If applicable, add the following below the License Header, with the fields
 enclosed by brackets [] replaced by your own identifying information:
 "Portions Copyrighted [year] [name of copyright owner]"

 Contributor(s):

 If you wish your version of this file to be governed by only the CDDL or
 only the GPL Version 2, indicate your decision by adding "[Contributor]
 elects to include this software in this distribution under the [CDDL or GPL
 Version 2] license."  If you don't indicate a single choice of license, a
 recipient has the option to distribute your version of this file under
 either the CDDL, the GPL Version 2 or to extend the choice of license to
 its licensees as provided above.  However, if you add GPL Version 2 code
 and therefore, elected the GPL Version 2 license, then the option applies
 only if the new code is made subject to such option by the copyright
 holder.
--%>

<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript">

  function enableDisableSubject(s) {
    if (s.selectedIndex == 0) {
      document.getElementById('appNameSelector').disabled = false;
      document.getElementById('appDomainField').disabled = true;
    } else {
      document.getElementById('appNameSelector').disabled = true;
      document.getElementById('appDomainField').disabled = false;
    }
  }

  function enableDisableActions() {
    if (this.checked) {
      disableActions(true);
    } else {
      disableActions(false);
    }
  }

  function disableActions(dis) {
    for (var i=1; i<5; i++) {
      document.getElementById("actions-" + i).disabled = dis;
    }
  }

</script>

<%--<p><s:text name="GadgetPermsCreate.pageHelp" /></p>--%>
<h3><s:text name="GadgetPerms.newPermHttp" /></h3>

<s:form action="GadgetPermissionCreateHttp!save"
    method="POST"
    enctype="multipart/form-data">
  <s:hidden name="permissionType" value="HttpPermission" />

  <s:select name="subjectType"
      label="%{getText('GadgetPermCreate.grantSubject')}"
      list="{appOption, domainOption}"
      onchange="enableDisableSubject(this)" />
  <s:select name="appId"
      label="%{getText('GadgetPermCreate.chooseApp')}"
      id="appNameSelector"
      list="apps"
      listKey="id"
      listValue="title"
      headerKey="%{getText('GadgetPermCreate.appListHeader')}"
      headerValue="%{getText('GadgetPermCreate.appListHeader')}"
      emptyOption="false" />
  <s:textfield name="domain"
      label="%{getText('GadgetPermCreate.domainOption')}"
      id="appDomainField"
      disabled="true" />

  <tr><td>&nbsp;</td><td></td></tr>
  <s:textfield name="name" value="*"
      label="%{getText('GadgetPermCreate.name')}" />
  <s:checkboxlist name="actions"
      label="%{getText('GadgetPermCreate.actions')}"
      id="actionsCheckboxList"
      list="{'GET', 'POST', 'PUT', 'DELETE', '*'}" />

  <tr><td>&nbsp;</td><td></td></tr>
  <s:submit value="%{getText('GadgetPermCreate.submit')}" />

</s:form>

<script type="text/javascript">
  // todo: better selector here than generated id
  document.getElementById("actions-5").onchange = enableDisableActions;
</script>
