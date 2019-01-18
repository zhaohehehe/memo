if (typeof require.ensure !== 'function') {
    require.ensure = function (dependencies, callback) {
        callback(require);
    };
}

const routes = {
    path: 'hero',
    component: require('./Hero'),
    indexRoute: {
        getComponent(nextState, callback) {
            require.ensure([], require => {
                callback(null, require('./hero-list/HeroList'));
            }, 'hero-list');
        }
    }
};

export default routes;