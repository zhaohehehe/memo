## zookeeper Session

- 客户端与服务端的任何交换操作都与会话相关，包括：临时节点的生命周期，客户端的请求顺序执行以及Watcher通知机制。

- 在ZooKeeper中，客户端和服务端建立长连接，服务端通过客户端上报heart_beat（上报有一定的频率）来确定客户端的存活状态，每一次上报都会重置下次session_timeout。

  1. 网络不稳定或是其他原因（网络闪断、客户端连接的zk机器挂了）导致连接断开，客户端会感知到<strong style="color:red">CONNECTIONLOSS(连接断开)异常</strong>;
  2. 感知到CONNECTIONLOSS异常之后，客户端会主动在地址列表中选择新的地址进行连接。如果连接新地址时间未超过session_timeout之前处于<strong style="color:red">SUSPENDED</strong>状态（此时临时节点还存在），连接新地址成功之后处于<strong style="color:red">RECONNECTED</strong>状态（此时会自动新建临时节点）。
  3. 如果没有新地址可以选择，处于<strong style="color:red">LOST</strong>。如果选择耗时过长，超过了session_timeout后还是没有成功连接上服务器，那么服务器认为这个Session已经结束了（服务器无法确认是因为其它异常原因还是客户端主动结束会话）。
  4. 由于在ZK中，很多数据和状态都是和会话绑定的，一旦会话失效，那么ZK就开始清除和这个会话有关的信息，包括这个会话创建的临时节点和注册的所有Watcher。
  5. 如果在这之后，由于网络恢复后，客户端可能会重新连接上服务器，但是很不幸，服务器会告诉客户端<strong style="color:red">SESSIONEXPIRED（会话过期）异常</strong>。此时客户端的状态变成 CLOSED状态，应用可以根据实际情况实现自己的重连逻辑：重新实例zookeeper对象，然后重新操作所有临时数据（包括临时节点和注册Watcher）。					

## 掉线重连
- zookeeper listener处理逻辑(一下以解决临时节点由于网络闪退消失为例)：

  ```
  new ConnectionStateListener() {
  
  				@Override
  			public void stateChanged(CuratorFramework arg0, ConnectionState arg1) {
  					if (arg1 == ConnectionState.CONNECTED) {
  						log.debug("ConnectionStateListener:CONNECTED");
  					} else if (arg1 == ConnectionState.RECONNECTED) {
  						log.debug("ConnectionStateListener:RECONNECTED==========");
  						reStartClear(ephemeralNodePath);//!!!先删除ephemeralNodePath
  						registerNodes(ephemeralNodePath);//再创建ephemeralNodePath
  					} else if (arg1 == ConnectionState.SUSPENDED) {
  						log.debug("ConnectionStateListener:SUSPENDED===========");
  					} else if (arg1 == ConnectionState.LOST) {
  						log.debug("ConnectionStateListener:LOST");
  						try {
  							// 重新连接
  						   CuratorFramework client = getZooKeeperClient();									   client.getConnectionStateListenable().removeListener(this);
  						   reStartClear(ephemeralNodePath);//!!!先删除ephemeralNodePath
  						   registerNodes(ephemeralNodePath);//再创建ephemeralNodePath
  						   client.getConnectionStateListenable().addListener(this);
  						} catch (RuntimeException e) {
  							log.error("ConnectionStateListener重连超时", e);
  						} catch (Exception e) {
  							log.error("ConnectionStateListener重连异常", e);
  						}
  					}
  				}
  			}
  ```

  

- session_timeout应是tickTime的整数倍，且不小于minSessionTimeout，不大于maxSessionTimeout ；

- minSessionTimeout默认值为  2 * tickTime；

- maxSessionTimout默认值为 20 * tickTime；

-  tickTime是ZooKeeper的一个配置项，是ZooKeeper内部控制时间逻辑的最小时间单位，默认值为2000ms；

- 如果设置的session_timeout超出上述范围，会被ZooKeeper Server截取为minSessionTimeout或maxSessionTimeout
