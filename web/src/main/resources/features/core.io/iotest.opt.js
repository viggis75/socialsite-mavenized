var gadgets=gadgets||{};
function IoTest(A){TestCase.call(this,A)
}IoTest.inherits(TestCase);
IoTest.prototype.setUp=function(){this.oldGetUrlParameters=gadgets.util.getUrlParameters;
gadgets.util.getUrlParameters=function(){return{st:"authtoken",url:"http://www.gadget.com/gadget.xml",container:"foo"}
};
if(!shindig.auth){shindig.auth=new shindig.Auth()
}this.fakeXhrs=new fakeXhr.Factory(this);
this.oldXMLHttpRequest=window.XMLHTTPRequest;
window.XMLHttpRequest=this.fakeXhrs.getXhrConstructor();
gadgets.config.init({"core.io":{proxyUrl:"http://example.com/proxy?url=%url%&refresh=%refresh%&g=%gadget%&c=%container%",jsonProxyUrl:"http://example.com/json"}});
gadgets.io.preloaded_={}
};
IoTest.prototype.tearDown=function(){gadgets.util.getUrlParameters=this.oldGetUrlParameters;
window.XMLHttpRequest=this.oldXMLHTTPRequest
};
IoTest.prototype.testGetProxyUrl=function(){var A=gadgets.io.getProxyUrl("http://target.example.com/image.gif");
this.assertEquals("http://example.com/proxy?url=http%3a%2f%2ftarget.example.com%2fimage.gif&refresh=3600&g=http%3a%2f%2fwww.gadget.com%2fgadget.xml&c=foo",A)
};
IoTest.prototype.testGetProxyUrl_nondefaultRefresh=function(){var A=gadgets.io.getProxyUrl("http://target.example.com/image.gif",{REFRESH_INTERVAL:30});
this.assertEquals("http://example.com/proxy?url=http%3a%2f%2ftarget.example.com%2fimage.gif&refresh=30&g=http%3a%2f%2fwww.gadget.com%2fgadget.xml&c=foo",A)
};
IoTest.prototype.testGetProxyUrl_disableCache=function(){var A=gadgets.io.getProxyUrl("http://target.example.com/image.gif",{REFRESH_INTERVAL:0});
this.assertEquals("http://example.com/proxy?url=http%3a%2f%2ftarget.example.com%2fimage.gif&refresh=0&g=http%3a%2f%2fwww.gadget.com%2fgadget.xml&c=foo",A)
};
IoTest.prototype.testEncodeValues=function(){var A=gadgets.io.encodeValues({foo:"bar"});
this.assertEquals("foo=bar",A)
};
IoTest.prototype.setArg=function(C,B,A,D){if(B){C.setBodyArg(A,D)
}else{C.setQueryArg(A,D)
}};
IoTest.prototype.setStandardArgs=function(B,A){this.setArg(B,A,"refresh","3600");
this.setArg(B,A,"st","");
this.setArg(B,A,"contentType","TEXT");
this.setArg(B,A,"authz","");
this.setArg(B,A,"bypassSpecCache","");
this.setArg(B,A,"signViewer","true");
this.setArg(B,A,"signOwner","true");
this.setArg(B,A,"getSummaries","false");
this.setArg(B,A,"gadget","http://www.gadget.com/gadget.xml");
this.setArg(B,A,"container","foo");
this.setArg(B,A,"headers","");
this.setArg(B,A,"numEntries","3");
this.setArg(B,A,"postData","");
this.setArg(B,A,"httpMethod","GET")
};
IoTest.prototype.makeFakeResponse=function(A){return new fakeXhr.Response("throw 1; < don't be evil' >"+A,200)
};
IoTest.prototype.testNoMethod=function(){var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
var B=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("some data",B.text)
};
IoTest.prototype.testNoMethod_nonDefaultRefresh=function(){var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
A.setQueryArg("refresh","1800");
var B=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
},{REFRESH_INTERVAL:1800,});
this.assertEquals("some data",B.text)
};
IoTest.prototype.testNoMethod_disableRefresh=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var B=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
},{REFRESH_INTERVAL:0,});
this.assertEquals("some data",B.text)
};
IoTest.prototype.testRepeatGet=function(){var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
var B=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,B);
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("some data",B.text);
B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("some data",B.text)
};
IoTest.prototype.testPost=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","POST");
A.setBodyArg("postData","foo=bar");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setBodyArg("headers","Content-Type=application%2fx-www-form-urlencoded");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="POST";
B[gadgets.io.RequestParameters.POST_DATA]="foo=bar";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPost_noBody=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","POST");
A.setBodyArg("postData","");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setBodyArg("headers","Content-Type=application%2fx-www-form-urlencoded");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="POST";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPost_emptyBody=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","POST");
A.setBodyArg("postData","");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setBodyArg("headers","Content-Type=application%2fx-www-form-urlencoded");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="POST";
B[gadgets.io.RequestParameters.POST_DATA]="";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPut=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","PUT");
A.setBodyArg("postData","abcd");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="PUT";
B[gadgets.io.RequestParameters.POST_DATA]="abcd";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPut_noBody=function(){var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","PUT");
A.setBodyArg("postData","");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="PUT";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testSignedGet=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("signOwner","true");
A.setBodyArg("signViewer","true");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testSignedPost=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("signOwner","true");
A.setBodyArg("signViewer","true");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","");
A.setBodyArg("refresh",null);
A.setBodyArg("httpMethod","POST");
A.setBodyArg("headers","Content-Type=application%2fx-www-form-urlencoded");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
B.METHOD="POST";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testSignedGet_noViewerBoolean=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("signOwner","true");
A.setBodyArg("signViewer","false");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
B.VIEWER_SIGNED=false;
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testSignedGet_noViewerString=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("signOwner","true");
A.setBodyArg("signViewer","false");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
B.VIEWER_SIGNED="false";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testSignedGet_withNoOwnerAndViewerString=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("signOwner","false");
A.setBodyArg("signViewer","true");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
B.VIEWER_SIGNED="true";
B.OWNER_SIGNED=false;
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testOAuth=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{oauthApprovalUrl:"http://sp.example.com/authz?oauth_token=foo",oauthState:"newState"}}));
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("http://sp.example.com/authz?oauth_token=foo",C.oauthApprovalUrl);
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("oauthState","newState");
A.setBodyArg("refresh",null);
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'personal data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("personal data",C.text)
};
IoTest.prototype.testSignedEquivalentToOAuth=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","signed");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","");
A.setBodyArg("OAUTH_USE_TOKEN","always");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{oauthApprovalUrl:"http://sp.example.com/authz?oauth_token=foo",oauthState:"newState"}}));
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="SIGNED";
B.OAUTH_USE_TOKEN="always";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("http://sp.example.com/authz?oauth_token=foo",C.oauthApprovalUrl)
};
IoTest.prototype.testOAuth_error=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{oauthError:"SOME_ERROR_CODE",oauthErrorText:"Some helpful error message",oauthState:"newState"}}));
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertUndefined(C.oauthApprovalUrl);
this.assertEquals("SOME_ERROR_CODE",C.oauthError);
this.assertEquals("Some helpful error message",C.oauthErrorText)
};
IoTest.prototype.testOAuth_serviceAndToken=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","");
A.setBodyArg("OAUTH_SERVICE_NAME","some-service");
A.setBodyArg("OAUTH_TOKEN_NAME","some-token");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{oauthApprovalUrl:"http://sp.example.com/authz?oauth_token=foo",oauthState:"newState"}}));
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
B[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME]="some-service";
B[gadgets.io.RequestParameters.OAUTH_TOKEN_NAME]="some-token";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("http://sp.example.com/authz?oauth_token=foo",C.oauthApprovalUrl);
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","newState");
A.setBodyArg("OAUTH_SERVICE_NAME","some-service");
A.setBodyArg("OAUTH_TOKEN_NAME","some-token");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'personal data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
B[gadgets.io.RequestParameters.OAUTH_SERVICE_NAME]="some-service";
B[gadgets.io.RequestParameters.OAUTH_TOKEN_NAME]="some-token";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("personal data",C.text)
};
IoTest.prototype.testOAuth_preapprovedToken=function(){gadgets.io.clearOAuthState();
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","");
A.setBodyArg("OAUTH_REQUEST_TOKEN","reqtoken");
A.setBodyArg("OAUTH_REQUEST_TOKEN_SECRET","abcd1234");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'personal data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B.AUTHORIZATION="OAUTH";
B[gadgets.io.RequestParameters.OAUTH_REQUEST_TOKEN]="reqtoken";
B[gadgets.io.RequestParameters.OAUTH_REQUEST_TOKEN_SECRET]="abcd1234";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("personal data",C.text)
};
IoTest.prototype.testJson=function(){var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
A.setQueryArg("contentType","JSON");
var B=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{body:'{ "somejsonparam" : 3 }',}}));
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
},{CONTENT_TYPE:"JSON",});
this.assertEquals(3,B.data.somejsonparam)
};
IoTest.prototype.testJson_malformed=function(){var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
A.setQueryArg("contentType","JSON");
var B=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{body:"{ bogus : 3 }",}}));
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
},{CONTENT_TYPE:"JSON",});
this.assertEquals("failed to parse JSON",B.errors[0])
};
IoTest.prototype.testPreload=function(){gadgets.io.preloaded_={"http://target.example.com/somepage":{rc:200,body:"preloadedbody",headers:{"set-cookie":["foo=bar","baz=quux"],location:["somewhere"],}}};
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("preloadedbody",B.text);
this.assertEquals("somewhere",B.headers.location[0]);
this.assertEquals("foo=bar",B.headers["set-cookie"][0]);
this.assertEquals("baz=quux",B.headers["set-cookie"][1]);
var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
var B=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{body:"not preloaded",}}));
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("not preloaded",B.text)
};
IoTest.prototype.testPreloadMiss_postRequest=function(){gadgets.io.preloaded_={"http://target.example.com/somepage":{rc:200,body:"preloadedbody",}};
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("httpMethod","POST");
A.setBodyArg("postData","foo=bar");
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("refresh",null);
A.setBodyArg("headers","Content-Type=application%2fx-www-form-urlencoded");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
B[gadgets.io.RequestParameters.METHOD]="POST";
B[gadgets.io.RequestParameters.POST_DATA]="foo=bar";
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPreloadMiss_wrongUrl=function(){gadgets.io.preloaded_={"http://target.example.com/somepage2":{rc:200,body:"preloadedbody",}};
var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
var C=this.makeFakeResponse("{ 'http://target.example.com/somepage' : { 'body' : 'some data' }}");
this.fakeXhrs.expect(A,C);
var C=null;
var B={};
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("some data",C.text)
};
IoTest.prototype.testPreload_error404=function(){gadgets.io.preloaded_={"http://target.example.com/somepage":{rc:404,}};
var A=new fakeXhr.Expectation("GET","http://example.com/json");
this.setStandardArgs(A,false);
A.setQueryArg("url","http://target.example.com/somepage");
var B=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{body:"not preloaded",}}));
this.fakeXhrs.expect(A,B);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("Error 404",B.errors[0]);
var B=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(C){B=C
});
this.assertEquals("not preloaded",B.text)
};
IoTest.prototype.testPreload_oauthApproval=function(){gadgets.io.clearOAuthState();
gadgets.io.preloaded_={"http://target.example.com/somepage":{rc:200,oauthState:"stateinfo",oauthApprovalUrl:"http://example.com/approve",}};
var A=new fakeXhr.Expectation("POST","http://example.com/json");
this.setStandardArgs(A,true);
A.setBodyArg("url","http://target.example.com/somepage");
A.setBodyArg("authz","oauth");
A.setBodyArg("st","authtoken");
A.setBodyArg("refresh",null);
A.setBodyArg("oauthState","stateinfo");
A.setHeader("Content-Type","application/x-www-form-urlencoded");
var C=this.makeFakeResponse(gadgets.json.stringify({"http://target.example.com/somepage":{body:"not preloaded",}}));
this.fakeXhrs.expect(A,C);
var B={};
B.AUTHORIZATION="OAUTH";
var C=null;
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("http://example.com/approve",C.oauthApprovalUrl);
gadgets.io.makeRequest("http://target.example.com/somepage",function(D){C=D
},B);
this.assertEquals("not preloaded",C.text)
};