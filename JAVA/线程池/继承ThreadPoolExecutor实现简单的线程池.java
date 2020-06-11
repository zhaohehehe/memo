import java.lang.management.ManagementFactory;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.Assert;

import net.sf.json.JSONObject;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;

/**
 * 线程池中的线程是可以重用的，不用频繁的创建和销毁，提高了系统的性能。
 * 线程池中的队列可以管理大量的任务，任务的执行，调度，排队，丢弃等事宜都由线程池来管理，做到任务可控。
 * 线程池对线程进行一些维护和管理，比如线程定时执行，线程生命周期管理，多少个线程并发，线程执行的监控等
 * 
 * @author ZH
 * @param <T>
 *
 */
public abstract class MyAbstractThreadPoolExecutor extends ThreadPoolExecutor {

	private static final Logger log = LoggerFactory.getLogger(MyAbstractThreadPoolExecutor.class);

	/**
	 * 线程池类型
	 */
	private final String type;

	/**
	 * @param corePoolSize    线程池核心池大小，包含没有使用的线程。如果运行的线程数小于corePoolSize,会添加一个新线程到线程池(
	 *                        即使存在空闲线程)；
	 * @param maximumPoolSize 线程池允许容纳的最大线程数量（只有队列装不下的时候，才会使用到maximumPoolSize进行扩容）
	 * @param keepAliveTime   当线程的数量大于核心线程数时，空闲线程如果等待keepAliveTime时间后，仍然没有接到新的任务，
	 *                        就会执行reject策略。
	 * @param unit
	 * @param workQueue       ①如果到达一个新线程，如果当前运行的线程数大等于corePoolSize但是小于maximumPoolSize,会添加一个新线程到队列(->步骤②);
	 *                        ②如果队列未满,添加成功；如果队列已满,则创建新线程到线程池(线程池数量不能超过maximumPoolSize)(->步骤③)；
	 *                        ③如果线程池数量小于maximumPoolSize，创建新线程成功；如果大等于maximumPoolSize(启动reject处理策略
	 *                        )。
	 * @param threadFactory
	 * @param handler
	 */
	public MyAbstractThreadPoolExecutor(String type, int corePoolSize, int maximumPoolSize, long keepAliveTime,
			TimeUnit unit, BlockingQueue<Runnable> workQueue) {
		super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
		this.type = type;
		// 如果允许核心线程超时,keepAliveTime可以作用于核心线程
		super.allowsCoreThreadTimeOut();
		// 设置拒绝策略为：不进入线程池，由调用线程来执行已经被rejected的任务(将执行任务的责任推回给caller，此时执行将变成同步执行，不再是异步)
		// 仅供测试使用，实际部署为了防止OOM,请注释掉
		// super.setRejectedExecutionHandler(new
		// ThreadPoolExecutor.CallerRunsPolicy());
	}

	/**
	 * 提交带有线程描述的 Callable
	 * 
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 * @return
	 */
	public <T> Future<T> submit(final Callable<T> task, final JSONObject clientTaskDescription) {
		JSONObject newClientTaskDescription = JSONObject.fromObject(clientTaskDescription,
				MyAbstractThreadPoolExecutor.getOneJsonConfigInstance());
		return super.submit(wrap(task, newClientTaskDescription));
	}

	/**
	 * 提交带有线程描述的 Runnable
	 * 
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 * @return
	 */
	public Future<?> submit(final Runnable task, final JSONObject clientTaskDescription) {
		JSONObject newClientTaskDescription = JSONObject.fromObject(clientTaskDescription,
				MyAbstractThreadPoolExecutor.getOneJsonConfigInstance());
		return super.submit(wrap(task, newClientTaskDescription));
	}

	/**
	 * 提交带有线程描述的 Runnable
	 * 
	 * @param task                  提交的任务
	 * @param result
	 * @param clientTaskDescription 对提交任务的描述信息
	 * @return
	 */
	public <T> Future<T> submit(final Runnable task, final T result, final JSONObject clientTaskDescription) {
		JSONObject newClientTaskDescription = JSONObject.fromObject(clientTaskDescription,
				MyAbstractThreadPoolExecutor.getOneJsonConfigInstance());
		return super.submit(wrap(task, newClientTaskDescription), result);
	}

