window.opensocial.data=window.opensocial.data||{};
var gadgets=window.gadgets;
os.data=window.opensocial.data;
os.data.ATTR_KEY="key";
os.data.SCRIPT_TYPE="text/os-data";
os.data.RequestDescriptor=function(C){this.tagName=C.tagName;
this.tagParts=this.tagName.split(":");
this.attributes={};
this.dependencies=false;
for(var B=0;
B<C.attributes.length;
++B){var A=C.attributes[B].nodeName;
if(A){var D=C.getAttribute(A);
if(A&&D){this.attributes[A]=D;
this.computeNeededKeys_(D)
}}}this.key=this.attributes[os.data.ATTR_KEY];
this.register_()
};
os.data.RequestDescriptor.prototype.hasAttribute=function(A){return !!this.attributes[A]
};
os.data.RequestDescriptor.prototype.getAttribute=function(B){var A=this.attributes[B];
if(!A){return A
}var C=os.parseAttribute_(A);
if(!C){return A
}return os.data.DataContext.evalExpression(C)
};
os.data.RequestDescriptor.prototype.sendRequest=function(){var A=os.getCustomTag(this.tagParts[0],this.tagParts[1]);
if(!A){throw"Data handler undefined for "+this.tagName
}A(this)
};
os.data.RequestDescriptor.prototype.getSendRequestClosure=function(){var A=this;
return function(){A.sendRequest()
}
};
os.data.RequestDescriptor.prototype.computeNeededKeys_=function(E){var A=os.regExps_.VARIABLE_SUBSTITUTION;
var B=E.match(A);
while(B){var D=B[2].substring(2,B[2].length-1);
var C=D.split(".")[0];
if(!this.neededKeys){this.neededKeys={}
}this.neededKeys[C]=true;
B=B[3].match(A)
}};
os.data.RequestDescriptor.prototype.register_=function(){os.data.registerRequestDescriptor(this)
};
os.data.DataContext={};
os.data.DataContext.listeners_=[];
os.data.DataContext.dataSets_={};
os.data.DataContext.evalContext_=os.createContext(os.data.DataContext.dataSets_);
os.data.DataContext.getContext=function(){return os.data.DataContext.evalContext_
};
os.data.DataContext.registerListener=function(B,D){var C={};
C.keys={};
if(typeof (B)=="object"){for(var A in B){C.keys[B[A]]=true
}}else{C.keys[B]=true
}C.callback=D;
os.data.DataContext.listeners_.push(C);
if(os.data.DataContext.isDataReady(C.keys)){window.setTimeout(function(){C.callback()
},1)
}};
os.data.DataContext.getDataSet=function(A){return os.data.DataContext.dataSets_[A]
};
os.data.DataContext.isDataReady=function(B){for(var A in B){if(os.data.DataContext.getDataSet(A)==null){return false
}}return true
};
os.data.DataContext.putDataSet=function(A,C){var B=C;
if(typeof (B)=="undefined"||B===null){return 
}if(B.getData){B=B.getData();
B=B.array_||B
}os.data.DataContext.dataSets_[A]=B;
os.data.DataContext.fireCallbacks_(A)
};
os.data.DataContext.evalExpression=function(A){return os.data.DataContext.evalContext_.evalExpression(A)
};
os.data.DataContext.maybeFireListener_=function(B,A){if(os.data.DataContext.isDataReady(B.keys)){B.callback(A)
}};
os.data.DataContext.fireCallbacks_=function(B){for(var A=0;
A<os.data.DataContext.listeners_.length;
++A){var C=os.data.DataContext.listeners_[A];
if(C.keys[B]!=null){os.data.DataContext.maybeFireListener_(C,B)
}}};
os.data.getDataContext=function(){return os.data.DataContext
};
os.data.requests_={};
os.data.registerRequestDescriptor=function(A){if(os.data.requests_[A.key]){throw"Request already registered for "+A.key
}os.data.requests_[A.key]=A
};
os.data.currentAPIRequest_=null;
os.data.currentAPIRequestKeys_=null;
os.data.currentAPIRequestCallbacks_=null;
os.data.getCurrentAPIRequest=function(){if(!os.data.currentAPIRequest_){os.data.currentAPIRequest_=opensocial.newDataRequest();
os.data.currentAPIRequestKeys_=[];
os.data.currentAPIRequestCallbacks_={}
}return os.data.currentAPIRequest_
};
os.data.addToCurrentAPIRequest=function(C,B,A){os.data.getCurrentAPIRequest().add(C,B);
os.data.currentAPIRequestKeys_.push(B);
if(A){os.data.currentAPIRequestCallbacks_[B]=A
}window.setTimeout(os.data.sendCurrentAPIRequest_,0)
};
os.data.sendCurrentAPIRequest_=function(){if(os.data.currentAPIRequest_){os.data.currentAPIRequest_.send(os.data.createSharedRequestCallback_());
os.data.currentAPIRequest_=null
}};
os.data.createSharedRequestCallback_=function(){var B=os.data.currentAPIRequestKeys_;
var A=os.data.currentAPIRequestCallbacks_;
return function(C){os.data.onAPIResponse(C,B,A)
}
};
os.data.onAPIResponse=function(F,E,D){for(var B=0;
B<E.length;
B++){var A=E[B];
var C=F.get(A);
if(D[A]){D[A](A,C)
}else{os.data.DataContext.putDataSet(A,C)
}}};
os.data.registerRequestHandler=function(B,D){var A=B.split(":");
var C=os.getNamespace(A[0]);
if(!C){throw"Namespace "+A[0]+" is undefined."
}else{if(C[A[1]]){throw"Request handler "+A[1]+" is already defined."
}}C[A[1]]=D
};
os.data.processDocumentMarkup=function(D){var E=D||document;
var A=E.getElementsByTagName("script");
for(var B=0;
B<A.length;
++B){var C=A[B];
if(C.type==os.data.SCRIPT_TYPE){os.data.loadRequests(C)
}}os.data.registerRequestDependencies();
os.data.executeRequests()
};
if(window.gadgets&&window.gadgets["util"]){gadgets.util.registerOnLoadHandler(os.data.processDocumentMarkup)
}os.data.loadRequests=function(A){if(typeof (A)=="string"){os.data.loadRequestsFromMarkup_(A);
return 
}var B=A;
A=B.value||B.innerHTML;
os.data.loadRequestsFromMarkup_(A)
};
os.data.loadRequestsFromMarkup_=function(A){A=os.prepareTemplateXML_(A);
var C=os.parseXML_(A);
var B=C.firstChild;
while(B.nodeType!=DOM_ELEMENT_NODE){B=B.nextSibling
}os.data.processDataNode_(B)
};
os.data.processDataNode_=function(A){for(var C=A.firstChild;
C;
C=C.nextSibling){if(C.nodeType==DOM_ELEMENT_NODE){var B=new os.data.RequestDescriptor(C)
}}};
os.data.registerRequestDependencies=function(){for(var A in os.data.requests_){var C=os.data.requests_[A];
var E=C.neededKeys;
var D=[];
for(var B in E){if(os.data.DataContext.getDataSet(B)==null&&os.data.requests_[B]){D.push(B)
}}if(D.length>0){os.data.DataContext.registerListener(D,C.getSendRequestClosure());
C.dependencies=true
}}};
os.data.executeRequests=function(){for(var A in os.data.requests_){var B=os.data.requests_[A];
if(!B.dependencies){B.sendRequest()
}}};
os.data.transformSpecialValue=function(A){if(A.substring(0,1)=="@"){return A.substring(1).toUpperCase()
}return A
};
(function(){os.data.registerRequestHandler("os:ViewerRequest",function(B){var A=os.data.getCurrentAPIRequest().newFetchPersonRequest("VIEWER");
os.data.addToCurrentAPIRequest(A,B.key)
});
os.data.registerRequestHandler("os:OwnerRequest",function(B){var A=os.data.getCurrentAPIRequest().newFetchPersonRequest("OWNER");
os.data.addToCurrentAPIRequest(A,B.key)
});
os.data.registerRequestHandler("os:PeopleRequest",function(E){var C=E.getAttribute("userId");
var B=E.getAttribute("groupId")||"@self";
var A={};
A.userId=os.data.transformSpecialValue(C);
if(B!="@self"){A.groupId=os.data.transformSpecialValue(B)
}var D=os.data.getCurrentAPIRequest().newFetchPeopleRequest(opensocial.newIdSpec(A));
os.data.addToCurrentAPIRequest(D,E.key)
});
os.data.registerRequestHandler("os:DataRequest",function(C){var A=C.getAttribute("href");
var B=C.getAttribute("format")||"json";
var D={};
D[gadgets.io.RequestParameters.CONTENT_TYPE]=B.toLowerCase()=="text"?gadgets.io.ContentType.TEXT:gadgets.io.ContentType.JSON;
D[gadgets.io.RequestParameters.METHOD]=gadgets.io.MethodType.GET;
gadgets.io.makeRequest(A,function(E){os.data.DataContext.putDataSet(C.key,E.data)
},D)
})
})();
(os.data.populateParams_=function(){if(gadgets.util.hasFeature("views")){os.data.DataContext.putDataSet("ViewParams",gadgets.views.getParams())
}})();