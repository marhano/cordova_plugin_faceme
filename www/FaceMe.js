var exec = require('cordova/exec');

var PLUGIN_NAME = "FaceMe";

var FaceMe = function(){}

FaceMe.testPlugin = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
};

FaceMe.initializeSDK = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "initializeSDK", []);
};

FaceMe.getBase64Image = function(base64Image, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "getBase64Image", [base64Image]);
};

FaceMe.getBoundingBox = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "getBoundingBox", []);
};

FaceMe.getBitmapImage = function(pixelData, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "getBitmapImage", [pixelData]);
};

module.exports = FaceMe;