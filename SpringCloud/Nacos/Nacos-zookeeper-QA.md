# springcloud整合dubbo
#### zookeeper 作为注册中心

1. ```
   Q：
   ***************************
   APPLICATION FAILED TO START
   ***************************
   
   Description:
   
   Field autoServiceRegistration in org.springframework.cloud.client.serviceregistry.AutoServiceRegistrationAutoConfiguration required a single bean, but 2 were found:
   	- zookeeperAutoServiceRegistration: defined by method 'zookeeperAutoServiceRegistration' in class path resource [org/springframework/cloud/zookeeper/serviceregistry/ZookeeperAutoServiceRegistrationAutoConfiguration.class]
   	- nacosAutoServiceRegistration: defined by method 'nacosAutoServiceRegistration' in class path resource [org/springframework/cloud/alibaba/nacos/NacosDiscoveryAutoConfiguration.class]
   
   
   Action:
   
   Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
   
   
   ```

   ```
   A：出现这种问题可能是由于配置了多个注册中心，AutoServiceRegistrationConfiguration不知道加载哪个。
   设置spring.cloud.service-registry.auto-registration.enabled=false，不自动加载，然后手动加载dubbo.xml文件或者其他方式加载即可。
   ```

   

2. ```
   Q：
   Caused by: org.apache.zookeeper.KeeperException$UnimplementedException: KeeperErrorCode = Unimplemented for /dubbo/zhaohe.test.service.HelloService/providers/dubbo%3A%2F%2F172.16.124.148%3A20880%2Fzhaohe.test.service......
   ```

   ```
   A:安装的zookeeper版本和pom依赖的zookeeper版本不一致导致。如果安装的版本是zookeeper-3.4.14，可以修改pom依赖版本为：
   		<dependency>
   			<groupId>org.springframework.cloud</groupId>
   			<artifactId>spring-cloud-starter-zookeeper-discovery</artifactId>
   			<exclusions>
   				<exclusion>
   					<groupId>org.apache.zookeeper</groupId>
   					<artifactId>zookeeper</artifactId>
   				</exclusion>
   			</exclusions>
   		</dependency>
   		<dependency>
   			<groupId>org.apache.zookeeper</groupId>
   			<artifactId>zookeeper</artifactId>
   			<version>3.4.14</version>
   		</dependency>
   ```

   

#### nacos作为注册中心