	/**
	 * 封装Callable任务
	 * 
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 * @return
	 */
	private <T> Callable<T> wrap(final Callable<T> task, final JSONObject clientTaskDescription) {
		final long startTime = System.nanoTime();
		return new Callable<T>() {
			@Override
			public T call() throws Exception {
				onExecute(Thread.currentThread(), task, clientTaskDescription);
				long startcputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long startruntime = System.nanoTime();
				final long queueDuration = startruntime - startTime;
				log.debug("任务{}从提交到实际执行耗时{}NS", task, queueDuration);
				try {
					return task.call();
				} catch (Exception e) {
					log.error("任务[{}]执行过程出现异常[{}]", clientTaskDescription, e.getMessage(), clientProxyTrace());
					throw e;
				} finally {
					long cpuDuration = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startcputime;
					log.debug("任务{}CPU耗时{}NS,阻塞耗时{}NS", task, cpuDuration,
							(System.nanoTime() - startruntime) - cpuDuration + queueDuration);
					onFinish(Thread.currentThread(), task, clientTaskDescription);
				}

			}

		};
	}

	/**
	 * 封装Runnable任务
	 * 
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 * @return
	 */
	private Runnable wrap(final Runnable task, final JSONObject clientTaskDescription) {
		final long startTime = System.nanoTime();
		return new Runnable() {
			@Override
			public void run() {
				onExecute(Thread.currentThread(), task, clientTaskDescription);
				long startcputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long startruntime = System.nanoTime();
				final long queueDuration = startruntime - startTime;
				log.debug("任务{}从提交到实际执行耗时{}NS", task, queueDuration);
				task.run();
				long cpuDuration = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startcputime;
				log.debug("任务{}CPU耗时{}NS,阻塞耗时{}NS", task, cpuDuration,
						(System.nanoTime() - startruntime) - cpuDuration + queueDuration);
				onFinish(Thread.currentThread(), task, clientTaskDescription);
			}

		};
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		final long startTime = System.nanoTime();
		return super.submit(new Callable<T>() {
			@Override
			public T call() throws Exception {
				onExecute(Thread.currentThread(), task, null);
				long startcputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long startruntime = System.nanoTime();
				final long queueDuration = startruntime - startTime;
				log.debug("任务{}从提交到实际执行耗时{}NS", task, queueDuration);
				T result = task.call();
				long cpuDuration = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startcputime;
				log.debug("任务{}CPU耗时{}NS,阻塞耗时{}NS", task, cpuDuration,
						(System.nanoTime() - startruntime) - cpuDuration + queueDuration);
				onFinish(Thread.currentThread(), task, null);
				return result;
			}

		});
	}

	@Override
	public <T> Future<T> submit(final Runnable task, final T result) {
		final long startTime = System.nanoTime();
		return super.submit(new Runnable() {
			@Override
			public void run() {
				onExecute(Thread.currentThread(), task, null);
				long startcputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long startruntime = System.nanoTime();
				final long queueDuration = startruntime - startTime;
				log.debug("任务{}从提交到实际执行耗时{}NS", task, queueDuration);
				task.run();
				long cpuDuration = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startcputime;
				log.debug("任务{}CPU耗时{}NS,阻塞耗时{}NS", task, cpuDuration,
						(System.nanoTime() - startruntime) - cpuDuration + queueDuration);
				onFinish(Thread.currentThread(), task, null);
			}
		}, result);
	}

