var fakeXhr=fakeXhr||{};
fakeXhr.Expectation=function(B,A){this.method=B;
this.url=A;
this.queryArgs={};
this.bodyArgs={};
this.headers={}
};
fakeXhr.Expectation.prototype.setMethod=function(A){this.method=A
};
fakeXhr.Expectation.prototype.setUrl=function(A){this.url=A;
var B=A.indexOf("?");
if(B!==-1){this.queryArgs=this.parseForm(A.substr(B+1));
this.url=A.substr(0,B)
}};
fakeXhr.Expectation.prototype.setBodyArg=function(A,B){if(B!==null){this.bodyArgs[A]=B
}else{delete this.bodyArgs[A]
}};
fakeXhr.Expectation.prototype.setQueryArg=function(A,B){if(B!==null){this.queryArgs[A]=B
}else{delete this.queryArgs[A]
}};
fakeXhr.Expectation.prototype.setHeader=function(A,B){if(B!==null){this.headers[A]=B
}else{delete this.headers[A]
}};
fakeXhr.Expectation.prototype.parseForm=function(E){var B={};
if(E){var G=E.split("&");
for(var D=0;
D<G.length;
++D){var A=G[D].split("=");
var C=unescape(A[0]);
var F=unescape(A[1]);
B[C]=F
}}return B
};
fakeXhr.Expectation.prototype.toString=function(){return gadgets.json.stringify(this)
};
fakeXhr.Expectation.prototype.checkMatch=function(B,A){B.assertEquals(this.method,A.method);
B.assertEquals(this.url,A.url);
this.checkTableEquals(B,"query",this.queryArgs,A.queryArgs);
this.checkTableEquals(B,"body",this.bodyArgs,A.bodyArgs);
this.checkTableEquals(B,"header",this.headers,A.headers)
};
fakeXhr.Expectation.prototype.checkTableEquals=function(B,C,A,E){var D;
for(D in A){if(A.hasOwnProperty(D)){B.assertEquals("wrong value for "+C+" parameter "+D,A[D],E[D])
}}for(D in E){if(E.hasOwnProperty(D)){B.assertEquals("extra value for "+C+" parameter "+D,A[D],E[D])
}}};
fakeXhr.Response=function(B,A){this.responseText=B;
this.status=A||200
};
fakeXhr.Response.prototype.getResponseText=function(){return this.responseText
};
fakeXhr.Response.prototype.getStatus=function(){return this.status
};
fakeXhr.Factory=function(A){this.testcase=A;
this.expectations=[]
};
fakeXhr.Factory.prototype.expect=function(B,A){this.expectations.push({expect:B,response:A})
};
fakeXhr.Factory.prototype.find=function(B){this.testcase.assertTrue(this.expectations.length>0);
var A=this.expectations.shift();
A.expect.checkMatch(this.testcase,B);
return A.response
};
fakeXhr.Factory.prototype.getXhrConstructor=function(){var A=this;
return function(){return new fakeXhr.Request(A)
}
};
fakeXhr.Request=function(A){this.factory=A;
this.actual=new fakeXhr.Expectation(null,null);
this.response=null;
this.onreadystatechange=null
};
fakeXhr.Request.prototype.open=function(C,A,B){this.actual.setMethod(C);
this.actual.setUrl(A)
};
fakeXhr.Request.prototype.setRequestHeader=function(A,B){this.actual.setHeader(A,B)
};
fakeXhr.Request.prototype.send=function(A){this.actual.bodyArgs=this.actual.parseForm(A);
var B=this.factory.find(this.actual);
this.readyState=4;
this.status=B.status;
this.responseText=B.responseText;
this.onreadystatechange()
};