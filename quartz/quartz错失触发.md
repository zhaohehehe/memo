[Quartz官方文档]: https://www.w3cschool.cn/quartz_doc/

1. quartz主要组件：
   1. ThreadPool
   2. JobStore
   3. DataSource(如果需要的话)
   4. Scheduler

2. quartz线程池：

   > org.quartz.impl.StdSchedulerFactory implements SchedulerFactory
   >
   > ①SchedulerFactory首先执行initialize方法加载配置属性，然后instantiate Scheduler对象
   >
   > ②PROP_THREAD_POOL_CLASS（org.quartz.threadPool.class）属性指定线程池，一般使用自带的org.quartz.simpl.SimpleThreadPool就可以。
   >
   > ③PROP_THREAD_POOL_PREFIX（org.quartz.threadPool）加载其他的线程池配置，并通过反射(源码pointA)执行SimpleThreadPool的方法设置线程池参数。
   >
   > ```
   > 		// Get ThreadPool Properties
   >         // ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
   > 
   >         String tpClass = cfg.getStringProperty(PROP_THREAD_POOL_CLASS, null);
   > 
   >         if (tpClass == null) {
   >             initException = new SchedulerException(
   >                     "ThreadPool class not specified. ",
   >                     SchedulerException.ERR_BAD_CONFIGURATION);
   >             throw initException;
   >         }
   > 
   >         try {
   >             tp = (ThreadPool) loadHelper.loadClass(tpClass).newInstance();
   >         } catch (Exception e) {
   >             initException = new SchedulerException("ThreadPool class '"
   >                     + tpClass + "' could not be instantiated.", e);
   >             initException
   >                     .setErrorCode(SchedulerException.ERR_BAD_CONFIGURATION);
   >             throw initException;
   >         }
   >         tProps = cfg.getPropertyGroup(PROP_THREAD_POOL_PREFIX, true);
   >         try {
   >             setBeanProps(tp, tProps);//pointA
   >         } catch (Exception e) {
   >             initException = new SchedulerException("ThreadPool class '"
   >                     + tpClass + "' props could not be configured.", e);
   >             initException
   >                     .setErrorCode(SchedulerException.ERR_BAD_CONFIGURATION);
   >             throw initException;
   >         }
   > ```
   >
   > 
   >
   > ④org.quartz.simpl.SimpleThreadPool implements org.quartz.spi.ThreadPool <strong style='color:red'>固定的线程池数量，没有等待队列</strong>，该数量通过org.quartz.threadPool.threadCount属性配置，默认10。
   >
   > ```
   > /**
   >  * <p>
   >  * This is class is a simple implementation of a thread pool, based on the
   >  * <code>{@link org.quartz.spi.ThreadPool}</code> interface.
   >  * </p>
   >  * 
   >  * <p>
   >  * <CODE>Runnable</CODE> objects are sent to the pool with the <code>{@link #runInThread(Runnable)}</code>
   >  * method, which blocks until a <code>Thread</code> becomes available.
   >  * </p>
   >  * 
   >  * <p>
   >  * The pool has a fixed number of <code>Thread</code>s, and does not grow or
   >  * shrink based on demand.
   >  * </p>
   >  * 
   >  * @author James House
   >  * @author Juergen Donnerstag
   >  */
   > ```
   >
   > 

   ```
   		
   ```

   > org.quartz.impl.DirectSchedulerFactory implements SchedulerFactory

3. JobStore

   > ①org.quartz.jobStore.class指定加载class，如果没有配置，默认org.quartz.simpl.RAMJobStore
   >
   > ​		RAMJobStore用于存储内存中的调度信息（jobs，Triggers和日历）。RAMJobStore快速轻便，但是当进程终止时，所有调度信息都会丢失。
   >
   > ②加载org.quartz.jobStore前缀配置，同样通过反射设置setBeanProps(classInstance, jobStoreProps);
   >
   > ③org.quartz.jobStore.misfireThreshold属性指定：[misfire 错失、补偿执行](https://www.cnblogs.com/skyLogin/p/6927629.html)
   >
   > 调度(scheduleJob)或恢复调度(resumeTrigger,resumeJob)后不同的misfire对应的处理规则
   >
   > misfire产生的条件是：到了该触发执行时上一个执行还未完成，且线程池中没有空闲线程可以使用（或有空闲线程可以使用但job设置为@DisallowConcurrentExecution）且过期时间已经超过misfireThreshold就认为是misfire了，错失触发了
   >
   > 比如：13:07:24开始执行，重复执行5次，开始执行时，quartz已经计算好每次调度的时间刻，分别如下：
   >
   > 03:33:36，03:33:39，03:33:42，03:33:45，03:33:48，03:33:51
   >
   > 如果第一次执行时间为11s，到03:33:47结束，03:33:47减去03:33:39的时间间隔是8s，如果misfireThreshold设置的时间小于等于8s间隔，则认为是misfire了，如果大于8s间隔，则认为没有misfire。
   >
   > 下面这些原因可能造成 misfired job:
   >
   > 1. 系统因为某些原因被重启。在系统关闭到重新启动之间的一段时间里，可能有些任务会被 misfire；
   > 2. Trigger 被暂停（suspend）的一段时间里，有些任务可能会被 misfire；
   > 3. 线程池中所有线程都被占用，导致任务无法被触发执行，造成 misfire；
   > 4. 有状态任务在下次触发时间到达时，上次执行还没有结束；为了处理 misfired job，Quartz 中为 trigger 定义了处理策略，主要有下面两种：MISFIRE_INSTRUCTION_FIRE_ONCE_NOW：针对 misfired job 马上执行一次；MISFIRE_INSTRUCTION_DO_NOTHING：忽略 misfired job，等待下次触发；默认是MISFIRE_INSTRUCTION_SMART_POLICY，该策略在CronTrigger中=MISFIRE_INSTRUCTION_FIRE_ONCE_NOW线程默认1分钟执行一次；在一个事务中，默认一次最多recovery 20个；
   > 5. org.quartz.SimpleTrigger查看各种<strong style='color:red'>MisfireInstruction策略</strong>