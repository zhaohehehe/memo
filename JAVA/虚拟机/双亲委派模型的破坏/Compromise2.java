package zhaohe.study.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * <blockquote>
 * <p>
 * 引用自CSDN：https://blog.csdn.net/yangcheng33/article/details/52631940
 * <p>
 * SPI机制简介 SPI的全名为Service Provider Interface
 * <p>
 * 主要是应用于厂商自定义组件或插件中。在java.util.ServiceLoader的文档里有比较详细的介绍。
 * <p>
 * javaSPI机制的思想：我们系统里抽象的各个模块，往往有很多不同的实现方案，比如日志模块、xml解析模块、jdbc模块、JNDI、JCE、JXAB、
 * JBI等方案。面向的对象的设计里，
 * 我们一般推荐模块之间基于接口编程，模块之间不对实现类进行硬编码。一旦代码里涉及具体的实现类，就违反了可拔插的原则，如果需要替换一种实现，就需要修改代码。
 * 为了实现在模块装配的时候能不在程序里动态指明，这就需要一种服务发现机制。
 * <p>
 * JavaSPI就是提供这样的一个机制：为某个接口寻找服务实现的机制。有点类似IOC的思想，就是将装配的控制权移到程序之外，在模块化设计中这个机制尤其重要。
 * <p>
 * JavaSPI的具体约定为：当服务的提供者提供了服务接口的一种实现之后，在jar包的META-INF/services/
 * 目录里同时创建一个以服务接口命名的文件。
 * 该文件里就是实现该服务接口的具体实现类。而当外部程序装配这个模块的时候，就能通过该jar包META-INF/services/
 * 里的配置文件找到具体的实现类名，并装载实例化，完成模块的注入。基于这样一个约定就能很好的找到服务接口的实现类，而不需要再代码里制定。
 * <p>
 * jdk提供服务实现查找的一个工具类：java.util.ServiceLoader。 </blockquote>
 * 
 * @author ZH
 *
 */
public class Compromise2 {
	public static void jdbcSPITest() throws SQLException, ClassNotFoundException {
		/**
		 * 
		 * Class.forName(className)方法等价于Class.forName(className, true,
		 * currentLoader)
		 * 这个currentLoader指的是当前线程的ClassLoader，当前线程即Compromise2.main
		 * <p>
		 * 从Java1.6开始自带的jdbc4.0版本已支持SPI【Service Privoder
		 * Interface】服务加载机制，只要mysql的jar包在类路径中，就可以注册mysql驱动。
		 * 所以Class.forName部分可以省略。这里省略不是不加载，而是换成在 DriverManager.getConnection中加载。
		 * 
		 * <pre>
		 * Class<?> targetClass = Class.forName("com.mysql.jdbc.Driver");
		 * System.out.println(targetClass.getClassLoader());// AppClassLoader
		 * ClassLoader currentLoader = Compromise2.class.getClassLoader();
		 * System.out.println(currentLoader);// AppClassLoader
		 * Class.forName("com.mysql.jdbc.Driver", true, currentLoader);
		 * </pre>
		 */
		String url = "jdbc:mysql://172.16.11.13:3306/dbname";
		/**
		 * DriverManager的static代码块初始化如下：
		 * 
		 * <pre>
		 * static {
		 * 	loadInitialDrivers();
		 * 	println("JDBC DriverManager initialized");
		 * }
		 * </pre>
		 * 
		 * =====================================================================
		 * <p>
		 * DriverManager加载Driver的顺序如下(loadInitialDrivers方法)：
		 * <ol>
		 * <li>System.getProperty("jdbc.drivers")</li>
		 * <li>SPI加载驱动类,如果driver被包装成SP，那么通过暴露的java.sql.Driver.class服务加载相应的SPI。
		 * 
		 * <pre>
		 * // ServiceLoader.load() replaces the sun.misc.Providers()
		 * ServiceLoader&lt;Driver> loadedDrivers = java.util.ServiceLoader.load(Driver.class);
		 * Iterator&lt;Driver> driversIterator = loadedDrivers.iterator();
		 * try {
		 * 	while (driversIterator.hasNext()) {
		 * 		driversIterator.next();// 调用Class.forName
		 * 	}
		 * } catch (Throwable t) {
		 * 	// Do nothing
		 * }
		 * </pre>
		 * 
		 * <pre>
		 * public static &lt;S> java.util.ServiceLoader&lt;S> load(Class&lt;S> service) {
		 * 	// ContextClassLoader默认AppClassLoader,可以获取Thread.currentThread()自行设定
		 * 	ClassLoader cl = Thread.currentThread().getContextClassLoader();
		 * 	return ServiceLoader.load(service, cl);
		 * }
		 * </pre>
		 * 
		 * </li>
		 * </ol>
		 * <p>
		 * 由于BootrapLoader不能去加载由各个不同厂商提供的JAVA驱动程序，所以首先由BootrapLoader加载java.util.
		 * ServiceLoader.class，再由 java.util.ServiceLoader.load方法加载具体驱动类。
		 * 这里由于我们之前说的BootrapLoader不能被JAVA程序访问，所以load方法加载具体驱动类的ClassLoader
		 * 一定不是BootrapLoader，一个妥协点是可以使用ContextClassLoader加载。如下getConnection代码实现：
		 * 
		 * <pre>
		 * private static Connection getConnection(String url, java.util.Properties info, Class<?> caller)
		 * 		throws SQLException {
		 * 	ClassLoader callerCL = caller != null ? caller.getClassLoader() : null;
		 * 	synchronized (DriverManager.class) {
		 * 		if (callerCL == null) {
		 * 			callerCL = Thread.currentThread().getContextClassLoader();
		 * 		}
		 * 	}
		 * }
		 * </pre>
		 */
		Connection conn = DriverManager.getConnection(url, "**", "**");
		System.out.println(conn.isClosed());
	}

	public static void main(String[] args) throws Exception {
		Compromise2.spiTest();
	}

	private static void spiTest() throws ClassNotFoundException {
		ClassLoader myLoader = new ClassLoader() {
			// 只是为了测试
			@Override
			protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
				System.out.println("======my ClassLoader==========");
				Class<?> clazz = super.loadClass(name, resolve);
				return clazz;
			}
		};
		/**
		 * 双亲委派模型
		 */
		System.out.println("双亲委派模型：");
		Thread.currentThread().getContextClassLoader().loadClass("zhaohe.study.test.Compromise2");
		System.out.println("==========================================================================");
		/**
		 * 双亲委派模型的破坏
		 */
		System.out.println("双亲委派模型的破坏：");
		Thread.currentThread().setContextClassLoader(myLoader);
		Thread.currentThread().getContextClassLoader().loadClass("zhaohe.study.test.Compromise2");

	}
}
