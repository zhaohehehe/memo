/** 打包成静态资源文件 */
const path = require('path');
const webpack = require('webpack');
const autoprefixer = require('autoprefixer');
const HtmlWebpackPlugin = require('html-webpack-plugin');
const ExtractTextPlugin = require('extract-text-webpack-plugin');
const ProgressBarPlugin = require('progress-bar-webpack-plugin');
const OptimizeCssAssetsPlugin = require('optimize-css-assets-webpack-plugin');

const DIST_PATH = path.resolve(__dirname, '../dist');
const CLIENT_PATH = path.resolve(__dirname, '../client');

module.exports = {
    context: CLIENT_PATH,
    entry: {
        bundle: './',
        polyfill: 'babel-polyfill',
        vendor: [
            'react',
            'react-dom'
        ]
    },
    output: {
        path: DIST_PATH,
        filename: '[name].[chunkhash:8].js',
        chunkFilename: 'chunk.[name].[chunkhash:8].js',
        publicPath: './'
    },
    module: {
        loaders: [
            {
                // .js .jsx
                test: /\.(js|jsx)$/,
                exclude: /node_modules/,
                use: [{
                    loader: 'babel-loader',
                    options: {
                        presets: ['es2015', 'react', 'stage-0'],
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
                        name: 'assets/images/[name].[hash:8].[ext]'
                    }
                }]
            },
            {
                // 音视频文件
                test: /\.(mp4|ogg|mp3)$/,
                use: [{
                        loader: 'file-loader',
                        options: {
                            name: 'assets/medias/[name].[hash:8].[ext]'
                        }
                }]
            },
            {
                // 字体文件
                test: /\.(woff|woff2|eot|ttf|otf)(\?v=\d+\.\d+\.\d+)?$/,
                use: [{
                        loader: 'file-loader',
                        options: {
                            name: 'assets/fonts/[name].[hash:8].[ext]'
                        }
                }]
            },
            {
                test: /\.html$/,
                use: [{
                    loader: 'html-loader',
                    options: {
                        minimize: true
                    }
                }]
            },
            {
                // css文件
                test: /[^(\.m)]\.css$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: [
                        {
                            loader: 'css-loader',
                            options: {
                                minimize: true
                            }
                        },
                        {
                            loader: 'postcss-loader',
                            options: {
                                plugins: [
                                    autoprefixer()
                                ]
                            }
                        }
                    ]
                })
            },
            {
                // .m.css
                test: /\.m\.css$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: [
                        {
                            loader: 'css-loader',
                            options: {
                                modules: true,
                                localIdentName: '[path]--[name]--[local]',
                                minimize: true
                            }
                        },
                        {
                            loader: 'postcss-loader',
                            options: {
                                plugins: [
                                    autoprefixer()
                                ]
                            }
                        }
                    ]
                })
            },
            {
                // less
                test: /\.less$/,
                use: ExtractTextPlugin.extract({
                    fallback: 'style-loader',
                    use: [
                        {
                            loader: 'css-loader',
                            options: {
                                minimize: true
                            }
                        },
                        {
                            loader: 'postcss-loader',
                            options: {
                                plugins: [
                                    autoprefixer()
                                ]
                            }
                        },
                        'less-loader'
                    ]
                })
            },
            // scss sass 需要依赖node-sass sass-loader: yarn add -D node-sass sass-loader
            // {
            //     // scss sass
            //     test: /\.(sass|scss)$/,
            //     use: ExtractTextPlugin.extract({
            //         fallback: 'style-loader',
            //         use: [
            //             {
            //                 loader: 'css-loader',
            //                 options: {
            //                     minimize: true
            //                 }
            //             },
            //             {
            //                 loader: 'postcss-loader',
            //                 options: {
            //                     plugins: [
            //                         autoprefixer()
            //                     ]
            //                 }
            //             },
            //             'sass-loader'
            //         ]
            //     })
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
            filename: '[name].[chunkhash:8].js'
        }),
        new webpack.optimize.UglifyJsPlugin({
            compress: { warnings: false },
            comments: false
        }),
        new webpack.DefinePlugin({ 'process.env.NODE_ENV': JSON.stringify(process.env.NODE_ENV) }),
        new HtmlWebpackPlugin({
            filename: path.resolve(DIST_PATH, 'index.html'),
            template: path.resolve(CLIENT_PATH, 'index.html')
        }),
        new ExtractTextPlugin({
            filename: '[name].[contenthash:8].css',
            allChunks: true
        }),
        new OptimizeCssAssetsPlugin({
            cssProcessorOptions: { discardComments: { removeAll: true } }
        })
    ]
};
