function testResolveOpenSocialIdentifier(){var B=function(){this.foo="fooData";
this.bar_="barData";
this.thumbnailUrl_="thumbnailUrlData";
this.responseItem_={};
this.responseItem_.getData=function(){return"responseItemData"
}
};
B.prototype.getBar=function(){return this.bar_
};
B.prototype.getField=function(C){if(C=="THUMBNAIL_URL"){return this.thumbnailUrl_
}return null
};
B.prototype.get=function(C){if(C=="responseItem"){return this.responseItem_
}return null
};
var A=new B();
assertEquals("fooData",os.resolveOpenSocialIdentifier(A,"foo"));
assertEquals("barData",os.resolveOpenSocialIdentifier(A,"bar"));
assertEquals("thumbnailUrlData",os.resolveOpenSocialIdentifier(A,"THUMBNAIL_URL"));
assertEquals("responseItemData",os.resolveOpenSocialIdentifier(A,"responseItem"))
};