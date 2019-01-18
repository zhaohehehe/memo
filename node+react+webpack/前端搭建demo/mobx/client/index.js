import React from 'react';
import { render } from 'react-dom';
import { Router, match, browserHistory } from 'react-router';
import { Provider } from 'mobx-react';
import { createStore } from './stores';
import routes from './routes';
import './style.css';

const store = createStore(window.__INITIAL_STATE__);
match({ history: browserHistory, routes }, (error, redirectLocation, renderProps) => {
    render(
        <Provider {...store}>
            <Router {...renderProps}/>
        </Provider>,
        document.getElementById('root')
    );
});