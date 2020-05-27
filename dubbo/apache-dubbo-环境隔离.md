## apache dubbo 服务注册分组用于环境隔离

1. dubbo版本2.0.5以上
2. 注册分组依赖于dubbo注册中心。如果绕过注册中心，点对点直连服务提供者地址，注册分组失效。
3. 配置注册中心的情况下，跨注册组的服务不会相互影响，也无法相互调用（环境隔离）。如果要保证正确调用服务，必须指定正确的注册组名称，确保调用者和被调用者在同一个注册组中。
4.  zookeeper服务注册分组配置：

```
	 <dubbo:registry group="${registryGroup}" protocol="zookeeper" transporter="curator" 
		address="${zookeeper.addresses}" username="${zookeeper.username}" 
		password="${zookeeper.password}" />
	或者：
		RegistryConfig registry = new RegistryConfig();
		registry.setRegister(true);
		registry.setAddress(registryAddress);
		registry.setProtocol(RegistryProtocol.ZOOKEEPER_PROTOCOL);
		registry.setGroup(registryGroup);
```

5. Nacos服务注册分组配置：

```
	<dubbo:registry protocol="nacos"
		address="nacos://${spring.cloud.nacos.discovery.server-addr}"
		username="nacos" password="nacos">
		<!-- NACOS DUBBO:https://nacos.io/zh-cn/docs/use-nacos-with-dubbo.html -->
		<dubbo:parameter key="namespace" value="${registryGroup}"/>
	</dubbo:registry>
	或者：
		RegistryConfig registry = new RegistryConfig();
		registry.setRegister(true);
		registry.setAddress(registryAddress);
		registry.setProtocol(RegistryProtocol.NACOS_PROTOCOL);
		if (registry.getParameters() == null) {
			registry.setParameters(new HashMap<>());
		}
		//Nacos对应namespace
		registry.getParameters().put(PropertyKeyConst.NAMESPACE, registryGroup);
	
```

## apache dubbo 服务分组

1. 为了区分同一接口的不同实现，可配置服务分组。

2. 配置服务分组要求dubbo版本1.0.7以上； 

3. 服务分组不依赖于dubbo注册中心。即使绕过注册中心，点对点直连服务提供者地址，依然需要配置服务分组名称，并且要和服务提供者保持一致；

   ```
   <dubbo:provider version="1.0" group="public" />
   或者
   <dubbo:reference version="1.0" group="public" />
   ```

   

## apache dubbo 服务版本

1. 服务版本号功能建议dubbo版本2.7.6以上；

2.  服务版本号不依赖于dubbo注册中心。即使绕过注册中心，点对点直连服务提供者地址，依然需要配置服务版本号，并且要和服务提供者保持一致；

   ```
   <dubbo:provider version="1.0" group="public" />
   或者
   <dubbo:reference version="1.0" group="public" />
   ```

   