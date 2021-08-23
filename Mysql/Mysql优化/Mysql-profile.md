## SHOW PROFILE Statement
```
SHOW PROFILE [type [, type] ... ]
    [FOR QUERY n]
    [LIMIT row_count [OFFSET offset]]
type: {
    ALL
  | BLOCK IO : displays counts for block input and output operations
  | CONTEXT SWITCHES : displays counts for voluntary and involuntary context switches
  | CPU : displays user and system CPU usage times
  | IPC : displays counts for messages sent and received
  | MEMORY : is not currently implemented
  | PAGE FAULTS : displays counts for major and minor page faults
  | SOURCE : displays the names of functions from the source code, together with the name and line number of the file in which the function occurs
  | SWAPS : displays swap counts
}
```
1. display profiling information that indicates `resource usage` for statements executed during the course of the `current session`.
2. By default, `SHOW PROFILE` displays `Status` and` Duration` columns.`Status`类似`SHOW PROCESSLIST`.
```
  SHOW PROFILES;
  SHOW PROFILE;
  SHOW PROFILE FOR QUERY 1;
  SHOW PROFILE CPU FOR QUERY 1;
```
3. Profiling information is also available from the `INFORMATION_SCHEMA PROFILING` table.
```
  SELECT STATE, FORMAT(DURATION, 6) AS DURATION FROM INFORMATION_SCHEMA.PROFILING WHERE QUERY_ID = 1 ORDER BY SEQ;
```
4. 以后的MySQL版本会逐渐deprecated,替换为[Profiling Using Performance Schema](https://dev.mysql.com/doc/refman/8.0/en/performance-schema-query-profiling.html)。

***

##  Profiling Using Performance Schema

***Demo 01: limit collection of historical events to a specific user.***
> `setup_actors` is configured to allow monitoring and historical event collection for all foreground threads.

```
  SELECT * FROM performance_schema.setup_actors;
  UPDATE performance_schema.setup_actors SET ENABLED = 'NO', HISTORY = 'NO' WHERE HOST = '%' AND USER = '%';
  INSERT INTO performance_schema.setup_actors(HOST,USER,ROLE,ENABLED,HISTORY) VALUES('localhost','test_user','%','YES','YES');
```

***Demo 02***
> Ensure that statement and stage instrumentation is enabled by updating the `setup_instruments` table. Some instruments may `already be enabled by default`.

```
  SELECT * FROM performance_schema.setup_instruments WHERE NAME LIKE '%statement/%';
  UPDATE performance_schema.setup_instruments SET ENABLED = 'YES', TIMED = 'YES' WHERE NAME LIKE '%statement/%';
  SELECT * FROM performance_schema.setup_instruments WHERE NAME LIKE '%stage/%';
  UPDATE performance_schema.setup_instruments SET ENABLED = 'YES', TIMED = 'YES' WHERE NAME LIKE '%stage/%';

```
***Demo 03***
> Ensure that `events_statements_*` and `events_stages_*` consumers are enabled. Some consumers may already be enabled by default.

```
  SELECT * FROM performance_schema.setup_consumers WHERE NAME LIKE '%events_statements_%';
  UPDATE performance_schema.setup_consumers SET ENABLED = 'YES' WHERE NAME LIKE '%events_statements_%';
  SELECT * FROM performance_schema.setup_consumers WHERE NAME LIKE '%events_stages_%';
  UPDATE performance_schema.setup_consumers SET ENABLED = 'YES' WHERE NAME LIKE '%events_stages_%';
```
***Demo 04:profile监控。***
> 替代`SHOW PROFILES`。

```
  select * from employees;		 

  SELECT EVENT_ID, TRUNCATE(TIMER_WAIT/1000000000000,6) as Duration, SQL_TEXT
         FROM performance_schema.events_statements_history_long WHERE SQL_TEXT like '%employees%';

  SELECT event_name AS Stage, TRUNCATE(TIMER_WAIT/1000000000000,6) AS Duration
         FROM performance_schema.events_stages_history_long WHERE NESTING_EVENT_ID=287;
```

***
***
***
