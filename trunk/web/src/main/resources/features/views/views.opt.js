var gadgets=gadgets||{};
gadgets.views=function(){var D=null;
var A={};
var C={};
function B(H){var E=H.views;
for(var K in E){if(E.hasOwnProperty(K)){var L=E[K];
if(!L){continue
}A[K]=new gadgets.views.View(K,L.isOnlyVisible);
var F=L.aliases||[];
for(var J=0,I;
I=F[J];
++J){A[I]=new gadgets.views.View(K,L.isOnlyVisible)
}}}var G=gadgets.util.getUrlParameters();
if(G["view-params"]){C=gadgets.json.parse(G["view-params"])||C
}D=A[G.view]||A["default"]
}gadgets.config.register("views",null,B);
return{bind:function(T,R){if(typeof T!="string"){throw new Error("Invalid urlTemplate")
}if(typeof R!="object"){throw new Error("Invalid environment")
}var P=/^([a-zA-Z0-9][a-zA-Z0-9_\.\-]*)(=([a-zA-Z0-9\-\._~]|(%[0-9a-fA-F]{2}))*)?$/,V=new RegExp("\\{([^}]*)\\}","g"),S=/^-([a-zA-Z]+)\|([^|]*)\|(.+)$/,L=[],O=0,J,I,G,N,K,F,M,Q;
function H(X,W){return R.hasOwnProperty(X)?R[X]:W
}function E(W){if(!(I=W.match(P))){throw new Error("Invalid variable : "+W)
}}function U(a,W,Z){var X,Y=a.split(",");
for(X=0;
X<Y.length;
++X){E(Y[X]);
if(Z(W,H(I[1]),I[1])){break
}}return W
}while(J=V.exec(T)){L.push(T.substring(O,J.index));
O=V.lastIndex;
if(I=J[1].match(P)){G=I[1];
N=I[2]?I[2].substr(1):"";
L.push(H(G,N))
}else{if(I=J[1].match(S)){K=I[1];
F=I[2];
M=I[3];
Q=0;
switch(K){case"neg":Q=1;
case"opt":if(U(M,{flag:Q},function(X,W){if(typeof W!="undefined"&&(typeof W!="object"||W.length)){X.flag=!X.flag;
return 1
}}).flag){L.push(F)
}break;
case"join":L.push(U(M,[],function(Y,X,W){if(typeof X==="string"){Y.push(W+"="+X)
}}).join(F));
break;
case"list":E(M);
value=H(I[1]);
if(typeof value==="object"&&typeof value.join==="function"){L.push(value.join(F))
}break;
case"prefix":Q=1;
case"suffix":E(M);
value=H(I[1],I[2]&&I[2].substr(1));
if(typeof value==="string"){L.push(Q?F+value:value+F)
}else{if(typeof value==="object"&&typeof value.join==="function"){L.push(Q?F+value.join(F):value.join(F)+F)
}}break;
default:throw new Error("Invalid operator : "+K)
}}else{throw new Error("Invalid syntax : "+J[0])
}}}L.push(T.substr(O));
return L.join("")
},requestNavigateTo:function(E,G,F){gadgets.rpc.call(null,"requestNavigateTo",null,E.getName(),G,F)
},getCurrentView:function(){return D
},getSupportedViews:function(){return A
},getParams:function(){return C
}}
}();
gadgets.views.View=function(A,B){this.name_=A;
this.isOnlyVisible_=!!B
};
gadgets.views.View.prototype.getName=function(){return this.name_
};
gadgets.views.View.prototype.getUrlTemplate=function(){return gadgets.config&&gadgets.config.views&&gadgets.config.views[this.name_]&&gadgets.config.views[this.name_].urlTemplate
};
gadgets.views.View.prototype.bind=function(A){return gadgets.views.bind(this.getUrlTemplate(),A)
};
gadgets.views.View.prototype.isOnlyVisibleGadget=function(){return this.isOnlyVisible_
};
gadgets.views.ViewType=gadgets.util.makeEnum(["CANVAS","HOME","PREVIEW","PROFILE","FULL_PAGE","DASHBOARD","POPUP"]);