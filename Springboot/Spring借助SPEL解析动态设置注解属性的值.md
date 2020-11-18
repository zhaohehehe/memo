## 源码

```
public class EmbeddedValueResolver implements StringValueResolver {

	private final BeanExpressionContext exprContext;

	@Nullable
	private final BeanExpressionResolver exprResolver;


	public EmbeddedValueResolver(ConfigurableBeanFactory beanFactory) {
		this.exprContext = new BeanExpressionContext(beanFactory, null);
		this.exprResolver = beanFactory.getBeanExpressionResolver();
	}


	@Override
	@Nullable
	public String resolveStringValue(String strVal) {
        //解析SPEL表达式
		String value = this.exprContext.getBeanFactory().resolveEmbeddedValue(strVal);
		if (this.exprResolver != null && value != null) {
            //调用StandardBeanExpressionResolver.evaluate(...)解析value
			Object evaluated = this.exprResolver.evaluate(value, this.exprContext);
			value = (evaluated != null ? evaluated.toString() : null);
		}
		return value;
	}

}
```

```
org.springframework.context.expression.StandardBeanExpressionResolver
@Override
	@Nullable
	public Object evaluate(@Nullable String value, BeanExpressionContext evalContext) throws BeansException {
		if (!StringUtils.hasLength(value)) {
			return value;
		}
		try {
			Expression expr = this.expressionCache.get(value);
			if (expr == null) {
              //this.expressionParser是
              //org.springframework.expression.spel.standard.SpelExpressionParser
				expr = this.expressionParser.parseExpression(value, this.beanExpressionParserContext);
				this.expressionCache.put(value, expr);
			}
			StandardEvaluationContext sec = this.evaluationCache.get(evalContext);
			if (sec == null) {
				sec = new StandardEvaluationContext(evalContext);
				sec.addPropertyAccessor(new BeanExpressionContextAccessor());
				sec.addPropertyAccessor(new BeanFactoryAccessor());
				sec.addPropertyAccessor(new MapAccessor());
				sec.addPropertyAccessor(new EnvironmentAccessor());
				sec.setBeanResolver(new BeanFactoryResolver(evalContext.getBeanFactory()));
				sec.setTypeLocator(new StandardTypeLocator(evalContext.getBeanFactory().getBeanClassLoader()));
				ConversionService conversionService = evalContext.getBeanFactory().getConversionService();
				if (conversionService != null) {
					sec.setTypeConverter(new StandardTypeConverter(conversionService));
				}
				customizeEvaluationContext(sec);
				this.evaluationCache.put(evalContext, sec);
			}
			return expr.getValue(sec);
		}
		catch (Throwable ex) {
			throw new BeanExpressionException("Expression parsing failed", ex);
		}
	}

```
## DEMO
1. 创建activeMQ消息监听时动态指定@JmsListener的destination（tpopic）

```
@Component
	@ConditionalOnProperty(name = "message.type", havingValue = "activemq")
	class ActiveMQReceiver {

		@Value("${activemq.topic.prefix:}")
		private String topicPrefix;

		@Value("${activemq.topic.name:}")
		private String topicName;

		@Bean(value = "testDestination")
		public String destination() {
			return topicPrefix + topicName;
		}

		@JmsListener(destination = "#{@testDestination}", containerFactory = "同样可替换")
		public void onMessage(Message message) {
			try {
				String msg = ((TextMessage) message).getText();
        .....
			} catch (Exception e) {

			}
		}
	}
```
2. 不使用@JmsListener注解，动态指定destination（topic）

```
public void demo(String destinationName, String url, String username, String password) {
		SimpleJmsListenerEndpoint endpoint = new SimpleJmsListenerEndpoint();
		endpoint.setId(destinationName);
		endpoint.setDestination(destinationName);
		endpoint.setMessageListener(message -> {
			String text = ((TextMessage) message).getText();
		});

		try {
			DefaultJmsListenerContainerFactory beanFactory = createJmsListenerContainerFactory(username, password, url);
			JmsListenerAnnotationBeanPostProcessor bean = ApplicationContext
					.getBean(JmsListenerAnnotationBeanPostProcessor.class);
			Field endpointRegistry = JmsListenerAnnotationBeanPostProcessor.class.getDeclaredField("endpointRegistry");
			endpointRegistry.setAccessible(true);
			JmsListenerEndpointRegistry registry = (JmsListenerEndpointRegistry) endpointRegistry.get(bean);
			registry.registerListenerContainer(endpoint, beanFactory, true);
		} catch (NoSuchFieldException | IllegalAccessException | IllegalStateException e) {
		}

	}
```



## 参考
https://blog.csdn.net/ouyangguangfly/article/details/106646378
