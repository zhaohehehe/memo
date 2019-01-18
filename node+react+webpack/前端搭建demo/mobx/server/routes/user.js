import Router from 'koa-router';

const router = new Router({ prefix: '/user' });

router.get('/getUserInfo', async (ctx) => ctx.body = { name: 'aaaa' });

export default router;
