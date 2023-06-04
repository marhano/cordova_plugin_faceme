var exec = require('cordova/exec');

exports.coolMethod = function (arg0, success, error) {
    exec(success, error, 'FaceMe', 'coolMethod', [arg0]);
};

exports.initializeFaceme = function(success, error){
    exec(success, error, 'FaceMe', 'initializeFaceme', []);
}
