console.log( __filename );
console.log( __dirname );

function printHello(){
    console.log( "Hello, World000!");
 }
 // 两秒后执行以上函数
 setTimeout(printHello, 2000);

 function printHello(){
    console.log( "Hello, World111!");
 }
 // 两秒后执行以上函数
 var t = setTimeout(printHello, 2000);
 
 // 清除定时器
 clearTimeout(t);

 function printHello(){
    console.log( "Hello, World222!");
 }
 // 两秒后执行以上函数
 setInterval(printHello, 2000);
 //console,process等