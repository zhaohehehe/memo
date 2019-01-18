/*
 * utils for http
 * 
 */

import axios from 'axios';

export const http = {
    instance: axios.create({
        headers: {
            'Content-Type': 'application/json'
        },
        withCredentials: true,
        timeout: 1000 * 30
    }),
    create: (config) => {
        this.instance = axios.create(config);
        return this;
    }
};

export const post = (url, params, config) => {
    return new Promise((resolve, reject) => {
        http.instance.post(url, JSON.stringify(params), config)
            .then(res => resolve(res.data))
            .catch(error => reject(error));
    });
};
http.post = post;

export const get = (url, params, config) => {
    return new Promise((resolve, reject) => {
        http.instance.get(url, Object.assign({}, { params }, config))
            .then(response => resolve(response.data))
            .catch(error => reject(error));
    });
};
http.get = get;