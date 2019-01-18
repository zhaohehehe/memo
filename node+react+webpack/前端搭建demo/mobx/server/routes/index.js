import fs from 'fs';
import path from 'path';
import Router from 'koa-router';
import userRouter from './user';
import heroRouter from './hero';

// 定义路由，其前缀 /api
const router = new Router({ prefix: '/api' });

// 添加user 路由
router.use(userRouter.routes(), userRouter.allowedMethods());
// 添加hero 路由
router.use(heroRouter.routes(), heroRouter.allowedMethods());

export default router;