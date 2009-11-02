function compileAndRender_(A,C){var B=os.compileTemplate(document.getElementById(A));
var D=document.createElement("div");
B.renderInto(D,C);
return D
}function markupToNode(A){var B=document.createElement("div");
B.innerHTML=A;
return B.firstChild
}function isRealAttribute(A){return A.specified&&A.name.indexOf("js")!=0&&A.name!="__jstcache"
}function nodeToNormalizedMarkup(C){if(C.nodeType==3){return C.nodeValue
}else{if(C.nodeType==1){var E=false;
for(var B=0;
B<C.attributes.length;
B++){if(isRealAttribute(C.attributes[B])){E=true
}}if(C.getAttribute("customtag")!=null){E=false
}if(C.nodeName=="SPAN"&&!E){var D="";
for(var B=0;
B<C.childNodes.length;
B++){D+=nodeToNormalizedMarkup(C.childNodes[B])
}return D
}if(C.style.display=="none"){return""
}var D="<"+C.nodeName;
for(var B=0;
B<C.attributes.length;
B++){var A=C.attributes[B];
if(isRealAttribute(A)){D+=" "+A.name+'="'+A.value+'"'
}}D+=">";
for(var B=0;
B<C.childNodes.length;
B++){D+=nodeToNormalizedMarkup(C.childNodes[B])
}D+="</"+C.nodeName+">";
return D
}}return""
}function normalizeNodeOrMarkup(A){var B=typeof A=="string"?markupToNode(A):A;
return nodeToNormalizedMarkup(B)
}function assertTemplateDomEquals(A,B){A=normalizeNodeOrMarkup(A);
B=normalizeNodeOrMarkup(B);
assertEquals(A,B)
}function assertTemplateOutput(G,B,A,E){if(E instanceof Array){for(var D=0;
D<E.length;
D++){var I='<Templates xmlns:os="uri:unused">'+E[D]+"</Templates>";
var C=os.parseXML_(I);
os.Loader.processTemplatesNode(C)
}}var H=os.compileTemplateString(G);
var F=document.createElement("div");
H.renderInto(F,A);
assertTemplateDomEquals(B,F.firstChild)
}function testNamespace(){var C=os.createNamespace("custom","http://google.com/#custom");
assertEquals(C,os.getNamespace("custom"));
var B=os.createNamespace("custom","http://google.com/#custom");
assertEquals(B,os.getNamespace("custom"));
try{var A=os.createNamespace("custom","http://google.com/#custom_new");
fail("no exception thrown with new URL for the same namespace")
}catch(D){if(D.isJsUnitException){throw D
}}}function testSubstitution_text(){var A={title:"count",value:0};
assertTemplateOutput("<div>${title}:${value}</div>","<div>"+A.title+":"+A.value+"</div>",A)
}function testSubstitution_attribute(){var B={id:"varInAttr",color:"red",A1:111,text:"click me"};
var C=compileAndRender_("_T_Substitution_attribute",B);
var A=C.firstChild;
assertEquals(B.id,A.id);
assertEquals(B.color,A.style.color);
assertEquals("value "+B.A1,A.getAttribute("a1"));
assertEquals(B.text,A.innerHTML)
}function testSubstitution_nested(){var C={title:"Users",users:[{title:"President",color:"red",user:{name:"Bob",id:"101",url:"http://www.bob.com"}},{title:"Manager",color:"green",user:{name:"Rob",id:"102",url:"http://www.rob.com"}},{title:"Peon",color:"blue",user:{name:"Jeb",id:"103",url:"http://www.jeb.com"}}]};
os.createNamespace("my","www.google.com/#my");
os.Container.registerTag("my:user");
os.Container.registerTag("my:record");
var E=compileAndRender_("_T_Substitution_nested",C);
assertEquals(C.users.length,E.childNodes.length);
for(var D=0;
D<C.users.length;
D++){var B=C.users[D];
var F=E.childNodes[D];
assertEquals("DIV",F.tagName);
var G=F.firstChild;
while(!G.tagName){G=G.nextSibling
}assertEquals(B.color,G.color);
assertEquals(B.user.id,G.foo);
var I=G.childNodes[0];
assertEquals(B.color,I.firstChild.style.color);
assertEquals(B.title,I.firstChild.innerHTML);
var J=I.lastChild;
assertEquals(B.user.id,J.foo);
var H=J.firstChild.childNodes[0];
assertEquals(B.user.name,H.innerHTML);
assertContains(B.user.url,H.href);
var A=J.firstChild.childNodes[2];
assertEquals(B.user.id,A.innerHTML)
}}function testConditional_Number(){var A=os.compileTemplateString('<span if="42==42">TRUE</span><span if="!(42==42)">FALSE</span>').render();
assertEquals("TRUE",domutil.getVisibleTextTrim(A))
}function testConditional_String(){var A=os.compileTemplateString("<span if=\"'101'=='101'\">TRUE</span><span if=\"'101'!='101'\">FALSE</span>").render();
assertEquals("TRUE",domutil.getVisibleTextTrim(A))
}function testConditional_Mixed(){var A=os.compileTemplateString("<span if=\"'101' gt 42\">TRUE</span><span if=\"'101' lt 42\">FALSE</span>").render();
assertEquals("TRUE",domutil.getVisibleTextTrim(A))
}function testRepeat(){var C={entries:[{data:"This"},{data:"is"},{data:"an"},{data:"array"},{data:"of"},{data:"data."}]};
var D=compileAndRender_("_T_Repeat",C);
assertEquals(C.entries.length,D.childNodes.length);
for(var A=0;
A<C.entries.length;
A++){var B=C.entries[A];
assertEquals("DIV",D.childNodes[A].tagName);
assertEquals(B.data,domutil.getVisibleTextTrim(D.childNodes[A]))
}}function testSelect(){var E={options:[{value:"one"},{value:"two"},{value:"three"}]};
var F=compileAndRender_("_T_Options",E);
var A=F.firstChild;
assertEquals(E.options.length,A.options.length);
for(var B=0;
B<E.options.length;
B++){var C=E.options[B];
var D=A.options[B];
assertEquals("OPTION",D.tagName);
assertEquals(C.value,D.getAttribute("value"))
}}function testList(){os.Container.registerTag("custom:list");
var A=compileAndRender_("_T_List");
assertEquals("helloworld",domutil.getVisibleText(A))
}function testTag_input(){var E=os.createNamespace("custom","http://google.com/#custom");
E.input=function(H,F){var G=document.createElement("input");
G.value=F[H.getAttribute("value")];
return G
};
var D={data:"Some default data"};
var C=os.compileTemplateString('<custom:input value="data"/>');
var B=C.render(D);
var A=B.getElementsByTagName("input")[0];
assertEquals(A.value,D.data)
}function testTag_blink(){var B=os.createNamespace("custom","http://google.com/#custom");
B.blink=function(G,F){var E=document.createElement("span");
E.appendChild(G.firstChild);
function D(){var H=E.style.visibility=="visible";
E.style.visibility=H?"hidden":"visible";
setTimeout(D,500)
}E.onAttach=function(){D()
};
return E
};
Clock.reset();
var C=compileAndRender_("_T_Tag_blink");
var A=C.firstChild.firstChild;
assertEquals(A.style.visibility,"visible");
Clock.tick(500);
assertEquals(A.style.visibility,"hidden");
Clock.tick(500);
assertEquals(A.style.visibility,"visible")
}function testHelloWorld(){assertTemplateOutput("<div>Hello world!</div>","<div>Hello world!</div>")
}function testSimpleExpression(){assertTemplateOutput("<div>${HelloWorld}</div>","<div>Hello world!</div>",{HelloWorld:"Hello world!"})
}function testNamedTemplate(){assertTemplateOutput("<div><os:HelloWorld/></div>","<div>Hello world!</div>",null,['<Template tag="os:HelloWorld">Hello world!</Template>'])
}function testParameter(){var A=function(B){assertTemplateOutput('<div><os:HelloWorldWithParam text="Hello world!"/></div>',"<div>Hello world!</div>",null,['<Template tag="os:HelloWorldWithParam">'+B+"</Template>"])
};
A("${$my.text}");
A("${My.text}");
A("${my.text}")
}function testContent(){var A=function(B){assertTemplateOutput("<div><os:HelloWorldWithContent>Hello world!</os:HelloWorldWithContent></div>","<div>Hello world!</div>",null,['<Template tag="os:HelloWorldWithContent">'+B+"</Template>"])
};
A("<os:renderAll/>");
A("<os:RenderAll/>")
}function testNamedContent(){var A=function(B){assertTemplateOutput("<div><os:HelloWorldWithNamedContent><os:DontShowThis>Don't show this</os:DontShowThis><os:Content>Hello <b>world!</b></os:Content><Content>Hello <b>world!</b></Content></os:HelloWorldWithNamedContent></div>","<div>Hello <b>world!</b></div>",null,['<Template tag="os:HelloWorldWithNamedContent">'+B+"</Template>"])
};
A('<os:renderAll content="os:Content"/>');
A('<os:renderAll content="Content"/>')
}function testRepeatedContent(){var A=function(B){assertTemplateOutput("<os:HelloWorldRepeatedContent><Word>Hello</Word><Word>world!</Word><os:Word>Hello</os:Word><os:Word>world!</os:Word></os:HelloWorldRepeatedContent>","<div>Helloworld!</div>",null,['<Template tag="os:HelloWorldRepeatedContent">'+B+"</Template>"])
};
A('<div><span repeat="$my.Word"><os:renderAll/></span></div>');
A('<div><span repeat="${$my.Word}"><os:renderAll/></span></div>');
A('<div><span repeat="${$my.os:Word}"><os:renderAll/></span></div>');
A('<div><span repeat="$my.os:Word"><os:renderAll/></span></div>');
A('<div><span repeat="${my.os:Word}"><os:renderAll/></span></div>');
A('<div><span repeat="$my.os:Word"><os:renderAll/></span></div>');
A('<div><span repeat="${My.os:Word}"><os:renderAll/></span></div>')
}function testRepeatedContentTwice(){}function testRenderAllBadExprInContent(){}function testBooleanTrue(){assertTemplateOutput('<span if="${BooleanTrue}">Hello world!</span>',"<span>Hello world!</span>",{BooleanTrue:true});
assertTemplateOutput('<span if="BooleanTrue">Hello world!</span>',"<span>Hello world!</span>",{BooleanTrue:true});
assertTemplateOutput('<span if="!BooleanTrue">Hello world!</span>',"<span></span>",{BooleanTrue:true});
assertTemplateOutput('<span if="${!BooleanTrue}">Hello world!</span>',"<span></span>",{BooleanTrue:true})
}function testBooleanFalse(){assertTemplateOutput('<span if="BooleanFalse">Hello world!</span>',"<span></span>",{BooleanFalse:false});
assertTemplateOutput('<span if="!BooleanFalse">Hello world!</span>',"<span>Hello world!</span>",{BooleanFalse:false});
assertTemplateOutput('<span if="${!BooleanFalse}">Hello world!</span>',"<span>Hello world!</span>",{BooleanFalse:false});
assertTemplateOutput('<span if="${BooleanFalse}">Hello world!</span>',"<span></span>",{BooleanFalse:false})
}function testRepeatedNode(){var A=function(B){assertTemplateOutput(B,"<div>Helloworld!</div>",{Words:["Hello","world!"],WordObjects:[{value:"Hello"},{value:"world!"}]})
};
A('<div><span repeat="WordObjects">${$cur.value}</span></div>');
A('<div><span repeat="WordObjects">${value}</span></div>');
A('<div><span repeat="WordObjects">${cur.value}</span></div>');
A('<div><span repeat="Words">${cur}</span></div>');
A('<div><span repeat="Words">${$cur}</span></div>');
A('<div><span repeat="Words">${Cur}</span></div>');
A('<div><span repeat="Words">${$this}</span></div>')
}function testDynamicRepeatedContent(){assertTemplateOutput('<os:DynamicRepeat><Word repeat="WordObjects">${$cur.value}</Word></os:DynamicRepeat>',"<div>Helloworld!</div>",{WordObjects:[{value:"Hello"},{value:"world!"}]},['<Template tag="os:DynamicRepeat"><div><span repeat="$my.Word"><os:renderAll/></span></div></Template>'])
}function testReplaceTopLevelVars(){function B(D,C){assertEquals(C,os.replaceTopLevelVars_(D))
}B("my.man","$my.man");
B("my","$my");
B("My.man","$my.man");
B("My","$my");
B("cur.man","$this.man");
B("cur","$this");
B("Cur.man","$this.man");
B("Cur","$this");
B("$my.man","$my.man");
B("$my","$my");
B("ns.My","ns.My");
B("Cur/2","$this/2");
B("Cur*2","$this*2");
B("Cur[My.name]","$this[$my.name]");
B("Cur||'Nothing'","$this||'Nothing'");
B("My.man+your.man","$my.man+your.man");
B("your.man>My.man","your.man>$my.man");
function A(C){B("My.man"+C+"your.man","$my.man"+C+"your.man");
B("your.man"+C+"My.man","your.man"+C+"$my.man");
B("My"+C+"My","$my"+C+"$my")
}A("+");
A(" + ");
A("-");
A("<");
A(" lt ");
A(">");
A(" gt ");
A("=");
A("!=");
A("==");
A("&&");
A(" and ");
A("||");
A(" or ");
A(" and !");
A("/");
A("*");
A("|");
A("(");
A("[")
}function testHtmlTag(){var B=os.compileTemplateString('<os:Html code="${foo}"/>');
var A=B.render({foo:"Hello <b>world</b>!"});
var C=A.getElementsByTagName("b");
assertEquals(1,C.length)
}function testOnAttachAttribute(){var B=os.compileTemplateString("<div onAttach=\"this.title='bar'\"/>");
var A=document.createElement("div");
B.renderInto(A);
assertEquals("bar",A.firstChild.title)
}function testSpacesAmongTags(){var A=function(C){var B=os.compileTemplateString(C).render();
assertEquals("Hello world!",domutil.getVisibleTextTrim(B))
};
os.Loader.loadContent('<Templates xmlns:os="uri:unused"><Template tag="os:msg">${My.text}</Template></Templates>');
A('<div><os:msg text="Hello"/>\n <os:msg text="world!"/></div>');
A('<div><os:msg text="Hello"/>  <os:msg text="world!"/></div>');
A('<div> <os:msg text="Hello"/>  <os:msg text="world!"/>\n</div>');
os.Loader.loadContent('<Templates xmlns:os="uri:unused"><Template tag="os:msg"><os:Render/></Template></Templates>');
A("<div><os:msg>Hello</os:msg>\n <os:msg>world!</os:msg>\n</div>");
A("<div><os:msg>Hello</os:msg>  <os:msg>world!</os:msg></div>");
A("<div>\n  <os:msg>Hello</os:msg>  <os:msg>world!</os:msg>\n</div>")
};