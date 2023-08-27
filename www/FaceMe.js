var exec = require('cordova/exec');

var PLUGIN_NAME = "FaceMe";

var FaceMe = function(){}

function isFunction(obj) {
    return !!(obj && obj.constructor && obj.call && obj.apply);
  };

FaceMe.testPlugin = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "testPlugin", []);
};

FaceMe.initializeSDK = function(licenseKey, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "initializeSDK", [licenseKey]);
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

FaceMe.activateLicense = function(licenseKey, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "activateLicense", [licenseKey]);
};

FaceMe.deactivateLicense = function(onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "deactivateLicense", []);
};

FaceMe.detectFace = function(base64Image, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "detectFace", [base64Image]);
};

FaceMe.enrollFace = function(base64Image, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "enrollFace", [base64Image]);
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

FaceMe.addFace = function(username, onSuccess, onError){
    exec(onSuccess, onError, PLUGIN_NAME, "addFace", [username]);
};

FaceMe.startAntiSpoofing = function(options, onSuccess, onError){
    if(!options){
        options = {};
    }else if(isFunction(options)){
        onSuccess = options;
        options = {};
    }
    const IDLE_COLOR = "#FFFFFF";
    const ACTIVE_COLOR = "#FF8C00";
    const BLACK_COLOR = "#000000"
    const BORDER_FONT = 3.3;

    options.showFPS = options.showFPS || false;

    options.frameActiveColor = options.frameActiveColor || ACTIVE_COLOR;
    options.frameIdleColor = options.frameIdleColor || IDLE_COLOR;
    options.frameBorderWidth = options.frameBorderWidth || BORDER_FONT;
    options.showFrame = options.showFrame || false;

    //Circle
    options.circleActiveColor = options.circleActiveColor || ACTIVE_COLOR;
    options.circleIdleColor = options.circleIdleColor || IDLE_COLOR;
    options.circleBorderWidth = options.circleBorderWidth || BORDER_FONT;

    //Action Detail Hint
    options.actionDetailHintActiveColor = options.actionDetailHintActiveColor || ACTIVE_COLOR;
    options.actionDetailHintIdleColor = options.actionDetailHintIdleColor || IDLE_COLOR;
    options.actionDetailHintFont = options.actionDetailHintFont || null;
    options.actionDetailHintFontSize = options.actionDetailHintFontSize || 20;

    //Action Hint
    options.actionHintColor = options.actionHintColor || IDLE_COLOR;
    options.actionHintFont = options.actionHintFont || null;
    options.actionHintFontSize = options.actionHintFontSize || 24;

    //Progress Bar
    options.progressBarForegroundColor = options.progressBarForegroundColor || "#008BF7";
    options.progressBarBackgroundColor = options.progressBarBackgroundColor || "#55BB860B";
    options.progressBarWidth = options.progressBarWidth || 400;
    options.progressBarHeight = options.progressBarHeight || 6;

    //Footer
    options.footerTitleColor = options.footerTitleColor || BLACK_COLOR;
    options.footerTitleFont = options.footerTitleFont || null;
    options.footerTitleFontSize = options.footerTitleFontSize || -1;
    options.footerSubtitleColor = options.footerSubtitleColor || BLACK_COLOR;
    options.footerSubtitleFont = options.footerSubtitleFont || null;
    options.footerSubtitleFontSize = options.footerSubtitleFontSize || -1;
    options.showFooter = options.showFooter || false;

    //User Action Hint
    options.userActionHintColor = options.userActionHintColor || BLACK_COLOR;
    options.userActionHintFont = options.userActionHintFont || null;
    options.userActionHintFontSize = options.userActionHintFontSize || -1;
    options.showUserActionSteps = options.showUserActionSteps || false;

    //Speech Number
    options.speechNumberActiveColor = options.speechNumberActiveColor || "#1E90FF"; 
    options.speechNumberIdleColor = options.speechNumberIdleColor || "#888888"; 
    options.speechNumberFont = options.speechNumberFont || false; 
    options.speechNumberFontSize = options.speechNumberFontSize || -1; 

    //Speech Language
    options.speechLanguageColor = options.speechLanguageColor || BLACK_COLOR; 
    options.speechLanguageFont = options.speechLanguageFont || null; 
    options.speechLanguageBackgroundColor = options.speechLanguageBackgroundColor || "#DBEEFE"; //nr
    options.showSpeechLanguage = options.showSpeechLanguage || false; 

    //Alert Position
    options.alertDistanceToCircle = options.alertDistanceToCircle || -1;

    //Alert Background
    options.alertBackgroundColor = options.alertBackgroundColor || "#A59A0505";

    //Alert Title
    options.alertTitleColor = options.alertTitleColor || IDLE_COLOR;
    options.alertTitleFont = options.alertTitleFont || null;
    options.alertTitleFontSize = options.alertTitleFontSize || -1;

    //Alert Description
    options.alertDescriptionColor = options.alertDescriptionColor || IDLE_COLOR;
    options.alertDescriptionFont = options.alertDescriptionFont || null;
    options.alertDescriptionFontSize = options.alertDescriptionFontSize || -1;

    exec(onSuccess, onError, PLUGIN_NAME, "startAntiSpoofing", [
        options.showFPS = options.showFPS,

        options.frameActiveColor,
        options.frameIdleColor,
        options.frameBorderWidth,
        options.showFrame,

        //Circle
        options.circleActiveColor,
        options.circleIdleColor,
        options.circleBorderWidth,

        //Action Detail Hint
        options.actionDetailHintActiveColor,
        options.actionDetailHintIdleColor,
        options.actionDetailHintFont,
        options.actionDetailHintFontSize,

        //Action Hint
        options.actionHintColor,
        options.actionHintFont,
        options.actionHintFontSize,

        //Progress Bar
        options.progressBarForegroundColor,
        options.progressBarBackgroundColor,
        options.progressBarWidth,
        options.progressBarHeight,

        //Footer
        options.footerTitleColor,
        options.footerTitleFont,
        options.footerTitleFontSize,
        options.footerSubtitleColor,
        options.footerSubtitleFont,
        options.footerSubtitleFontSize,
        options.showFooter,

        //User Action Hint
        options.userActionHintColor,
        options.userActionHintFont,
        options.userActionHintFontSize,
        options.showUserActionSteps,

        //Speech Number
        options.speechNumberActiveColor,
        options.speechNumberIdleColor,
        options.speechNumberFont,
        options.speechNumberFontSize, 

        //Speech Language
        options.speechLanguageColor,
        options.speechLanguageFont,
        options.speechLanguageBackgroundColor, 
        options.showSpeechLanguage,

        //Alert Position
        options.alertDistanceToCircle,

        //Alert Background
        options.alertBackgroundColor,

        //Alert Title
        options.alertTitleColor,
        options.alertTitleFont,
        options.alertTitleFontSize,

        //Alert Description
        options.alertDescriptionColor,
        options.alertDescriptionFont,
        options.alertDescriptionFontSize,
    ]);
};

FaceMe.stopAntiSpoofing = function(onSuccess, onError){
  exec(onSuccess, onError, PLUGIN_NAME, "stopAntiSpoofing", []);  
};

module.exports = FaceMe;