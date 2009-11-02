function UrlTemplateTest(A){TestCase.call(this,A)
}UrlTemplateTest.inherits(TestCase);
UrlTemplateTest.prototype.setUp=function(){};
UrlTemplateTest.prototype.tearDown=function(){};
UrlTemplateTest.prototype.batchTest=function(B){for(var E=0;
E<B.length;
++E){var D=B[E];
var H=D[0];
var A=D[1];
var F=D[2];
if(typeof F==="string"){this.assertEquals(F,gadgets.views.bind(H,A))
}else{var C=false;
try{gadgets.views.bind(H,A);
C=true
}catch(G){this.assertEquals(F.message,G.message)
}this.assertFalse(C)
}}};
UrlTemplateTest.prototype.testVariableSubstitution=function(){this.batchTest([["http://host/path/{A=65}{66=B}",{A:"a"},"http://host/path/aB"],["http://host/path/{open}{social}{0.8}{d-_-b}",{open:"O",social:"S","0.8":"v0.8","d-_-b":"!"},"http://host/path/OSv0.8!"],["http://host/path/{undefined_value}/suffix",{value:"undefined"},"http://host/path//suffix"],["http://host/path/{recurring}{recurring}{recurring}",{recurring:"."},"http://host/path/..."],[null,null,new Error("Invalid urlTemplate")],["http://host/path/{var}","string",new Error("Invalid environment")],["http://host/path/{invalid definition!!!}",{value:"defined"},new Error("Invalid syntax : {invalid definition!!!}")],["http://host/path/{} is also invalid",{value:"defined"},new Error("Invalid syntax : {}")]])
};
UrlTemplateTest.prototype.testPrefixOperator=function(){this.batchTest([["http://host/path/{-prefix|/|foo}{-prefix|/|bar}{-prefix|-|baz}",{foo:"bar",baz:["b","a","z"]},"http://host/path//bar-b-a-z"],["http://host/path/{-prefix|/|foo=bar}",{},"http://host/path//bar"]])
};
UrlTemplateTest.prototype.testSuffixOperator=function(){this.batchTest([["http://host/path/{-suffix|/|foo}{-suffix|/|bar}{-suffix|-|baz}",{foo:"bar",baz:["b","a","z"]},"http://host/path/bar/b-a-z-"],["http://host/path/{-suffix|/|foo=bar}",{},"http://host/path/bar/"]])
};
UrlTemplateTest.prototype.testListOperator=function(){this.batchTest([["http://host/path/{-list|/|foo}{-list|-|bar}{-list|-|baz}{-list|*|BAZ}",{foo:["f","o","o"],bar:[],BAZ:["baz"]},"http://host/path/f/o/obaz"]])
};
UrlTemplateTest.prototype.testJoinOperator=function(){this.batchTest([["http://host/path/{-join|*|spam}/{-join|&|foo,bar,baz}{-join|-|b}",{spam:"eggs",foo:"FOO",baz:"BAZ"},"http://host/path/spam=eggs/foo=FOO&baz=BAZ"]])
};
UrlTemplateTest.prototype.testOptOperator=function(){this.batchTest([["http://host/path/{-opt|spam|foo}/{-opt|eggs|foo,bar}/{-opt|ham|foo,bar,baz}",{bar:[],baz:"BAZ"},"http://host/path///ham"]])
};
UrlTemplateTest.prototype.testNegOperator=function(){this.batchTest([["http://host/path/{-neg|spam|foo}/{-neg|eggs|foo,bar}/{-neg|ham|foo,bar,baz}",{bar:[],baz:"BAZ"},"http://host/path/spam/eggs/"]])
};