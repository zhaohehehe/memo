let start;
if (process.env.NODE_ENV && process.env.NODE_ENV === 'production') {
    start = require('../config/server.prod.js');
} else {
    start = require('../config/server.dev.js');
}
const Koa = require('koa');
const json = require('koa-json');
const bodyParser = require('koa-bodyparser');
const logger = require('koa-logger');
const session = require('koa-session');
const compress = require('koa-compress');
const convert = require('koa-convert');
const router = require('./routes');
const clientRoute = require('./middlewares/clientRoute');
const proxy = require('./middlewares/proxy');
const port = process.env.port || 3000;
const app = new Koa();
app.keys = ['this is a fucking secret'];
app.use(convert(session(app)));
app.use(compress());
app.use(proxy);
app.use(bodyParser());
app.use(json());
app.use(logger());
start(app, router, clientRoute, port);