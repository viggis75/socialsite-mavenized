function verifyNamespace(A){var B=os.getNamespace(A);
if(!B){B=os.createNamespace(A)
}return B
}function testRequestHandler(){verifyNamespace("test");
var A={};
os.data.registerRequestHandler("test:request",function(C){A[C.key]=C.getAttribute("data")
});
var B='<test:request key="first" data="testData"/><test:request key="second" data="${foo}"/>';
os.data.loadRequests(B);
assertNotNull(os.data.requests_.first);
assertNotNull(os.data.requests_.second);
os.data.DataContext.putDataSet("foo","bar");
os.data.executeRequests();
assertEquals("testData",A.first);
assertEquals("bar",A.second)
}function testPutDataSet(){var A="test1";
var B="foo";
os.data.DataContext.putDataSet(A,B);
assertEquals(B,os.data.DataContext.getDataSet(A))
}function testListener(){var A=false;
os.data.DataContext.registerListener("testKey",function(){A=true
});
os.data.DataContext.putDataSet("testKey",{});
assertEquals(true,A)
};