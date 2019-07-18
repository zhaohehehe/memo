#### 1. MySQL indexes

1. `PRIMARY KEY` （ B-trees）

2.  `UNIQUE`（ B-trees）

3.  `INDEX`（ B-trees）

4. `FULLTEXT`（ B-trees）

5.  Indexes on spatial data types use R-trees

6. `MEMORY` tables also support [hash indexes](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_hash_index)

7. `FULLTEXT` （inverted lists 倒排索引）

   #### B-Tree Index Characteristics

   1.  use the [`=`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_equal), [`>`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_greater-than), [`>=`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_greater-than-or-equal), [`<`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_less-than), [`<=`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_less-than-or-equal), or [`BETWEEN`](https://dev.mysql.com/doc/refman/8.0/en/comparison-operators.html#operator_between) operators。

   2. [`LIKE`](https://dev.mysql.com/doc/refman/8.0/en/string-comparison-functions.html#operator_like) comparisons if the argument to [`LIKE`](https://dev.mysql.com/doc/refman/8.0/en/string-comparison-functions.html#operator_like) is a constant string that does not start with a wildcard character。

      例如： 

      ```sql
      SELECT * FROM tbl_name WHERE key_col LIKE 'Patrick%';（employ indexes使用index）
      SELECT * FROM tbl_name WHERE key_col LIKE 'Pat%_ck%';（employ indexes使用index）
      SELECT * FROM tbl_name WHERE key_col LIKE '%Patrick%';（not employ indexes）
      SELECT * FROM tbl_name WHERE key_col LIKE other_col;（not employ indexes）
      ```

   3. A search using `*col_name* IS NULL` employs indexes if *col_name* is indexed.

   4. Any index that does not span all [`AND`](https://dev.mysql.com/doc/refman/8.0/en/logical-operators.html#operator_and) levels in the `WHERE` clause is not used to optimize the query. In other words, to be able to use an index, a prefix of the index must be used in every [`AND`](https://dev.mysql.com/doc/refman/8.0/en/logical-operators.html#operator_and) group.（index的前缀必须在每个AND组里面使用，Mysql**组合索引采用最左前缀原则**）

      The following `WHERE` clauses use indexes:

      ```sql
      ... WHERE index_part1=1 AND index_part2=2 AND other_column=3
      
          /* index = 1 OR index = 2 */
      ... WHERE index=1 OR A=10 AND index=2
      
          /* optimized like "index_part1='hello'" */
      ... WHERE index_part1='hello' AND index_part3=5
      
          /* Can use index on index1 but not on index2 or index3 */
          因为（index1=1 AND index2=2） OR （index1=3 AND index3=3），index1在2个OR条件中都有
      ... WHERE index1=1 AND index2=2 OR index1=3 AND index3=3;
      ```

      These `WHERE` clauses do *not* use indexes:

      ```sql
          /* index_part1 is not used */
      ... WHERE index_part2=1 AND index_part3=2
      
          /*  Index is not used in both parts of the WHERE clause  */
      ... WHERE index=1 OR A=10
      
          /* No index spans all rows  */
      ... WHERE index_part1=1 OR index_part2=10
      ```
      
   #### Hash Index Characteristics
   
#### 2. MySQL uses indexes for these operations:

   - `WHERE` .
   
   - 如果有多个索引（multiple indexes），**Mysql会选择其中一个**：the most[selective](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_selectivity) index(数据分布的一个属性，列中不同值的数目（其基数）除以表中的记录数。高选择性意味着列值相对唯一，并且可以通过索引有效地检索）。
   
   - If the table has **a multiple-column index**, any **leftmost prefix of the index** can be used by the optimizer to look up rows. For example, if you have a three-column index on `(col1, col2, col3)`, you have indexed search capabilities on `(col1)`, `(col1, col2)`, and `(col1, col2, col3)`. For more information, see [Section 8.3.6, “Multiple-Column Indexes”](https://dev.mysql.com/doc/refman/8.0/en/multiple-column-indexes.html).
   
       - As an alternative to a composite index, you can introduce a column that is “hashed” based on information from other columns. If this column is short, reasonably unique, and indexed, it might be faster than a “wide” index on many columns. In MySQL, it is very easy to use this extra column:

         ```sql
         SELECT * FROM tbl_name
           WHERE hash_col=MD5(CONCAT(val1,val2))
           AND col1=val1 AND col2=val2;
         ```

       - Suppose that a table has the following specification:

         ```sql
         CREATE TABLE test (
             id         INT NOT NULL,
             last_name  CHAR(30) NOT NULL,
             first_name CHAR(30) NOT NULL,
             PRIMARY KEY (id),
             INDEX name (last_name,first_name)
         );
         ```

         Therefore, the `name` index is used for lookups in the following queries:

         ```sql
         SELECT * FROM test WHERE last_name='Widenius';

         SELECT * FROM test
           WHERE last_name='Widenius' AND first_name='Michael';

         SELECT * FROM test
           WHERE last_name='Widenius'
           AND (first_name='Michael' OR first_name='Monty');

         SELECT * FROM test
           WHERE last_name='Widenius'
           AND first_name >='M' AND first_name < 'N';
         ```

         However, the `name` index is *not* used for lookups in the following queries:

         ```sql
         SELECT * FROM test WHERE first_name='Michael';

         SELECT * FROM test
           WHERE last_name='Widenius' OR first_name='Michael';
         ```

       - Suppose that you issue the following [`SELECT`](https://dev.mysql.com/doc/refman/8.0/en/select.html) statement:

         ```sql
         SELECT * FROM tbl_name
           WHERE col1=val1 AND col2=val2;
         ```

         If a multiple-column index exists on `col1` and `col2`, the appropriate rows can be fetched directly. If separate single-column indexes exist on `col1` and `col2`, the optimizer attempts to use the Index Merge optimization (see [Section 8.2.1.3, “Index Merge Optimization”](https://dev.mysql.com/doc/refman/8.0/en/index-merge-optimization.html)), or attempts to find the most restrictive index by deciding which index excludes more rows and using that index to fetch the rows.
         
       - if you have a three-column index on `(col1, col2, col3)`, you have indexed search capabilities on `(col1)`, `(col1, col2)`, and `(col1, col2, col3)`.
         
         ```sql
         SELECT * FROM tbl_name WHERE col1=val1;（use the index）
         SELECT * FROM tbl_name WHERE col1=val1 AND col2=val2;（use the index）
         
         SELECT * FROM tbl_name WHERE col2=val2;（not use the index）
         SELECT * FROM tbl_name WHERE col2=val2 AND col3=val3;（not use the index）
         ```

   - A multiple-column index可以看成是一个有序数组。 
     

   - To retrieve rows from other tables when performing joins. (前提是相同类型和大小)
   
   - To find the [`MIN()`](https://dev.mysql.com/doc/refman/8.0/en/group-by-functions.html#function_min) or [`MAX()`](https://dev.mysql.com/doc/refman/8.0/en/group-by-functions.html#function_max) value for a specific indexed column *key_col*. This is optimized by a preprocessor that checks whether you are using `WHERE *key_part_N* =*constant*` on all key parts that occur before *key_col* in the index. In this case, MySQL does a single key lookup for each [`MIN()`](https://dev.mysql.com/doc/refman/8.0/en/group-by-functions.html#function_min) or [`MAX()`](https://dev.mysql.com/doc/refman/8.0/en/group-by-functions.html#function_max) expression and replaces it with a constant. If all expressions are replaced with constants, the query returns at once. For example:
   
     ```sql
     SELECT MIN(key_part2),MAX(key_part2)
       FROM tbl_name WHERE key_part1=10;
     ```
   
   - To sort or group a table if the sorting or grouping is done on a leftmost prefix of a usable index (for example, `ORDER BY *key_part1*, *key_part2*`). If all key parts are followed by `DESC`, the key is read in reverse order. (Or, if the index is a descending index, the key is read in forward order.) See [Section 8.2.1.15, “ORDER BY Optimization”](https://dev.mysql.com/doc/refman/8.0/en/order-by-optimization.html), [Section 8.2.1.16, “GROUP BY Optimization”](https://dev.mysql.com/doc/refman/8.0/en/group-by-optimization.html), and [Section 8.3.13, “Descending Indexes”](https://dev.mysql.com/doc/refman/8.0/en/descending-indexes.html).
   
   1. ORDER BY Optimization：indexsort + filesort >参见：<https://segmentfault.com/a/1190000015987895>
   2. GROUP BY Optimization:Loose Index Scan+Tight Index Scan（**No**）
   3. Descending Indexes（**No**）
   
   - In some cases, a query can be optimized to retrieve values without consulting the data rows. (An index that provides all the necessary results for a query is called a [covering index](https://dev.mysql.com/doc/refman/8.0/en/glossary.html#glos_covering_index).) If a query uses from a table only columns that are included in some index, the selected values can be retrieved from the index tree for greater speed:
   
     ```sql
     SELECT key_part3 FROM tbl_name
       WHERE key_part1=1
     ```