<%@ taglib prefix="s" uri="/struts-tags" %>

<h1><s:text name="SelfRegistration.heading" /></h1>

<s:form action="SelfRegistration!save" method="POST">
    <s:textfield key="Login.username"                 name="newUser.userId" />
    <s:password  key="Login.password"                 name="newUser.password" />
    <s:textfield key="SelfRegistration.userFullName"  name="newUser.fullName" />
    <s:textfield key="Profile.primaryEmail"           name="newUser.emailAddress" />
    <s:submit />
</s:form>
