var exec = require('cordova/exec');

var PLUGIN_NAME = "FaceMe";

var FaceMe = function(){}

FaceMe.testPlugin = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
};

FaceMe.initializeSDK = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "initializeSDK", []);
};

FaceMe.extractFace = function(base64Image, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "extractFace", [base64Image]);
};

module.exports = FaceMe;