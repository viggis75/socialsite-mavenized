var gadgets=gadgets||{};
var opensocial=opensocial||{};
function ActivityTest(A){TestCase.call(this,A)
}ActivityTest.inherits(TestCase);
ActivityTest.prototype.setUp=function(){gadgets.util=gadgets.util||{};
this.oldEscape=gadgets.util.escape;
gadgets.util.escape=function(A){return A
}
};
ActivityTest.prototype.tearDown=function(){gadgets.util.escape=this.oldEscape
};
ActivityTest.prototype.testSetField=function(){var A=new opensocial.Activity({title:"yellow"});
this.assertEquals("yellow",A.getField("title"));
A.setField("title","purple");
this.assertEquals("purple",A.getField("title"))
};