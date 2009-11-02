os.nsmap_={};
os.nsurls_={};
os.createNamespace=function(C,B){var A=os.nsmap_[C];
if(!A){A={};
os.nsmap_[C]=A;
os.nsurls_[C]=B
}else{if(os.nsurls_[C]!=B){throw ("Namespace "+C+" already defined with url "+os.nsurls_[C])
}}return A
};
os.getNamespace=function(A){return os.nsmap_[A]
};
os.addNamespace=function(C,B,A){if(os.nsmap_[C]){throw ("Namespace '"+C+"' already exists!")
}os.nsmap_[C]=A;
os.nsurls_[C]=B
};
os.getCustomTag=function(C,B){var A=os.nsmap_[C];
if(!A){return null
}if(A.getTag){return A.getTag(B)
}else{return A[B]
}};
os.getRequiredNamespaces=function(A){var C="";
for(var B in os.nsurls_){if(A.indexOf("<"+B+":")>=0&&A.indexOf("xmlns:"+B+":")<0){C+=" xmlns:"+B+'="'+os.nsurls_[B]+'"'
}}return C
};
os.defineBuiltinTags=function(){var C=os.getNamespace("os")||os.createNamespace("os","http://opensocial.com/#template");
C.Render=function(E,I,D){var K=D.getVariable(os.VAR_parentnode);
var G=E.getAttribute("content")||"*";
var M=os.getValueFromNode_(K,G);
if(!M){return""
}else{if(typeof (M)=="string"){var F=document.createTextNode(M);
M=[];
M.push(F)
}else{if(!isArray(M)){var L=[];
for(var H=0;
H<M.childNodes.length;
H++){L.push(M.childNodes[H])
}M=L
}else{if(G!="*"&&M.length==1&&M[0].nodeType==DOM_ELEMENT_NODE){var L=[];
for(var H=0;
H<M[0].childNodes.length;
H++){L.push(M[0].childNodes[H])
}M=L
}}}}if(os.isIe){for(var H=0;
H<M.length;
H++){if(M[H].nodeType==DOM_TEXT_NODE){var J=os.trimWhitespaceForIE_(M[H].nodeValue,(H==0),(H==M.length-1));
if(J!=M[H].nodeValue){M[H].parentNode.removeChild(M[H]);
M[H]=document.createTextNode(J)
}}}}return M
};
C.render=C.RenderAll=C.renderAll=C.Render;
C.Html=function(E){var D=E.code?""+E.code:E.getAttribute("code")||"";
return D
};
function B(D,E){return function(){E.apply(D)
}
}function A(H,G,I,D){var F=D.getVariable(os.VAR_callbacks);
var E=new Function(G);
F.push(B(H,E))
}os.registerAttribute("onAttach",A)
};
os.defineBuiltinTags();