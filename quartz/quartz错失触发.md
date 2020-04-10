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
   >
   > 2. Trigger 被暂停（suspend）的一段时间里，有些任务可能会被 misfire；
   >
   > 3. 线程池中所有线程都被占用，导致任务无法被触发执行，造成 misfire；
   >
   > ①   【多个Job并行触发】每天晚上8点半定时会触发600个Job，Job平均执行时间不小于3分钟。如果线程池设置的是100个并发，那么超过100之后可能没有线程可用。
   >
   > 虽然可以设置misfireThreshold（超时临界时间，只有超过misfireThreshold设置的时间之后才判定为misfired）容忍策略，但是如果时间设置的是1分钟，极端情况，可能100个Job都没有在1分钟内执行结束，这个时候线程依然不够用。
   >
   > ②   【多个Job高频触发】假设配置某个Job15分钟执行一次，Job本身执行业务平均耗费时10分钟（实际业务很少有这种操作，优先级次之）。10:00Job开始执行，但是可能由于数据库、网络等原因导致该Job执行耗时20分钟，10:20才执行结束。如果延迟的Job比较多（大量线程延迟释放），那么10:15的触发可能不会按时执行。
   >
   > 4. 有状态任务在下次触发时间到达时，上次执行还没有结束；为了处理 misfired job，Quartz 中为 trigger 定义了处理策略，主要有下面两种：MISFIRE_INSTRUCTION_FIRE_ONCE_NOW：针对 misfired job 马上执行一次；MISFIRE_INSTRUCTION_DO_NOTHING：忽略 misfired job，等待下次触发；默认是MISFIRE_INSTRUCTION_SMART_POLICY，该策略在CronTrigger中=MISFIRE_INSTRUCTION_FIRE_ONCE_NOW线程默认1分钟执行一次；在一个事务中，默认一次最多recovery 20个；
   >
   > 出现misfired不代表之后就不会执行，quartz允许配置策略决定misfired job是否执行。主要包含2种Trigger: SimpleTrigger和CronTrigger。可以按照实际业务配置不同的misfired策略。什么样的<strong style='color:red'>MisfireInstruction策略</strong>合适？
   >
   > **假设前提：下午5点到7点，每隔一个小时执行一次，理论上共执行3次**
   >
   >    1. Trigger
   >
   >       - MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY
   >
   >      这个不是忽略已经错失的触发的意思，而是说忽略MisFire策略。它会在资源合适的时候，重新触发所有的MisFire任务，并且不会影响现有的调度时间。
   >     
   >      比如，SimpleTrigger每15秒执行一次，而中间有5分钟时间它都MisFire了，一共错失了20个，5分钟后，假设资源充足了，并且任务允许并发，它会被一次性触发。
   >     
   >      这个属性是所有Trigger都适用。
   >
   >    2. CronTrigger
   >
   >       1. MISFIRE_INSTRUCTION_FIRE_ONCE_NOW
   >
   >    CornTrigger默认策略，合并部分misfire，正常执行下一个周期的任务。
   >    若是5点misfire，6:15系统恢复之后，立刻执行一次（只会一次，5点和6点的合并为一次）misfire，以后正常。
   >
   >       2. MISFIRE_INSTRUCTION_DO_NOTHING
   >
   >    若是5点misfire，6:15系统恢复之后，只会执行7点的misfire。如果下次执行时间超过了end time，实际上就没有执行机会了
   >
   >    ```java
   >    /**
   >       					 * <pre>
   >       					 * <p>CronTrigger.MISFIRE_INSTRUCTION_DO_NOTHING:可能频繁的misfire,misfired之后什么都不做,等待下次Cron触发频率时刻开始按照Cron频率依次执行。</p>
   >       					 * <p>例如每1分钟执行一次，开始10:00misfired，那么下次下次Cron触发频率时刻为10:01。</p>
   >       					 * <p>之后相当于从10:01开始的触发。</p>
   >       					 * </pre>
   >       					 * 
   >       					 * <pre>
   >       					 * <p>Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY:可能一直不会misfire。如果执行的频率较快，Job执行的时间又比较长，会出现misfired。</p>
   >       					 * <p>例如每5分钟执行一次，10:00misfired,下次触发时间是10:05。</p>
   >       					 *	<code>
   >       					 *		if(当前时钟小于10:05){
   >       					 *			while(true){
   >       					 *				if(资源（线程）满足){进入资源竞争队列}else{sleep一会，等待资源释放，入等待栈}
   >       					 *			}
   >       					 *		}else{
   >       					 *			开始10:05的新一轮触发;
   >       					 *			while(true){
   >       					 *				if(资源（线程）满足){【10:00的恢复触发】以及【10:05的开始触发】进入资源竞争队列①}else{sleep一会，等待资源释放，入等待栈}
   >       					 *			}
   >       					 *		}
   >       					 *	</code>
   >       					 * <p>可以执行的触发，存储为队列；不能执行的，存储为栈。所以随着时间的推移，总是先恢复最近的misfire。注意：位置①的竞争是公平竞争，【10:00的恢复触发】以及【10:05的开始触发】都可以进入队列。</p>
   >       					 * <p>最终如果等待栈中依然存在trigger，为misfire(如果调度时间已经过了，整个调度都停了)。</p>
   >       					 * </pre>
   >       					 * 
   >       					 * <pre>
   >       					 * <p>CronTrigger.MISFIRE_INSTRUCTION_FIRE_ONCE_NOW</p>
   >       					 * <pre>
   >       					 * 和Trigger.MISFIRE_INSTRUCTION_IGNORE_MISFIRE_POLICY类似，只不过错过的trigger不进行叠加。如果10:00misfire,10:05misfire,10:15时只会恢复一次10:05的misfire,
   >       					 * 如果此次misfire恢复失败并且又出现了新的misfire，10:20时除了执行当前的trigger之外，只会恢复一次10:15的misfire(10:05的misfire不会恢复)
   >       					 * </pre>
   >       					 * 
   >       					 * </pre>
   >    					 */
   >    ```
   >
   >    
   >
   >    3. SimpleTrigger
   >       1. MISFIRE_INSTRUCTION_FIRE_NOW
   >       2. MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
   >       3. MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT
   >       4. MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
   >       5. MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
   >       6. MISFIRE_INSTRUCTION_SMART_POLICY
   >
   >    **SimpleTrigger的MisFire策略有：**
   >
   > ​	<strong style='color:red'>参考：</strong><https://blog.csdn.net/chen888999/article/details/78575492>
   >
   >       - MISFIRE_INSTRUCTION_FIRE_NOW
   >     
   >      忽略已经MisFire的任务，并且立即执行调度。这通常只适用于只执行一次的任务。
   >     
   >       - MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT
   >     
   >      将startTime设置当前时间，立即重新调度任务（包括MisFire的，执行次数不会少）
   >      固定次数执行的job:立即执行第一个发生misfire的任务，并且修改startTime为当前时间，然后按照设定的间隔时间执行下一次任务，直到所有的任务执行完成，此命令不会遗漏任务的执行次数。
   >     例如：10:15会立即执行9:00的任务，startTime修改为10:15，然后后续的任务执行时间为,11:15,12:15,13:15,14:15，也就是说任务完成时间延迟到了14:15，但是任务的执行次数还是总共的6次。
   >     ————————————————
   >     版权声明：本文为CSDN博主「自在小丁」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
   >     原文链接：https://blog.csdn.net/chen888999/article/details/78575492
   >     
   >       - MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_REMAINING_REPEAT_COUNT
   >     
   >      类似MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT，区别在于会忽略已经MisFire的任务（执行次数会少）
   >      固定次数执行的job:立即执行第一个发生misfire的任务，并且修改startTime为当前时间，然后按照设定的间隔时间执行下一个任务，直到所有剩余任务执行完成，此命令会忽略已经发生misfire的任务（第一个misfire任务除外，因为会被立即执行），继续执行剩余的正常任务。
   >     例如：10:15会立即执行9:00的任务，并且修改startTime为10:15，然后Quartz会忽略10:00发生的misfire的任务，然后后续的执行时间为：11:15,12:15,13:15，由于10:00的任务被忽略了，因此总的执行次数实际上是5次。
   >     ————————————————
   >     版权声明：本文为CSDN博主「自在小丁」的原创文章，遵循 CC 4.0 BY-SA 版权协议，转载请附上原文出处链接及本声明。
   >     原文链接：https://blog.csdn.net/chen888999/article/details/78575492
   >     
   >       - MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT
   >     
   >      不会修改startTime，在下一次调度时间点，重新开始调度任务（包括MisFire的）（与上述链接中的说法不一致，待验证？？？？）
   >     
   >       - MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
   >     
   >      类似于MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_EXISTING_COUNT，区别在于会忽略已经MisFire的任务。（与上述链接中的说法不一致，待验证？？？？）
   >     
   >       - MISFIRE_INSTRUCTION_SMART_POLICY
   >     
   >      所有的Trigger的MisFire默认值都是这个，大致意思是“把处理逻辑交给聪明的Quartz去决定”。基本策略是，
   >     
   >         1. 如果是只执行一次的调度，使用MISFIRE_INSTRUCTION_FIRE_NOW
   >         2. 如果是无限次的调度(repeatCount是无限的)，使用MISFIRE_INSTRUCTION_RESCHEDULE_NEXT_WITH_REMAINING_COUNT
   >         3. 否则，使用MISFIRE_INSTRUCTION_RESCHEDULE_NOW_WITH_EXISTING_REPEAT_COUNT