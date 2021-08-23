## sed命令

1. 以行为单位的新增/删除，并将结果输出到标准输出

  **nl命令在linux系统中用来计算显示文件中行号**

  sed '10a\1111111111111111' testfile（第10行后添加一行到testfile）

  nl testfile | sed '2a newcontext' （第2行后，即第3行添加）

  nl testfile | sed '2i newcontext'（第2行前，即第2行添加）

  nl testfile | sed '2i newline1\nnewline2'（追加多行以\n进行追加新行）
  nl testfile | sed '/world/a\taobao\njingdong\naliyun' lb.txt（world行下插入多行）

  nl testfile | sed '2,5d'（删除第 2~5 行）

  nl testfile | sed '2d'（只删除第2行）

  nl testfile | sed '2,$d'（删除第3行到最后）

2. 以行为单位的替换与显示，并将结果输出到标准输出

   1. nl testfile  | sed '2,5c hello'（2到5行的全部内容替换为hello）
   2. nl testfile  | sed -n '2,5p'（只显示2到5行）
   3. sed -n '5,10p' testfile （只显示2到5行）

3. 数据的搜寻并显示

   1.  nl testfile | sed  '/hello/p'（如果文件中找到hello，输出所有行）
   2.  nl testfile | sed -n '/hello/p'（如果文件中找到hello，输出匹配行）

4. 数据的搜寻并删除

   1.  nl testfile | sed  '/hello/d'（删除所有包含hello的行，其他行输出）

5. 数据的搜寻并替换

   1. nl testfile | sed 's/要被取代的字串/新的字串/g'
   2. nl testfile | sed 's/要被取代的字串//g'（删除）

6. 多点编辑

   1. nl testfile  | sed -e '9,$d' -e 's/hello/byby/g'（删除并替换）

7. 截取日志

   1. 某个时间到当前的日志：

       sed -n '/2020-02-16 15:02:33/,$p' test.log

      或者

       sed -n '/2020-02-16 15:02:33/,$'p test.log

   2. 某个时间段的日志：

       sed -n '/2020-02-16 15:02:33/,/2020-02-16 15:03:33/p' test.log > 2016.txt

      或者

      sed -n '/2020-02-16 15:02:33/,/2020-02-16 15:03:33/'p test.log > 2016.txt
