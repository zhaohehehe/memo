# apache dubbo 2.7.6

#### 获取reference报错：

#### org.apache.dubbo.registry.integration.RegistryDirectory

```
	public synchronized void notify(List<URL> urls) {
       	...
        refreshOverrideAndInvoker(providerURLs);
    }
    
    private void refreshOverrideAndInvoker(List<URL> urls) {
        // mock zookeeper://xxx?mock=return null
        overrideDirectoryUrl();
        refreshInvoker(urls);
    }
    
    private void refreshInvoker(List<URL> invokerUrls) {
        Assert.notNull(invokerUrls, "invokerUrls should not be null");
        if (invokerUrls.size() == 1
                && invokerUrls.get(0) != null
                && EMPTY_PROTOCOL.equals(invokerUrls.get(0).getProtocol())) {
            this.forbidden = true; // Forbid to access
            this.invokers = Collections.emptyList();
            routerChain.setInvokers(this.invokers);
            destroyAllInvokers(); // Close all invokers
        } else {
            this.forbidden = false; // Allow to access
            Map<String, Invoker<T>> oldUrlInvokerMap = this.urlInvokerMap; // local reference
            if (invokerUrls == Collections.<URL>emptyList()) {
                invokerUrls = new ArrayList<>();
            }
            if (invokerUrls.isEmpty() && this.cachedInvokerUrls != null) {
                invokerUrls.addAll(this.cachedInvokerUrls);
            } else {
                this.cachedInvokerUrls = new HashSet<>();
                this.cachedInvokerUrls.addAll(invokerUrls);//Cached invoker urls, convenient for comparison
            }
            if (invokerUrls.isEmpty()) {
                return;
            }
            Map<String, Invoker<T>> newUrlInvokerMap = toInvokers(invokerUrls);// Translate url list to Invoker map

            /**
             * If the calculation is wrong, it is not processed.
             *
             * 1. The protocol configured by the client is inconsistent with the protocol of the server.
             *    eg: consumer protocol = dubbo, provider only has other protocol services(rest).
             * 2. The registration center is not robust and pushes illegal specification data.
             *
             */
            if (CollectionUtils.isEmptyMap(newUrlInvokerMap)) {
                logger.error(new IllegalStateException("urls to invokers error .invokerUrls.size :" + invokerUrls.size() + ", invoker.size :0. urls :" + invokerUrls
                        .toString()));
                return;
            }

            List<Invoker<T>> newInvokers = Collections.unmodifiableList(new ArrayList<>(newUrlInvokerMap.values()));
            // pre-route and build cache, notice that route cache should build on original Invoker list.
            // toMergeMethodInvokerMap() will wrap some invokers having different groups, those wrapped invokers not should be routed.
            routerChain.setInvokers(newInvokers);
            this.invokers = multiGroup ? toMergeInvokerList(newInvokers) : newInvokers;
            this.urlInvokerMap = newUrlInvokerMap;

            try {
                destroyUnusedInvokers(oldUrlInvokerMap, newUrlInvokerMap); // Close the unused Invoker
            } catch (Exception e) {
                logger.warn("destroyUnusedInvokers error. ", e);
            }
        }
    }
```

1.  providers有变更或者其他Listener可能会调用refreshInvoker方法。

2. 如果this.forbidden = true，那么invoker.isAvaliable()=false，可能导致consumer调用失败。

3. dubbo 2.7.6版本的org.apache.dubbo.registry.nacos.NacosRegistry.getServiceNames0(...)会获取

   serviceNames.add(getLegacySubscribedServiceName(url))。导致服务出现2种情况，例如：

   ①正常：providers:IService::group

   ②Legacy：providers:IService:group

   ```
    private Set<String> getServiceNames0(URL url) {
           NacosServiceName serviceName = createServiceName(url);
           final Set<String> serviceNames;
           if (serviceName.isConcrete()) { // is the concrete service name
               serviceNames = new LinkedHashSet<>();
               serviceNames.add(serviceName.toString());
               // Add the legacy service name since 2.7.6
               serviceNames.add(getLegacySubscribedServiceName(url));
           } else {
               serviceNames = filterServiceNames(serviceName);
           }
   
           return serviceNames;
       }
   ```

   而且，org.apache.dubbo.registry.support.AbstractRegistry.notify(...)调用 listener.notify(...)和saveProperties(url)方法。前者会调用RegistryDirectory.refreshInvoker(...)。

   ```
    protected void notify(URL url, NotifyListener listener, List<URL> urls) {
         	......
           // keep every provider's category.
           Map<String, List<URL>> result = new HashMap<>();
           for (URL u : urls) {
               if (UrlUtils.isMatch(url, u)) {
                   String category = u.getParameter(CATEGORY_KEY, DEFAULT_CATEGORY);
                   List<URL> categoryList = result.computeIfAbsent(category, k -> new ArrayList<>());
                   categoryList.add(u);
               }
           }
           if (result.size() == 0) {
               return;
           }
           Map<String, List<URL>> categoryNotified = notified.computeIfAbsent(url, u -> new ConcurrentHashMap<>());
           for (Map.Entry<String, List<URL>> entry : result.entrySet()) {
               String category = entry.getKey();            
               List<URL> categoryList = entry.getValue();
               categoryNotified.put(category, categoryList);
               listener.notify(categoryList);
               // We will update our cache file after each notification.
               // When our Registry has a subscribe failure due to network jitter, we can return at least the existing cache URL.
               saveProperties(url);
           }
       }
   ```

   所以，当使用后台硬编码调用dubbo服务时，会找不到服务(java.lang.IllegalStateException: Failed to check the status of the service)。解决办法：provider/consume添加group和version属性，统一为：
   
   providers:interface:version:group。
   
   <dubbo:provider   version="5.0" group="public" ... />
   
   <dubbo:consumer  version="5.0" group="public" .../>
   
   