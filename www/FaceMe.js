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

FaceMe.activateLicense = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "activateLicense", []);
};

FaceMe.deactivateLicense = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "deactivateLicense", []);
};

FaceMe.detectFace = function(base64Image, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "detectFace", [base64Image]);
};

FaceMe.enrollFace = function(username, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "enrollFace", [username]);
};

FaceMe.recognizeFace = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "recognizeFace", []);
};

FaceMe.deleteFace = function(faceId, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "deleteFace", [faceId]);
};

FaceMe.updateFace = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "updateFace", []);
};

FaceMe.selectFace = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "selectFace", []);
};

module.exports = FaceMe;