//if(process.env.NODE_ENV === 'production') {
    // if(process.env.BUILD === 'server') {
    //     module.exports = require('./config/webpack.prod.config').serverConfig;
    // }else {
    //     module.exports = require('./config/webpack.prod.config').clientConfig;
    // }
    //module.exports = require('./config/webpack.prod.config');
//}else {
 //   module.exports = require('./config/webpack.dev.config');
//}

//只需执行webpack命令即可
var webpack=require('webpack');
module.exports = {
    entry: "./start.js",
    output: {
        path: __dirname,
        filename: "bundle.js"
    },
    module: {
        loaders: [
            { test: /\.css$/, loader: "style-loader!css-loader" }
        ]
    },
    //执行npm install webpack --save-dev
    plugins:[
        new webpack.BannerPlugin('菜鸟教程 webpack 实例')
        ]
};

