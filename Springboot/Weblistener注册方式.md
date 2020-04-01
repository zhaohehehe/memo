# WebListener

1. @WebListener注解的class内部使用其他注解失效：

   将@WebListener改成@Component注解，springboot会自动将其包装为`ServletListenerRegistrationBean`对象

   https://www.cnblogs.com/yihuihui/p/12034522.html