package com.bonc.dataplatform.workflowmonitor.dao.impl;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * SynchronousQueue本质上是零容量的队列，因此如果有空闲线程，ExecutorService只会执行新的任务。如果所有的线程都被占用，
 * 新任务会被立刻拒绝不会等待。
 * <p>
 * 当进程背景要求立刻启动或者被丢弃时，这种机制是可取的。
 * 
 * @author ZH
 *
 */
public class MySynchronousQueue {
	public static void main(String[] args) {
		BlockingQueue<Runnable> queue = new SynchronousQueue<>();
		ExecutorService executorService = new ThreadPoolExecutor(2, 2, 0L, TimeUnit.MILLISECONDS, queue);
	}
}
