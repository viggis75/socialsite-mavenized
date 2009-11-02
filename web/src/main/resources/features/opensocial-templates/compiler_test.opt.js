function testSubstitution(){var A=[["hello world",null],["hello $world",null],["hello ${Cur} world","'hello '+($this)+' world'"],["${Cur} hello","($this)+' hello'"],["hello ${Cur}","'hello '+($this)"],["$ ${Cur}","'$ '+($this)"],["$${Cur}","'$'+($this)"],["${Cur} ${Index}","($this)+' '+($index)"],["a ${Cur} b ${Index} c","'a '+($this)+' b '+($index)+' c'"]];
for(var B=0;
B<A.length;
B++){var C=os.parseAttribute_(A[B][0]);
assertEquals(A[B][1],C)
}}function testWrapIdentifiers(){assertEquals("$_ir($_ir($context, 'foo'), 'bar')",os.wrapIdentifiersInExpression("foo.bar"));
assertEquals("$_ir($_ir($context, 'data'), 'array')()",os.wrapIdentifiersInExpression("data.array()"));
assertEquals("$_ir($_ir($context, 'data')(), 'array')",os.wrapIdentifiersInExpression("data().array"));
assertEquals("$_ir($context, 'os:Item')",os.wrapIdentifiersInExpression("os:Item"));
assertEquals("$_ir($context, 'foo') ? $_ir($context, 'bar') : $_ir($context, 'baz')",os.wrapIdentifiersInExpression("foo ? bar : baz"))
}function testTransformVariables(){assertEquals("$this.foo",os.transformVariables_("$cur.foo"))
}function testOperators(){var G={A:42,B:101};
var D=[{template:"${A lt B}",expected:"true"},{template:"${A gt B}",expected:"false"},{template:"${A eq A}",expected:"true"},{template:"${A neq A}",expected:"false"},{template:"${A lte A}",expected:"true"},{template:"${A lte B}",expected:"true"},{template:"${A gte B}",expected:"false"},{template:"${A gte A}",expected:"true"},{template:"${A eq "+G.A+"}",expected:"true"},{template:"${(A eq A) ? 'PASS' : 'FAIL'}",expected:"PASS"},{template:"${not true}",expected:"false"},{template:"${A eq A and B eq B}",expected:"true"},{template:"${A eq A and false}",expected:"false"},{template:"${false or A eq A}",expected:"true"},{template:"${false or false}",expected:"false"}];
for(var C=0;
C<D.length;
C++){var F=D[C];
var E=os.compileTemplateString(F.template);
var A=E.render(G);
var B=A.firstChild.innerHTML;
assertEquals(B,F.expected)
}}function testCopyAttributes(){var A=document.createElement("div");
var B=document.createElement("div");
A.setAttribute("attr","test");
A.setAttribute("class","foo");
os.copyAttributes_(A,B);
assertEquals("test",B.getAttribute("attr"));
assertEquals("foo",B.getAttribute("className"));
assertEquals("foo",B.className)
}function testTbodyInjection(){var D,A,C,B;
D="<table><tr><td>foo</td></tr></table>";
A="<table><tbody><tr><td>foo</td></tr></tbody></table>";
C=os.compileTemplateString(D);
B=C.templateRoot_.innerHTML;
B=B.toLowerCase();
B=B.replace(/\s/g,"");
assertEquals(A,B);
D="<table><tr><td>foo</td></tr><tr><td>bar</td></tr></table>";
A="<table><tbody><tr><td>foo</td></tr><tr><td>bar</td></tr></tbody></table>";
C=os.compileTemplateString(D);
B=C.templateRoot_.innerHTML;
B=B.toLowerCase();
B=B.replace(/\s/g,"");
assertEquals(A,B)
}function testEventHandlers(){var C,B,A;
window.testEvent=function(D){window.testValue=D
};
C='<button onclick="testEvent(true)">Foo</button>';
B=os.compileTemplateString(C);
A=B.render();
document.body.appendChild(A);
window.testValue=false;
A.firstChild.click();
document.body.removeChild(A);
assertEquals(true,window.testValue);
C="<button onclick=\"testEvent('${title}')\">Foo</button>";
B=os.compileTemplateString(C);
A=B.render({title:"foo"});
document.body.appendChild(A);
window.testValue=false;
A.firstChild.click();
document.body.removeChild(A);
assertEquals("foo",window.testValue)
}function testNestedIndex(){var C,B,A;
C='<table><tr repeat="list" var="row" index="x"><td repeat="row" index="y">${x},${y}</td></tr></table>';
B=os.compileTemplateString(C);
A=B.render({list:[["a","b"],["c","d"]]});
assertEquals("1,1",A.lastChild.lastChild.lastChild.lastChild.innerHTML)
}function testLoopNullDefaultValue(){var C='<div repeat="foo">a</div>';
var B=os.compileTemplateString(C);
var A=B.templateRoot_.firstChild.getAttribute("jsselect");
assertEquals("$_ir($context, 'foo', null)",A)
}function testGetFromContext(){var A={foo:"bar"};
assertEquals("bar",os.getFromContext(A,"foo"));
A=os.createContext(A);
assertEquals("bar",os.getFromContext(A,"foo"));
A.setVariable("baz","bing");
assertEquals("bing",os.getFromContext(A,"baz"));
assertEquals("",os.getFromContext(A,"title"));
assertEquals(null,os.getFromContext(A,"title",null))
};