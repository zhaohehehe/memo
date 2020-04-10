# 什么时候使用dubbo?

官方文档：http://dubbo.apache.org/zh-cn/docs/user/references/protocol/dubbo.html

dubbo配置参考：<https://blog.csdn.net/niugang0920/article/details/81975421>

stub和skeleton 参考：<https://blog.csdn.net/weixin_30794499/article/details/96275668>

# dubbo://

Dubbo 缺省协议采用单一长连接和 NIO 异步通讯，适合于小数据量大并发的服务调用，以及服务消费者机器数远大于服务提供者机器数的情况。

反之，Dubbo 缺省协议不适合传送大数据量的服务，比如传文件，传视频等，除非请求量很低。

![dubbo-protocol.jpg](http://dubbo.apache.org/docs/zh-cn/user/sources/images/dubbo-protocol.jpg)

- Transporter: mina, netty, grizzy
- Serialization: dubbo, hessian2, java, json
- Dispatcher: all, direct, message, execution, connection
- ThreadPool: fixed, cached

## 特性

缺省协议，使用基于 mina `1.1.7` 和 hessian `3.2.1` 的 tbremoting 交互。

- 连接个数：单连接
- 连接方式：长连接
- 传输协议：TCP
- 传输方式：NIO 异步传输
- 序列化：Hessian 二进制序列化
- 适用范围：传入传出参数数据包较小（建议小于100K），消费者比提供者个数多，单一消费者无法压满提供者，尽量不要用 dubbo 协议传输大文件或超大字符串。
- 适用场景：常规远程服务方法调用

## 约束

- 参数及返回值需实现 `Serializable` 接口
- 参数及返回值不能自定义实现 `List`, `Map`, `Number`, `Date`, `Calendar` 等接口，只能用 JDK 自带的实现，因为 hessian 会做特殊处理，自定义实现类中的属性值都会丢失。
- Hessian 序列化，只传成员属性值和值的类型，不传方法或静态变量，兼容情况 [[1\]](http://dubbo.apache.org/zh-cn/docs/user/references/protocol/dubbo.html#fn1)[[2\]](http://dubbo.apache.org/zh-cn/docs/user/references/protocol/dubbo.html#fn2)：

## NIO

- [什么是NIO]: https://blog.csdn.net/a972669015/article/details/93376691

  阻塞概念：应用程序在获取网络数据的时候，如果网络传输数据很慢，那么程序就一直等着，直到传输完毕。

  非阻塞概念：应用程序直接可以获取已经准备好的数据，无需等待。

  IO为同步阻塞形式，NIO为同步非阻塞形式。NIO并没有实现异步，在JDK1.7之后，升级了NIO库包，支持异步非阻塞通信模型即NIO2.0（AIO）

  同步和异步：同步和异步一般是面向操作系统和应用程序对IO操作的层面上来区别的。

  同步时，应用程序会直接参与IO读写操作，并且我们的应用程序会直接阻塞到某一个方法上，直到数据准备就绪；或者采用轮询的方式实时检查数据的就绪状态，如果就绪则获取数据。

  异步时，则所有的IO读写操作交给操作系统处理，与我们应用程序没有直接关系，我们程序不需要关心IO读写，当操作系统完成了IO读写操作时，会给我们应用程序发送通知，我们的应用程序直接拿走数据即可。

-  BIO（同步且阻塞）  传统Socket编程。有阻塞模型，只要Client端和服务器端建立tcp连接之后，就需要三次握手，浪费性能。服务器端需要先启动，accept方法将服务器阻塞着，一个服务器进来，accept方法返回一个Socket对象，通过一个新线程进行数据处理。

- NIO（同步非阻塞） ：轮询。

- AIO（NIO2.0）（异步非阻塞） ：不需要自己实现轮询，由操作系统自己实现。

- IO(BIO)和NIO的区别:其本质就是阻塞和非阻塞的区别。

  ————————————————
  版权声明：本文为CSDN博主「Zau95255」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
  原文链接：https://blog.csdn.net/a972669015/article/details/93376691

## dubbo和http的区别

- dubbo的传输协议是TCP，属于

  [传输层协议]: https://www.cnblogs.com/Robin-YB/p/6668762.html

  而HTTP属于应用层协议。

- <strong style='color:red'>传输速度：dubbo减少了从应用层到传输层的数据传输，自然快于HTTP。</strong>

- ![img](http://img.blog.csdn.net/20160731161720376)