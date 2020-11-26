## tomcat相关命令

1. catalina.sh添加远程调试
	
	```
	CATALINA_OPTS="-server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5007"
	```
2. startup.sh添加远程调试(-x表示指定的变量会成为环境变量，可供shell以外的程序来使用)

	```
	declare -x CATALINA_OPTS="-server -Xdebug -Xnoagent -Djava.compiler=NONE -Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=5007"  
	```


