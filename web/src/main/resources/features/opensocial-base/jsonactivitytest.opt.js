var gadgets=gadgets||{};
var opensocial=opensocial||{};
function JsonActivityTest(A){TestCase.call(this,A)
}JsonActivityTest.inherits(TestCase);
JsonActivityTest.prototype.setUp=function(){this.oldGetField=opensocial.Container.getField;
opensocial.Container.getField=function(A,B,C){return A[B]
}
};
JsonActivityTest.prototype.tearDown=function(){opensocial.Container.getField=this.oldGetField
};
JsonActivityTest.prototype.testConstructArrayObject=function(){var B={fakeClass:[{field1:"value1"},{field2:"value2"}]};
FakeClass=function(C){this.fields=C
};
JsonActivity.constructArrayObject(B,"fakeClass",FakeClass);
var A=B.fakeClass;
this.assertTrue(A instanceof Array);
this.assertTrue(A[0] instanceof FakeClass);
this.assertTrue(A[1] instanceof FakeClass);
this.assertEquals("value1",A[0].fields.field1);
this.assertEquals("value2",A[1].fields.field2)
};
JsonActivityTest.prototype.testJsonActivityConstructor=function(){var D=new JsonActivity({title:"green",mediaItems:[{mimeType:"black",url:"white",type:"orange"}]});
var A=opensocial.Activity.Field;
this.assertEquals("green",D.getField(A.TITLE));
var C=D.getField(A.MEDIA_ITEMS);
this.assertTrue(C instanceof Array);
this.assertTrue(C[0] instanceof JsonMediaItem);
var B=opensocial.MediaItem.Field;
this.assertEquals("black",C[0].getField(B.MIME_TYPE));
this.assertEquals("white",C[0].getField(B.URL));
this.assertEquals("orange",C[0].getField(B.TYPE))
};
JsonActivityTest.prototype.testJsonMediaItemConstructor=function(){var B=new JsonMediaItem({mimeType:"black",url:"white",type:"orange"});
var A=opensocial.MediaItem.Field;
this.assertEquals("black",B.getField(A.MIME_TYPE));
this.assertEquals("white",B.getField(A.URL));
this.assertEquals("orange",B.getField(A.TYPE))
};