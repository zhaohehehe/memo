const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');

const CLIENT_BUILD_PATH = path.resolve(__dirname, '../build/client');
const CLIENT_PATH = path.resolve(__dirname, '../client');
const SERVER_PATH = path.resolve(__dirname, '../server');

module.exports = {
    context: CLIENT_PATH,
    devtool: 'eval-source-map',
    entry: {
        bundle: [
            './',
            'webpack-hot-middleware/client?path=/__webpack_hmr&timeout=20000'
        ],
        polyfill: 'babel-polyfill',
        vendor: [
            'react',
            'react-dom'
        ]
    },
    output: {
        path: CLIENT_BUILD_PATH,
        filename: '[name].js',
        chunkFilename: '[name].chunk.[id].js',
        publicPath: '/'
    },
    module: {
        rules: [
            {
                // 预加载，使用eslint进行代码规范检测
                test: /\.(js|jsx)$/,
                enforce: 'pre',
                use: 'eslint-loader'
            },
            {
                // 加载js jsx
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015', 'react', 'stage-0', 'react-hmre'],
                        plugins: ['webpack-alias', 'add-module-exports', 'transform-decorators-legacy']
                    }
                }]
            },
            {
                // 图片资源
                test: /\.(jpg|png|gif|webp|svg)$/,
                use: [{
                    loader: 'url-loader',
                    options: {
                        limit: 8192,
                        name: '[path][name].[ext]'
                    }
                }]
            },
            {
                // 音视频文件
                test: /\.(mp4|ogg|mp3)$/,
                use: [{
                        loader: 'file-loader',
                        options: {
                            name: '[path][name].[ext]'
                        }
                }]
            },
            {
                // 字体文件
                test: /\.(woff|woff2|eot|ttf|otf)(\?v=\d+\.\d+\.\d+)?$/,
                use: [{
                    loader: 'file-loader',
                    options: {
                        name: '[path][name].[ext]'
                    }
                }]
            },
            {
                test: /\.json$/,
                use: ['json-loader']
            },
            {
                test: /\.html$/,
                use: [{
                    loader: 'html-loader',
                    options: {
                        minimize: false
                    }
                }]
            },
            {
                // css文件
                test: /[^(\.m)]\.css$/,
                use: [
                    'style-loader',
                    {
                        loader: 'css-loader',
                        options: {
                            sourceMap: true
                        }

                    },
                    {
                        loader: 'postcss-loader',
                        options: {
                            plugins: [
                                autoprefixer()
                            ],
                            sourceMap: true
                        }
                    }
                ]
            },
            {
                // .m.css
                test: /\.m\.css$/,
                use: [
                    'style-loader',
                    {
                        loader: 'css-loader',
                        options: {
                            modules: true,
                            localIdentName: '[path]--[name]--[local]',
                            sourceMap: true
                        }
                    },
                    {
                        loader: 'postcss-loader',
                        options: {
                            plugins: [
                                autoprefixer()
                            ],
                            sourceMap: true
                        }
                    }
                ]
            },
            {
                // less
                test: /\.less$/,
                use: [
                    'style-loader',
                    {
                        loader: 'css-loader',
                        options: {
                            sourceMap: true
                        }
                    },
                    {
                        loader: 'postcss-loader',
                        options: {
                            plugins: [
                                autoprefixer()
                            ],
                            sourceMap: true
                        }
                    },
                    {
                        loader: 'less-loader',
                        options: { sourceMap: true }
                    }
                ]
            },
            // scss sass 需要依赖node-sass sass-loader: yarn add -D node-sass sass-loader
            // {
            //     // scss sass
            //     test: /\.(sass|scss)$/,
            //     use: [
            //         'style-loader',
            //         {
            //             loader: 'css-loader',
            //             options: {
            //                 sourceMap: true
            //             }
            //         },
            //         {
            //             loader: 'postcss-loader',
            //             options: {
            //                 plugins: [
            //                     autoprefixer()
            //                 ],
            //                 sourceMap: true
            //             }
            //         },
            //         {
            //             loader: 'sass-loader',
            //             options: { sourceMap: true }
            //         }
            //     ]
            // }
        ]
    },
    resolve: {
        extensions: ['.js', '.jsx', '.json'],
        alias: {
            'src': path.resolve(__dirname, '../client/src')
        },
        modules: [path.resolve(__dirname), '../node_modules']
    },
    plugins: [
        new webpack.optimize.ModuleConcatenationPlugin(),
        new webpack.optimize.CommonsChunkPlugin({
            names: ['vendor', 'polyfill', 'manifest'],
            filename: '[name].js'
        }),
        new webpack.HotModuleReplacementPlugin(),
        new webpack.NoEmitOnErrorsPlugin(),
        new webpack.DefinePlugin({ 'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV) }),
        new HtmlWebpackPlugin({
            filename: path.resolve(SERVER_PATH, 'index.html'),
            template: path.resolve(CLIENT_PATH, 'index.html')
        }),
        new ProgressBarPlugin({ summary: false })
    ]
};
