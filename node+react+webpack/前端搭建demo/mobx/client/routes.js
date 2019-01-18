// 服务器上没有require.ensure方法，所以定义一个
if (typeof require.ensure !== 'function') {
    require.ensure = function (dependencies, callback) {
        callback(require);
    };
}

const routes = {
    childRoutes: [{
        path: '/',
        component: require('./src/Root'),
        indexRoute: {
            getComponent(nextState, callback) {
                require.ensure([], require => {
                    callback(null, require('./src/home/Home'));
                }, 'home');
            }
        },
        childRoutes: [{
            // 添加hero模块路由
            getChildRoutes(location, callback) {
                require.ensure([], require => {
                    callback(null, require('./src/hero/hero.routes'));
                }, 'hero.routes');
            }
        }]
    }]
};

export default routes;