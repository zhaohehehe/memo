
## 开启慢查询日志(一般不启用，只有调优时用到)
```
show variables like '%slow_query_log%';
set global slow_query_log=1; -- 重启失效
```

## 设置慢查询阈值
```
show variables like '%long_query_time%'; -- 单位秒
set global long_query_time=3; -- 关闭当前session，重新开启新的session，看到变化
```
## 查看慢查询记录数
```
show global status like '%Slow_queries%';
```

## [mysqldumpslow工具](https://dev.mysql.com/doc/refman/8.0/en/mysqldumpslow.html)
- 得到返回记录集最多的10个SQL:<br>`mysqldumpslow -s r -t 10 /var/.../*-slow.log`
- 得到访问次数最多的10个SQL:<br>`mysqldumpslow -s c -t 10 /var/.../*-slow.log`
- 得到按照时间排序的前10条里面含有左连接的查询语句:<br>`mysqldumpslow -s t -t 10 -g "left join" /var/.../*-slow.log`
- 另外建议在使用这些命令时结合|和more使用，否则有可能出现爆屏情况:<br>`mysqldumpslow -s r -t 10 /var/.../*-slow.log| more`
