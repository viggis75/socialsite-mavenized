var gadgets=gadgets||{};
gadgets.config=function(){var A={};
return{register:function(D,C,B){if(A[D]){throw new Error('Component "'+D+'" is already registered.')
}A[D]={validators:C||{},callback:B}
},get:function(B){if(B){if(!A[B]){throw new Error('Component "'+B+'" not registered.')
}return configuration[B]||{}
}return configuration
},init:function(H,G){configuration=H;
for(var F in A){if(A.hasOwnProperty(F)){var E=A[F],D=H[F],B=E.validators;
if(!G){for(var C in B){if(B.hasOwnProperty(C)){if(!B[C](D[C])){throw new Error('Invalid config value "'+D[C]+'" for parameter "'+C+'" in component "'+F+'"')
}}}}if(E.callback){E.callback(H)
}}}},EnumValidator:function(E){var D=[];
if(arguments.length>1){for(var C=0,B;
B=arguments[C];
++C){D.push(B)
}}else{D=E
}return function(G){for(var F=0,H;
H=D[F];
++F){if(G===D[F]){return true
}}}
},RegExValidator:function(B){return function(C){return B.test(C)
}
},ExistsValidator:function(B){return typeof B!=="undefined"
},NonEmptyStringValidator:function(B){return typeof B==="string"&&B.length>0
},BooleanValidator:function(B){return typeof B==="boolean"
},LikeValidator:function(B){return function(D){for(var E in B){if(B.hasOwnProperty(E)){var C=B[E];
if(!C(D[E])){return false
}}}return true
}
}}
}();