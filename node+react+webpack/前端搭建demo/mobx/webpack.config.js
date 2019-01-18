if(process.env.NODE_ENV === 'production') {
    // if(process.env.BUILD === 'server') {
    //     module.exports = require('./config/webpack.prod.config').serverConfig;
    // }else {
    //     module.exports = require('./config/webpack.prod.config').clientConfig;
    // }
    module.exports = require('./config/webpack.prod.config');
}else {
    module.exports = require('./config/webpack.dev.config');
}