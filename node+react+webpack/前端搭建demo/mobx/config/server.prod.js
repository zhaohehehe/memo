import 'babel-polyfill';
import serve from 'koa-static';
import path from 'path';
import views from 'koa-views';

const start = (app, router, clientRoute, port) => {
    app.use(views(path.resolve(__dirname, '../dist/server'), { map: { html: 'ejs' } }));
    app.use(serve(path.resolve(__dirname, '../dist/client')));
    app.use(clientRoute);
    app.use(router.routes());
    app.use(router.allowedMethods());
    app.listen(port);
    console.log(`\n==> 🌎  Listening on port ${port}. Open up http://localhost:${port}/ in your browser.\n`);
};

module.exports = start;