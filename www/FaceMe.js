var exec = require('cordova/exec');

var PLUGIN_NAME = "FaceMe";

function isFunction(obj){
    return !!(obj && obj.constructor && obj.call && obj.apply);
};

exports.initializeSDK = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "initializedSDK", []);
};

exports.testPlugin = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
};

//var FaceMe = function(){}

// FaceMe.testPlugin = function(onSuccess, onError){
//     exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
// }

// FaceMe.initializeSDK = function(onSuccess, onError){
//     exec(onSuccess, onError, PLUGIN_NAME, "initializeSDK", []);
// };

// module.exports = FaceMe;