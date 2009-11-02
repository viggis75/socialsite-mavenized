function PrefsTest(A){TestCase.call(this,A)
}PrefsTest.inherits(TestCase);
PrefsTest.prototype.setUp=function(){this.params={myCounter:100,myString:"15.3",myUndefined:undefined,myObject:{},myFloat:3.3,myBool:true,myArray:["one","two"],boolString:"true"}
};
PrefsTest.prototype.tearDown=function(){this.params=undefined
};
PrefsTest.prototype.testGetInt=function(){var B={myCounter:100,myString:15,myUndefined:0,myObject:0,myFloat:3};
var A=new gadgets.Prefs();
gadgets.Prefs.setInternal_(this.params,100);
for(var C in B){this.assertEquals(B[C],A.getInt(C))
}};
PrefsTest.prototype.testGetFloat=function(){var B={myCounter:100,myString:15.3,myUndefined:0,myObject:0,myFloat:3.3};
var A=new gadgets.Prefs();
gadgets.Prefs.setInternal_(this.params,100);
for(var C in B){this.assertEquals(B[C],A.getFloat(C))
}};
PrefsTest.prototype.testGetBool=function(){var B={myCounter:true,myString:true,myUndefined:false,myObject:false,myFloat:true,myBool:true,boolString:true,myArray:false};
var A=new gadgets.Prefs();
gadgets.Prefs.setInternal_(this.params,100);
for(var C in B){this.assertEquals(B[C],A.getBool(C))
}};