function testDomUtils(){var B=document.getElementById("domSource");
var C=document.getElementById("domTarget");
var A=B.innerHTML;
C.innerHTML="";
os.appendChildren(B,C);
assertEquals(A,C.innerHTML);
os.removeChildren(C);
assertEquals(0,C.childNodes.length);
var D=document.createElement("p");
B.appendChild(D);
os.replaceNode(D,document.createElement("div"));
assertEquals("DIV",B.firstChild.tagName)
}function testGetPropertyGetterName(){assertEquals("getFoo",os.getPropertyGetterName("foo"));
assertEquals("getFooBar",os.getPropertyGetterName("fooBar"))
}function testConvertToCamelCase(){assertEquals("foo",os.convertToCamelCase("FOO"));
assertEquals("fooBar",os.convertToCamelCase("FOO_BAR"));
assertEquals("fooBarBaz",os.convertToCamelCase("FOO_BAR__BAZ"))
};