import proxy from 'koa-proxies';
import { PROXY_URL } from '../../client/src/utils';

/**
 * 代理插件
 */
export default proxy('/apis', {
    target: PROXY_URL,
    changeOrigin: true,
    rewrite: path => path.replace(/^\/apis/, ''),
    logs: true
});