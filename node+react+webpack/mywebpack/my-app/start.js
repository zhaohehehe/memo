document.write("It works.");
//require CSS 文件的时候都要写 loader 前缀 !style-loader!css-loader!，
//当然我们可以根据模块类型（扩展名）来自动绑定需要的 loader。
// 将 runoob1.js 中的 require("!style-loader!css-loader!./style.css") 修改为 require("./style.css") ：效果是一样的
require("!style-loader!css-loader!./style.css");//webpack start.js bundle.js
//require("./style.css");//webpack start.js bundle.js --module-bind 'css=style-loader!css-loader',但是没有效果
document.write(require("./start2.js"));