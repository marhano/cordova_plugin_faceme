# Guide to Creating a Cordova Plugin (Noob Edition)

## 1. Create a test project to test your plugin:
   - Open a terminal and run the following command:
     ```
     ionic start <project_name> blank --type=angular --cordova
     ```

## 2. Set up the plugin folder:
   - Create a separate folder outside your project to store your plugins. You can name it anything you want, e.g., "cordova_plugins."
   - Open the folder you created in Visual Studio Code (VSCode).

## 3. Install Plugman:
   - Install Plugman globally by running the following command:
     ```
     npm install -g plugman
     ```

## 4. Create the plugin project:
   - Run the following command to create the plugin project, following the naming convention provided (use <> to avoid conflicts later on):
     ```
     plugman create --name <PluginName> --plugin_id <cordova-plugin-pluginname> --plugin_version <1.0.0>
     ```

## 5. Open the plugin project in VSCode:
   - Navigate to the plugin you just created using the terminal:
     ```
     cd PluginName
     ```
   - Open the project in VSCode:
     ```
     code .
     ```
     
## 6. Add platforms to the plugin project:
   - To add Android support, run the following command:
     ```
     plugman platform add --platform_name android
     ```
   - For iOS support, run the following command:
     ```
     plugman platform add --platform_name ios
     ```
     
## 7. Modify plugin.xml:
   - Open the plugin.xml file in VSCode.
   - Find the `<feature>` tag and modify the Android project with an appropriate package name (e.g., inc.bastion.pluginname.PluginName). Ensure that the separator is a dot (".") for it to work correctly.
   - Find the `<source-file>` change the target-dir to the package name swap the "." with "/" of the packange name e.g.,

      ```xml
      <source-file src="src/android/PluginName.java" target-dir="src/inc/bastion/pluginname/PluginName" />
      ```
   - Open the `PluginName.java` from `src/android/PluginName.java` and do the same with the package name (e.g,).

      ```java
      package inc.bastion.pluginname;
      ```

## 8. Create package.json file:
   - Generate a package.json file for your plugin project:
     ```
     plugman createpackage.json .
     ```
   - Leave the name field as default.

## 9. Congratulations! You've created your first Cordova plugin.

# Testing the Plugin:

## 1. Go to the test project you created.
## 2. Install the plugin using the following command:
   ```
   ionic cordova plugin add <plugin-directory> --link
   ```
   - Example: `ionic cordova plugin add ../cordova_plugins/PluginName --link`
   - If your plugin is located outside your test project, use `"../"` to navigate back.

## 3. Testing the plugin:
   - Open home.page.ts in your test project.
   - Add the following code at the very top, below the imports:
     ```ts
     declare var cordova: any;
     ```
   - Import the Platform module and add it to the constructor:
     ```ts
     import { Platform } from '@ionic/angular';
     
     constructor(private platform: Platform) {}
     ```

## 4. Inside the ngOnInit() function:
   ```ts
   this.platform.ready().then(() => {
       const success = (result: any) => {
           alert(result);
       };
       cordova.exec(success, null, 'TestPlugin', 'coolMethod', ['Hello World']);
   });
   ```

   - Replace "TestPlugin" with your plugin name.
   - Replace "coolMethod" with the method name inside your plugin (check PluginName.js in www/PluginName.js and src/android/PluginName.java).
   - Replace "'Hello World'" with the parameter you want to pass.

## 5. Test the plugin:
   - Run the following commands in your terminal to test the application on an emulator or actual device:
     ```
     ionic cordova platform add android
     ionic cordova build android
     ionic cordova run android
     ```

   - The application should display an alert with the message "Hello World."

# Debugging

When modifying the plugin project and reinstalling at the test project make sure to remove the platform before installing the plugin again to reset the android platform to it's clean state.

```
ionic cordova platform remove android
ionic cordova plugin add ../cordova_plugins/PluginName --link
ionic cordova platform add android
ionic cordova run android
```
