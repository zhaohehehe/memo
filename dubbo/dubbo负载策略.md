## Dubbo负载均衡调优

#### DUBBO提供的负载均衡策略(来自：https://dubbo.apache.org/zh/docs/v2.7/user/examples/loadbalance/)
1. Random LoadBalance
> 随机，按权重设置随机概率。
在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重。
2. RoundRobin LoadBalance
> 轮询，按公约后的权重设置轮询比率。
存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上。
3. LeastActive LoadBalance
> 最少活跃调用数，相同活跃数的随机，活跃数指调用前后计数差。
使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大。

  什么是actives?
  参见：https://blog.csdn.net/yuanshangshenghuo/article/details/107886517
  ```
  // 服务消费者实现，而且需要配置actives属性才能激活
  @Activate(group = CONSUMER, value = ACTIVES_KEY)
  public class ActiveLimitFilter implements Filter, Filter.Listener{
    ......
  }
  ```
4. ConsistentHash LoadBalance
> 一致性 Hash，相同参数的请求总是发到同一提供者。
当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动。
算法参见：http://en.wikipedia.org/wiki/Consistent_hashing

以上1、2、4都有可能出现慢的提供者累积请求的现象，如果某台机器因为资源不足等原因变慢，那最好的解决办法是跳过这台机器或者逐渐减少这台机器的访问（逐渐减压）。
3对于消费端同步请求来说可能不会出现累积请求的现象，但是对于异步请求来说，所有的请求正常都会有很快的相应，当时实际业务执行会有快慢，依然会存在请求累积。所以在上述策略基础上，可能需要动态调整权重或者活跃数(actives)来避免负载不均。由于活跃数无法动态调整，但是可以调整权重。

#### 动态调整权重
> https://cloud.tencent.com/developer/article/1658797 中提及：<br>
dubbo的配置会有优先级问题，最简单的理解，它不仅可以在服务提供端配置，也可在服务消费端配置，反正最后都是通过URL传递的。但是，负载均衡的权重配置如果是动态修改，那就只能在服务提供端配置。为什么呢？<br>
负载均衡是在消费端（消费服务时会从invokers列表中doSelect）实现的，权重起作用肯定也是在消费端。而动态修改元数据只有订阅者会收到，因为服务提供者不订阅消费者。
