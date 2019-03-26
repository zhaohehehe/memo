package zhaohe.study.test;

public class Compromise1 {
	public static void main(String[] args) throws Exception {
		ClassLoader myLoader = new ClassLoader() {
			/**
			 * findClass是JDK1.2之后为了向前兼容做出的妥协。
			 * <p>
			 * 用户也可以重写loadClass()方法，但是不推荐
			 */
			@Override
			protected Class<?> findClass(String name) throws ClassNotFoundException {
				System.out.println("双亲委派模型的破坏");
				System.out.println("=====这里添加自己的代码逻辑==========");
				return super.findClass(name);
			}

			@Override
			public Class<?> loadClass(String name) throws ClassNotFoundException {
				System.out.println("=====不推荐重写loadClass()，如果为了兼容请重写findClass()==========");
				return super.loadClass(name);
			}
		};
		Object obj = myLoader.loadClass("zhaohe.study.test.ClassLoaderTest").newInstance();
		System.out.println(obj.getClass().getClassLoader());
	}
}
