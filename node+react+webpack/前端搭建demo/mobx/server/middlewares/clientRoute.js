import React from 'react';
import { renderToString } from 'react-dom/server';
import { match, RouterContext } from 'react-router';
import { Provider } from 'mobx-react';
import { createStore } from '../../client/stores';
import routes from '../../client/routes';

const store = createStore();

function _match(location) {
    return new Promise((resolve, reject) => {
        match(location, (error, redirectLocation, renderProps) => {
            if (error) {
                return reject(error);
            }

            resolve({ redirectLocation, renderProps });
        });
    });
}

export default async (ctx, next) => {
    try {
        const { redirectLocation, renderProps } = await _match({ routes, location: ctx.url });
        if (renderProps) {
            await ctx.render('index', {
                root: renderToString(
                    <Provider {...store}>
                        <RouterContext {...renderProps} />
                    </Provider>
                ),
                state: JSON.stringify(store)
            });
        } else {
            await next();
        }
    } catch (e) {
        ctx.status = 500;
        ctx.body = e.message;
    }
};