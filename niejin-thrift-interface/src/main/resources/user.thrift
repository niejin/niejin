namespace java com.duowan.niejin.thrift
struct User{
	1:i32 id
	2:string name
}

service UserService{
	User getUser(1:i32 id)
}