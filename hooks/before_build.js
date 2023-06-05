#!/usr/bin/env node

var fs = require('fs');
var path = require('path');

module.exports = function(context) {
    var platformRoot = path.join(context.opts.projectRoot, 'platforms/android');
    var gradleFile = path.join(platformRoot, 'app/build.gradle');

    if (fs.existsSync(gradleFile)) {
        var buildGradle = fs.readFileSync(gradleFile, 'utf8');
        var newLine = "\n";
        var insertionPoint = buildGradle.lastIndexOf('dependencies {');
      
        if (insertionPoint !== -1) {
          // Find the position of the next line after 'dependencies {'
          var nextLineIndex = buildGradle.indexOf('\n', insertionPoint);
      
          if (nextLineIndex !== -1) {
            var implementationLine = '    implementation files(\'src/main/libs/faceme-6.14.0.aar\')';
      
            // Check if the 'implementation' line already exists
            if (buildGradle.includes(implementationLine)) {
              console.log('The implementation line already exists.');
              return;
            }
      
            // Insert the 'implementation' line after the 'dependencies {' line
            var newBuildGradle =
              buildGradle.substring(0, nextLineIndex) +
              implementationLine +
              newLine +
              buildGradle.substring(nextLineIndex);
      
            fs.writeFileSync(gradleFile, newBuildGradle, 'utf8');
          }
        }
    }      
      
};
