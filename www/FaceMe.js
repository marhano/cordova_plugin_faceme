var exec = require('cordova/exec');

var PLUGIN_NAME = "FaceMe";
var FaceMe = function(){}

function isFunction(obj){
    return !!(obj && obj.constructor && obj.call && obj.apply);
};

FaceMe.testPlugin = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
}

FaceMe.initializeSDK = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "initializeSDK", []);
};

module.exports = FaceMe;



// exports.coolMethod = function (arg0, success, error) {
//     exec(success, error, 'FaceMe', 'coolMethod', [arg0]);
// };

// exports.initializeFaceme = function(success, error){
//     exec(success, error, 'FaceMe', 'initializeFaceme', []);
// }
