## durid discard connection 原因

1. sql运行导致锁表，引起服务异常。

2. idle连接被数据库主动关闭，但是durid连接池不知道。

   例如：timeBetweenEvictionRunsMillis+minEvictableIdleTimeMillis>mysql wait_timeout

3. [mysql btroubleshooting 3.13.9、3.13.10]: https://dev.mysql.com/doc/connectors/en/connector-j-usagenotes-troubleshooting.html



### **连接池是怎么判断一条连接是Idle状态的？**

-  **minEvictableIdleTimeMillis**：最小空闲时间，默认30分钟，如果连接池中空闲连接数大于minIdle，并且空闲时间(idleMillis)大于minEvictableIdleTimeMillis，则连接池会将该空闲连接设置成Idle状态并关闭；也就是说如果一条连接30分钟都没有使用到，并且这种空闲连接的数量超过了minIdle，则这些连接就会被关闭了。

- **timeBetweenEvictionRunsMillis** 有两个含义： 
  1) Destroy线程回收连接，会检测连接的间隔时间 。
  2) testWhileIdle的判断依据，见testWhileIdle说明

- **testWhileIdle**

  建议配置为true，不影响性能，并且保证安全性。 
  申请连接的时候检测，如果空闲时间大于 timeBetweenEvictionRunsMillis，执行validationQuery检测连接是否有效，无效销毁。

  每隔timeBetweenEvictionRunsMillis秒对连接池进行一次检测,将空闲时间超过minEvictableIdleTimeMillis秒并且连接数超过minIdle的这部分连接进行销毁。

-  **maxEvictableIdleTimeMillis**：最大空闲时间，默认7小时，如果minIdle设置得比较大，**连接池中的空闲连接数一直没有超过minIdle，这时那些空闲连接是不是一直不用关闭**？当然不是，如果连接太久没用(mysql超过wait_timeout,show variables like 'wait_timeout')，数据库也会把它关闭，这时如果连接池不把这条连接关闭，系统就会拿到一条已经被数据库关闭的连接，会出现以下异常。

  ```
   ERROR com.alibaba.druid.pool.DruidDataSource:handleConnectionException(1241) - discard connection
   
  com.mysql.jdbc.exceptions.jdbc4.CommunicationsException: Communications link failure
  
  The last packet successfully received from the server was 7,875,041 milliseconds ago.  The last packet sent successfully to the server was 7,875,041 milliseconds ago.
  ```

  为了避免这种情况，Druid会判断池中的连接如果空闲时间大于maxEvictableIdleTimeMillis，也会强行把它关闭，而不用判断空闲连接数是否小于minIdle。

  所以，如果修改了mysql的wait_timeout,那么可以配置maxEvictableIdleTimeMillis判断可空闲最大时间,即使当前线程数等于minIdle也会强制回收,当然这个回收流程需要timeBetweenEvictionRunsMillis间隔时间后才会检测。

  **mysql推荐**：If your application really needs a non-stale connection, then your best option is to configure your pool correctly, i.e. don't let connections sit idle longer than 'wait_timeout' on the MySQL server, and have the pool test connections before handing them out.

  如果是mysql也可以配置autoReconnect=true，但是**不建议这么做！**

  [mysql Bug #5020]: https://bugs.mysql.com/bug.php?id=5020

  

  ```
  Note: Autoreconnect functionality will be depcreated and eventually removed in future releases. 
  
  The reason this isn't working for your particular case is that the methodolgy for autoreconnect was changed to be safer after 3.0.11, and is related to autoCommit state, which will also cause the current 'in-flight' transaction to fail (if you attempt your transaction again _after_ the failure, the driver will reconnect). Please see the docs for the explanation on how to correctly use this feature in the 'Troubleshooting' section.
  
  In any case, there is no 100% safe way that a JDBC driver can re-connect automatically if a TCP/IP connection dies without risking corruption of the database 'state' (even _with_ transactional semantics), which is why this feature will eventually be removed.
  
  The JDBC spec does not specify that a connection is alive no matter what happens to the underlying network for this very reason. 
  
  Clients of JDBC drivers are responsible for dealing with network failures, as only the application itself (really the developer of the application) 'knows' what the 'correct' response to a transaction failing due to the network going down is. 'Wait_timeout' expiring on the server is basically a 'forced' network failure by the server. You can correct this in a non-robust way by setting 'wait_timeout' higher, however, you as a developer should be handling SQL exceptions in your code and taking appropriate recovery actions, not just passing them up the call stack.
  
  Connection errors aways have a SQLState class of '08'. If you detect this, you can get another connection and retry the transaction (if it is appropriate).
  
  ```

  参见源码：com.alibaba.druid.pool.DruidDataSource.shrink(...)

  ```
   if (idleMillis >= minEvictableIdleTimeMillis) {
                          if (checkTime && i < checkCount) {//空闲连接大于minIdle
                              evictConnections[evictCount++] = connection;
                              continue;
                          } else if (idleMillis > maxEvictableIdleTimeMillis) {
                              evictConnections[evictCount++] = connection;
                              continue;
                          }
                      }
  ```