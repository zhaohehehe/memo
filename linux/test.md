

## ssh简介

1. SSH仅仅是一协议标准，目的是实现安全远程登录以及其它安全网络服务。其具体的实现有很多，既有开源实现的OpenSSH，也有商业实现方案。
2. 工作原理：
	1. 对称加密：client端：encryp(明文,秘钥K); server端：decryp(密文,秘钥K);
	
	  > 但是秘钥K怎么安全保存呢？
	
	2. 非对称加密：
	
		1. 远程Server收到Client的登录请求，Server把自己的公钥发给用户。
		```
		#client:ssh root@172.16.11.12
		```
		2. Client使用这个公钥，将密码进行加密。
		3. Client将加密的密码发送给Server端。
		```
		#client:encrypt(password明文,公钥)
		```
		4. 远程Server用自己的私钥，解密登录密码，然后验证其合法性。
		```
		#server:decrypt(password密文,私钥)
		#server:check(password)
		```
		5. 若验证通过或者失败，给Client相应的响应。
	 > client端如何保证接受到的公钥就是目标Server端的？容易产生中间人攻击
	3. ssh解决上述问题：
		1. 基于口令的认证：对server的公钥进行验证
		```
		server端自己对公钥进行认证，初次登陆的时候，系统给出提示：
		The authenticity of host 'ssh-server.example.com (12.18.429.21)' can't be established.
RSA key fingerprint is 98:2e:d7:e0:de:9f:ac:67:28:c2:42:2d:37:16:58:4d.
Are you sure you want to continue connecting (yes/no)?
（无法确认主机的真实性，是否继续连接？RSA公钥有1024位，不好保存，所以用公钥生成128位的指纹）
		如果yes,该server就会进入known_hosts中，接下来可以通过密码登录。
		```
		2. 基于公钥认证
		```
		免密登录，上面每次登录都需要输入密码。类似于github的SSH Keys设置。
			1. client将自己的公钥存放在Server上，追加在文件authorized_keys中。
			2. #client:ssh root@172.16.11.12
			3. Server端接收到Client的连接请求后，会在authorized_keys中匹配到Client的公钥pubKey，并生成随机数R，用Client的公钥对该随机数进行加密得到pubKey(R)，然后将加密后信息发送给Client。
			4. #server:encrypt(明文R,Client公钥)
			5. Client端通过私钥进行解密得到随机数R:
			6. #client:decrypt(密文R,Client私钥)=R
			7. 然后对随机数R和本次会话的SessionKey利用MD5生成摘要Digest1，	发送给Server端。Server端会也会对R和SessionKey利用同样摘要算法生成Digest2。Server端会最后比较Digest1和Digest2是否相同，完成认证过程。接下来可以通过密码登录。
		```
## ssh命令



