function testInjectJavaScript(){var A="function testFunction() { return 'foo'; }";
os.Loader.injectJavaScript(A);
assertTrue(window.testFunction instanceof Function);
assertEquals(window.testFunction(),"foo")
}function testInjectStyle(){var A=".testCSS { width: 100px; height: 200px; }";
os.Loader.injectStyle(A);
var B=getStyleRule(".testCSS");
assertNotNull(B);
assertEquals(B.style.width,"100px");
assertEquals(B.style.height,"200px")
}var testContentXML='<Templates xmlns:test="http://www.google.com/#test">  <Namespace prefix="test" url="http://www.google.com/#test"/>  <Template tag="test:tag">    <div id="tag"></div>  </Template>  <JavaScript>    function testJavaScript() {      return "testJavaScript";    }  </JavaScript>  <Style>    .testStyle {      width: 24px;    }  </Style>  <TemplateDef tag="test:tagDef">    <Template>      <div id="tagDef"></div>    </Template>    <JavaScript>      function testJavaScriptDef() {        return "testJavaScriptDef";      }    </JavaScript>    <Style>      .testStyleDef {        height: 42px;      }    </Style>  </TemplateDef></Templates>';
function testLoadContent(){os.Loader.loadContent(testContentXML);
var B=os.nsmap_.test;
assertNotNull(B);
assertTrue(B.tag instanceof Function);
assertTrue(B.tagDef instanceof Function);
assertTrue(window.testJavaScript instanceof Function);
assertEquals(window.testJavaScript(),"testJavaScript");
assertTrue(window.testJavaScriptDef instanceof Function);
assertEquals(window.testJavaScriptDef(),"testJavaScriptDef");
var C=getStyleRule(".testStyle");
assertNotNull(C);
assertEquals(C.style.width,"24px");
var A=getStyleRule(".testStyleDef");
assertNotNull(A);
assertEquals(A.style.height,"42px")
}function getStyleRule(B){var D=document.styleSheets;
for(var C=0;
C<D.length;
++C){var E=D[C].cssRules||D[C].rules;
if(E){for(var A=0;
A<E.length;
++A){if(E[A].selectorText==B||E[A].selectorText==B.toLowerCase()){return E[A]
}}}}return null
};