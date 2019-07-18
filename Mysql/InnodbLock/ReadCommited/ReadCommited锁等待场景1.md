# 条件：RC

> 多个insert索引冲突

1. T1：

   begin;

   insert into lock_test(id,u_1,u_2,`key`)  values('003','003','003','003');

2. T2:

   begin;

   insert into lock_test(id,u_1,u_2,`key`)  values('004','003','003','004');
   
   ![img](file:///insert_scene01/1.png)

3. T3:

   begin;

   insert into lock_test(id,u_1,u_2,`key`)  values('006','004','004','006');

   ![img](file:///insert_scene01/2.png)

   分析：|代表T2 冲突后锁定

   001	001	001	001

   > 002	001	002	002
   >
   > **003	003	003	003（T1成功insert）**
   >
   > **004	003	003	004（T2冲突）**

   **006	004	004	006(T3未锁定区域，成功insert)**

   005	005	005	005
   009	009	009	009

   

   

   

   **或者**

   begin;

   insert into lock_test(id,u_1,u_2,`key`)  values('006','002','002','002');

   ![img](file:///insert_scene01/3.png)

   分析：|代表T2 冲突后锁定

   001	001	001	001

   > 002	001	002	002
   >
   > **006	002	002	002(T3锁定区域，等待锁)**
   >
   > **003	003	003	003（T1成功insert）**
   >
   > **004	003	003	004（T2冲突）**

   005	005	005	005
   009	009	009	009

   