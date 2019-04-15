package **;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 监控从任务的提交到实际执行耗时：当我们向线程池提交任务的那一刻，就立马开始测量时间，而任务一开始被执行就停止测量
 * <p>
 * 注意：代码不是完整的实现
 * 
 * @author ZH
 *
 */
public class WaitTimeMonitoringExecutorService implements ExecutorService {
	private final ExecutorService target;
	private static final Logger log = LoggerFactory.getLogger(WaitTimeMonitoringExecutorService.class);

	public WaitTimeMonitoringExecutorService(ExecutorService target) {
		this.target = target;
	}

	@Override
	public void execute(Runnable command) {
		// TODO Auto-generated method stub

	}

	@Override
	public void shutdown() {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Runnable> shutdownNow() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isShutdown() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean isTerminated() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean awaitTermination(long timeout, TimeUnit unit) throws InterruptedException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public <T> Future<T> submit(final Callable<T> task) {
		final long startTime = System.currentTimeMillis();
		return target.submit(new Callable<T>() {

			@Override
			public T call() throws Exception {
				final long queueDuration = System.currentTimeMillis() - startTime;
				log.info("Task {} spent {}ms in queue", task, queueDuration);
				return task.call();
			}

		});

	}

	@Override
	public <T> Future<T> submit(final Runnable task, final T result) {
		return submit(new Callable<T>() {

			@Override
			public T call() throws Exception {
				task.run();
				return result;
			}
		});
	}

	@Override
	public Future<?> submit(final Runnable task) {
		return submit(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				task.run();
				return null;
			}
		});
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks) throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks) throws InterruptedException, ExecutionException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T invokeAny(Collection<? extends Callable<T>> tasks, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		// TODO Auto-generated method stub
		return null;
	}

}
