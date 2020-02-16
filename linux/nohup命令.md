## nohup命令

1. &的意思是在后台运行， 什么意思呢？ 意思是说， 当你在执行 ./start.sh & 的时候， 即使你用Ctrl + C, 那么start.sh照样运行（因为**对SIGINT信号免疫**）。 但是要注意， 如果你直接关掉shell后， 那么，start.sh进程同样消失。 可见， &的后台并不硬（因为**对SIGHUP信号不免疫**）。
   - 结果会输出到终端
   - 使用Ctrl + C发送SIGINT信号，程序免疫
   - 关闭shell发送SIGHUP信号，程序关闭

2. nohup的意思是忽略SIGHUP信号， 所以当运行nohup ./start.sh的时候， 关闭shell, 那么start.sh进程还是存在的（因为**对SIGHUP信号免疫**）。 但是， 要注意， 如果你直接在shell中用Ctrl + C, 那么start.sh进程也是会消失的（因为**对SIGINT信号不免疫**）。

   - 结果默认会输出到nohup.out
   - 使用Ctrl + C发送SIGINT信号，程序关闭
   - 关闭shell发送SIGHUP信号，程序免疫

3. 线上nohup和&组合，同时免疫SIGINT和SIGHUP信号，并且将日志记录到nohup.out文件（而不是标准终端）。

   nohup java -jar test.jar &

   tail -f nohup.out