	@Override
	public Future<?> submit(final Runnable task) {
		final long startTime = System.nanoTime();
		return super.submit(new Runnable() {

			@Override
			public void run() {
				onExecute(Thread.currentThread(), task, null);
				long startcputime = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime();
				long startruntime = System.nanoTime();
				final long queueDuration = startruntime - startTime;
				log.debug("任务{}从提交到实际执行耗时{}NS", task, queueDuration);
				task.run();
				long cpuDuration = ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime() - startcputime;
				log.debug("任务{}CPU耗时{}NS,阻塞耗时{}NS", task, cpuDuration,
						(System.nanoTime() - startruntime) - cpuDuration + queueDuration);
				onFinish(Thread.currentThread(), task, null);

			}
		});
	}

	/**
	 * 记录任务信息(在beforeExecute方法之后、run方法开始时执行)
	 * 
	 * @param t                     执行任务的线程
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 */
	protected abstract <T> void onExecute(Thread t, T task, JSONObject clientTaskDescription);

	@Override
	protected synchronized void beforeExecute(Thread t, Runnable r) {
		Assert.isTrue(r instanceof FutureTask, "beforeExecute方法的Runnable参数类型其实是java.util.concurrent.FutureTask");
		log.debug("执行{}线程池beforeExecute方法......", this.type);
		super.beforeExecute(t, r);
	}

	/**
	 * 移除任务信息(在run方法结束、afterExecute方法之前执行)
	 * 
	 * @param t                     执行任务的线程
	 * @param task                  提交的任务
	 * @param clientTaskDescription 对提交任务的描述信息
	 */
	protected abstract <T> void onFinish(Thread t, T task, JSONObject clientTaskDescription);

	/**
	 * 重写afterExecute方法，捕获线程执行异常信息
	 */
	@Override
	protected synchronized void afterExecute(Runnable r, Throwable t) {
		Assert.isTrue(r instanceof FutureTask, "afterExecute方法的Runnable参数类型其实是java.util.concurrent.FutureTask");
		log.debug("执行{}线程池afterExecute方法......", this.type);
		super.afterExecute(r, t);
		clientProxyTrace(r, t);
	}

	protected abstract void onTerminated();

	/**
	 * 重写terminated方法,该方法是线程池自动调用的
	 */
	@Override
	protected void terminated() {
		onTerminated();
		super.terminated();
	}

	/**
	 * 这里不暴露关闭方法
	 */
	@SuppressWarnings("unused")
	private void complete() {
		this.shutdown();
		try {
			if (!this.awaitTermination(36000, TimeUnit.SECONDS)) {
				this.shutdownNow();
			}
		} catch (InterruptedException e) {
			this.shutdownNow();
		}
	}

	/**
	 * 捕获线程执行异常信息
	 * 
	 * @param r
	 * @param t
	 */
	private void clientProxyTrace(Runnable r, Throwable t) {
		if (t == null && r instanceof Future<?>) {
			try {
				((Future<?>) r).get();
			} catch (CancellationException ce) {
				t = ce;
			} catch (ExecutionException ee) {
				t = ee.getCause();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt(); // ignore/reset
			} catch (Exception e) {
				t = e;
			}
		}
		if (t != null) {
			log.error("任务执行过程出现异常[{}]", t, clientProxyTrace());
		}
	}

	private MyThreadPoolExecutorException clientProxyTrace() {
		return new MyThreadPoolExecutorException("客户端代理任务stack trace");
	}

	public String getType() {
		return type;
	}

	/**
	 * json序列化空值过滤配置
	 * 
	 * @return
	 */
	public static JsonConfig getOneJsonConfigInstance() {
		JsonConfig jsonConfig = new JsonConfig();
		jsonConfig.setJsonPropertyFilter(new PropertyFilter() {
			@Override
			public boolean apply(Object arg0, String arg1, Object arg2) {
				return "".equals(arg2) || arg2 instanceof net.sf.json.JSONNull;
			}
		});
		return jsonConfig;
	}

}

class MyThreadPoolExecutorException extends Exception {

	private static final long serialVersionUID = 1L;

	public MyThreadPoolExecutorException(String message, Throwable cause) {
		super(message, cause);
	}

	public MyThreadPoolExecutorException(String message) {
		super(message);
	}

}
