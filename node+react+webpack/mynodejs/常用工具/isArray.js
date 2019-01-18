var util = require('util');

util.isArray([])
// true
util.isArray(new Array)
// true
util.isArray({})
// false

var util = require('util');

util.isRegExp(/some regexp/)
// true
util.isRegExp(new RegExp('another regexp'))
// true
util.isRegExp({})
// false
var util = require('util');

util.isDate(new Date())
// true
util.isDate(Date())
// false (without 'new' returns a String)
util.isDate({})
// false
var util = require('util');

//如果给定的参数 "object" 是一个错误对象返回true，否则返回false
util.isError(new Error())
// true
util.isError(new TypeError())
// true
util.isError({ name: 'Error', message: 'an error occurred' })
// false