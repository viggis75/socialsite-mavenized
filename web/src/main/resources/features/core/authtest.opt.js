var gadgets=gadgets||{};
function AuthTest(A){TestCase.call(this,A)
}AuthTest.inherits(TestCase);
AuthTest.prototype.setUp=function(){gadgets.util=gadgets.util||{};
gadgets.config=gadgets.config||{};
this.oldGetUrlParameters=gadgets.util.getUrlParameters;
this.oldConfigRegister=gadgets.config.register
};
AuthTest.prototype.tearDown=function(){gadgets.util.getUrlParameters=this.oldGetUrlParameters;
gadgets.config.register=this.oldConfigRegister
};
AuthTest.prototype.testTokenOnFragment=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"authtoken"}
};
var A=new shindig.Auth();
this.assertEquals("authtoken",A.getSecurityToken());
this.assertNull(A.getTrustedData());
A.updateSecurityToken("newtoken");
this.assertEquals("newtoken",A.getSecurityToken())
};
AuthTest.prototype.testTokenInConfig=function(){gadgets.config.register=function(B,C,D){D({"shindig.auth":{authToken:"configAuthToken"}})
};
gadgets.util.getUrlParameters=function(){return{st:"fragmentAuthToken"}
};
var A=new shindig.Auth();
this.assertEquals("configAuthToken",A.getSecurityToken());
this.assertNull(A.getTrustedData());
A.updateSecurityToken("newtoken");
this.assertEquals("newtoken",A.getSecurityToken())
};
AuthTest.prototype.testNoToken=function(){gadgets.config.register=function(B,C,D){D({"shindig.auth":null})
};
gadgets.util.getUrlParameters=function(){return{}
};
var A=new shindig.Auth();
this.assertEquals(null,A.getSecurityToken());
this.assertNull(A.getTrustedData());
A.updateSecurityToken("newtoken");
this.assertEquals("newtoken",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_normal=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&url=http%3a%2f%2fwww.example.com%2fgadget.xml",A.getSecurityToken());
A.updateSecurityToken("newtoken");
this.assertEquals("newtoken",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_blankvalue=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&url=$&url=",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&url=http%3a%2f%2fwww.example.com%2fgadget.xml&url=",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_dupname=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&url=$&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&url=http%3a%2f%2fwww.example.com%2fgadget.xml&url=http%3a%2f%2fwww.example.com%2fgadget.xml",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_blankname=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&=&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&=&url=http%3a%2f%2fwww.example.com%2fgadget.xml",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_nonpaired=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&foo&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&foo&url=http%3a%2f%2fwww.example.com%2fgadget.xml",A.getSecurityToken())
};
AuthTest.prototype.testAddParamsToToken_extraequals=function(){gadgets.config.register=function(B,C,D){D({})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&foo=$bar$=$baz$&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("t=abcd&foo=$bar$=$baz$&url=http%3a%2f%2fwww.example.com%2fgadget.xml",A.getSecurityToken())
};
AuthTest.prototype.testTrustedJson=function(){gadgets.config.register=function(B,C,D){D({"shindig.auth":{trustedJson:'{ "foo" : "bar" }'}})
};
gadgets.util.getUrlParameters=function(){return{st:"t=abcd&foo=$bar$=$baz$&url=$",url:"http://www.example.com/gadget.xml"}
};
var A=new shindig.Auth();
this.assertEquals("bar",A.getTrustedData().foo)
};