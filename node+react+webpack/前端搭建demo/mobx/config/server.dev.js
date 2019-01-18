const config = require('./webpack.dev.config');
// Provide custom regenerator runtime and core-js
require('babel-polyfill');

// Node babel source map support
require('source-map-support').install();

// Javascript require hook
require('babel-register')({
    presets: ['es2015', 'react', 'stage-0'],
    plugins: ['webpack-alias', 'add-module-exports', 'transform-decorators-legacy']
});

// Css require hook
require('css-modules-require-hook')({
    generateScopedName: (name, filePath) => {
        
        let paths = filePath.replace(config.context + '\\', '').split('\\');
        let fileName = paths.pop().replace(/.css$/, '').replace(/\./g, '-');
        let path = paths.join('-');
        return path + '---' + fileName + '--' + name;
    }
});

// Image require hook
require('asset-require-hook')({
    name: '/[hash].[ext]',
    extensions: ['jpg', 'png', 'gif', 'webp', 'svg'],
    limit: 8192
});

const convert = require('koa-convert');
const webpack = require('webpack');
const fs = require('fs');
const path = require('path');
const devMiddleware = require('koa-webpack-dev-middleware');
const hotMiddleware = require('koa-webpack-hot-middleware');
const views = require('koa-views');
const serve = require('koa-static');
const compiler = webpack(config);

// 将html渲染到server下
compiler.plugin('emit', (compilation, callback) => {
    const assets = compilation.assets;
    let file, data;

    Object.keys(assets).forEach(key => {
        if (key.match(/\.html$/)) {
            file = path.resolve(__dirname, 'server', key);
            data = assets[key].source();
            fs.writeFileSync(file, data);
        }
    });
    callback();
});

const start = (app, router, clientRoute, port) => {
    app.use(views(path.resolve(__dirname, '../server'), { map: { html: 'ejs' } }));
    app.use(clientRoute);
    app.use(router.routes(), router.allowedMethods());
    console.log(`\n==> 🌎  Listening on port ${port}. Open up http://localhost:${port}/ in your browser.\n`);
    app.use(convert(devMiddleware(compiler, {
        noInfo: true,
        publicPath: config.output.publicPath
    })));
    app.use(convert(hotMiddleware(compiler)));
    app.listen(port);
};

module.exports = start;