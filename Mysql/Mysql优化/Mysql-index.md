## type=range范围之后的索引失效

```
-- Using index condition; Using filesort
explain select col1 from lock_test where idx_1='001' and idx_2>'0012' ORDER BY idx_3 asc;
explain select col1 from lock_test where idx_1='001' and idx_2='0012' ORDER BY idx_3 asc;
-- 优化
drop index idx_key2 on lock_test;
create index `idx_key2` on lock_test(`idx_1`,`idx_3`);
show index from lock_test;
```
## ta LEFT JOIN tb 索引在tb上效率更高，RIGHT JOIN相反

## 最左前缀原则

## 不在索引列操作
- 函数

```
-- Using index idx_key2
explain select col1 from lock_test where idx_1='001';
-- All全表扫描
explain select col1 from lock_test where left(idx_1,3)='001';
```
- 计算
- 类型转换

## 尽量使用覆盖索引,避免select *
```
-- Extra = Using index
explain select idx_1 from lock_test where idx_1='001' and idx_2='001';
-- Extra = NULL
explain select idx_1,col1 from lock_test where idx_1='001' and idx_2='001';
-- Extra = NULL
explain select * from lock_test where idx_1='001' and idx_2='001';
```

## like '%...'会导致索引失效(使用`负覆盖索引`解决)
```
-- ALL
explain select * from lock_test where idx_1 like '%001';
-- ALL
explain select * from lock_test where idx_1 like '%001%';
-- range 使用idx_key2
explain select * from lock_test where idx_1 like '001%';

-- 使用负覆盖索引解决全表扫描的问题
explain select id,idx_1 from lock_test where idx_1 like '%001';
explain select id,idx_2 from lock_test where idx_1 like '%001%';
explain select id,idx_2 from lock_test where idx_1 like '%001%';
```
## 字符串不加单引号导致索引失效
```
-- ALL
explain select * from lock_test where idx_1 = 001;
-- ref
explain select * from lock_test where idx_1 = '001';
-- ALL
explain select * from lock_test where idx_1 = 001 and idx_2=002;
-- 使用idx_key2中的idx_1
explain select * from lock_test where idx_1 = '001' and idx_2=002;

```

## in-exists
子查询往往可以用JOIN或者其他条件表达式代替，依据实际情况选择。
![img](/imgs/mysql-in-exists.png "mysql-in-exists")

## group by排序尽量避免file_sort使用index_sort
- <p style='color:red'>filesort算法:two-pass、single-pass。</p>

  > 1. where条件筛选数据。
  2. 按行读取数据。
  3. 加载数据到sort_buffer
    - `two-pass`存储orderByKey和`行指针`到sort_buffer（第1次IO）
    - `one-pass`存储orderByKey和`select colums`到sort_buffer（第1次IO）
  4. 如果缓冲区不满,一次排序即可；如果缓冲区满了，反复每次读取sort_buffer_size范围内的数据行进行内存排序，排序结果写入`temporary${n}`临时文件中。
  5. Merge buffer。合并多个临时文件`temporary${n}`。
  6. 读取排序结果。
    - `two-pass`根据临时文件中行指针随机读取底层物理存储记录。（第2次IO）
    - `one-pass`直接读取临时文件即可。

- [sort_buffer_size](https://dev.mysql.com/doc/refman/8.0/en/server-system-variables.html#sysvar_sort_buffer_size)
- > On Linux, there are `thresholds of 256KB and 2MB` where larger values may significantly `slow down memory allocation`, so you should consider staying below one of those values
- > If you see many `Sort_merge_passes` per second in `SHOW GLOBAL STATUS` output, you can consider increasing the `sort_buffer_size` value to speed up ORDER BY or GROUP BY operations that cannot be improved with query optimization or improved indexing.
- ***注意：A filesort operation uses `temporary disk files` as necessary if the result set is too large to fit in memory.***
- filesort可能在内存中完成，也可能需要额外的`temporary disk files`。
> EXPLAIN does not distinguish whether the optimizer does or does not perform a filesort in memory.
Use of an in-memory filesort can be seen in optimizer trace output.
Look for filesort_priority_queue_optimization.
For information about the optimizer trace, see [MySQL Internals: Tracing the Optimizer](https://dev.mysql.com/doc/internals/en/optimizer-tracing.html).

- <p style='color:red'>没有使用filesort如何增加order by速度？？？</p>

  > （@deprecated 对于`MySQL 8.0.20以前`的版本）如果order by很慢，而且没有使用index，也没有使用filesort：try lowering the `max_length_for_sort_data` system variable to a value that is appropriate to trigger a filesort。如果这个值过高，会引发a combination of `high disk activity and low CPU` activity。

- <p style='color:red'>使用filesort如何增加order by速度？？？</p>

  + 可以适当增加sort_buffer_size。考虑到存储在sort buffer的数据列受到系统变量`max_sort_length`的影响，特别对于`long string column`需要增加max_sort_length的情况，相应的也需要适当的增大sort_buffer_size。
<br> ***To monitor the number of merge passes (to merge temporary files), check the `Sort_merge_passes` status variable.***
  + > Increase the `read_rnd_buffer_size` variable value so that more rows are read at a time.
  + > Change the `tmpdir` system variable to point to a dedicated file system with large amounts of free space. The variable value can list several paths that are used in round-robin fashion; you can use this feature to spread the load across several directories. Separate the paths by colon characters (`:`) on Unix and semicolon characters (`;`) on Windows. The paths should name directories in file systems located on different `physical` disks, not different partitions on the same disk.
