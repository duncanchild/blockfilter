$.ajax(
type:'GET',
url:"http://example.com/users/feeds/",
data:"format=json&id=123",
success:function(feed) {
document.write(feed);
},
dataType:'jsonp'
);