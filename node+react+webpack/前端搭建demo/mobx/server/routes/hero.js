import Router from 'koa-router';

const router = new Router({ prefix: '/hero' });

const heros = [{
    id: 1,
    name: '李白',
    desc: '诗仙李白'
}, {
    id: 2,
    name: '杜甫',
    desc: '诗圣杜甫'
}];

router.get('/getHeros', async (ctx) => ctx.body = heros.map(hero => ({ id: hero.id, name: hero.name })));
router.post('/addHero', async (ctx) => {
    let hero = Object.assign({}, ctx.request.body, { id: heros.length + 1 });
    heros.push(hero);
    ctx.body = { id: hero.id, name: hero.name };
});

export default router;