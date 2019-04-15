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
 * 保留客户端异常信息
 * 
 * @author ZH
 *
 */
public class ExecutorServiceWithClientTrace implements ExecutorService {

	private final ExecutorService target;
	private static final Logger log = LoggerFactory.getLogger(ExecutorServiceWithClientTrace.class);

	public ExecutorServiceWithClientTrace(ExecutorService target) {
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
	public <T> Future<T> submit(Callable<T> task) {
		return target.submit(wrap(task, clientTrace(), Thread.currentThread().getName()));
	}

	private Exception clientTrace() {
		return new Exception("Client stack trace");
	}

	private <T> Callable<T> wrap(final Callable<T> task, final Exception clientStack, final String clientThreadName) {
		return new Callable<T>() {

			@Override
			public T call() throws Exception {
				try {
					return task.call();
				} catch (Exception e) {
					log.error("Exception {} in task submitted from thrad {} here:", e, clientThreadName, clientStack);
					throw e;

				}
			}
		};
	}

	@Override
	public <T> Future<T> submit(Runnable task, T result) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Future<?> submit(Runnable task) {
		// TODO Auto-generated method stub
		return null;
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
