function testTemplateType(){assertTrue(os.Container.isTemplateType_("text/template"));
assertTrue(os.Container.isTemplateType_("text/os-template"));
assertTrue(!os.Container.isTemplateType_("os-template"))
}function testRegisterTemplates(){os.Container.registerDocumentTemplates();
assertNotNull(os.getTemplate("os:Test"));
os.Container.processInlineTemplates();
var A=document.getElementById("test");
assertNotNull(A);
assertEquals("tag template",domutil.getVisibleText(A))
};