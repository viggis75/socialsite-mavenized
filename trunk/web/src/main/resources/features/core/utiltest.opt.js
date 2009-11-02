function UtilTest(A){TestCase.call(this,A)
}UtilTest.inherits(TestCase);
UtilTest.prototype.setUp=function(){};
UtilTest.prototype.tearDown=function(){};
UtilTest.prototype.testMakeEnum=function(){var B=["Foo","BAR","baz"];
var A=gadgets.util.makeEnum(B);
this.assertEquals("Foo",A.Foo);
this.assertEquals("BAR",A.BAR);
this.assertEquals("baz",A.baz)
};