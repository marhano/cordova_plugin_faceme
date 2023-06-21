---
title: FaceMe
description: Integrate faceme cyberlink sdk to cordova.
---
<!--
# license: Licensed to the Apache Software Foundation (ASF) under one
#         or more contributor license agreements.  See the NOTICE file
#         distributed with this work for additional information
#         regarding copyright ownership.  The ASF licenses this file
#         to you under the Apache License, Version 2.0 (the
#         "License"); you may not use this file except in compliance
#         with the License.  You may obtain a copy of the License at
#
#           http://www.apache.org/licenses/LICENSE-2.0
#
#         Unless required by applicable law or agreed to in writing,
#         software distributed under the License is distributed on an
#         "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
#         KIND, either express or implied.  See the License for the
#         specific language governing permissions and limitations
#         under the License.
-->

# cordova-plugin-faceme

<!-- [![Android Testsuite](https://github.com/apache/cordova-plugin-device/actions/workflows/android.yml/badge.svg)](https://github.com/apache/cordova-plugin-device/actions/workflows/android.yml) [![Chrome Testsuite](https://github.com/apache/cordova-plugin-device/actions/workflows/chrome.yml/badge.svg)](https://github.com/apache/cordova-plugin-device/actions/workflows/chrome.yml) [![iOS Testsuite](https://github.com/apache/cordova-plugin-device/actions/workflows/ios.yml/badge.svg)](https://github.com/apache/cordova-plugin-device/actions/workflows/ios.yml) [![Lint Test](https://github.com/apache/cordova-plugin-device/actions/workflows/lint.yml/badge.svg)](https://github.com/apache/cordova-plugin-device/actions/workflows/lint.yml) -->

## How to use

Import `FaceMe` in your component file:

```ts
import { FaceMe } from '@awesome-cordova-plugins/face-me/ngx';
```

Add `FaceMe` to the constructor:
```ts
constructor(private platform: Platform, private faceMe: FaceMe)
```
Ensure that the platform is ready before using the plugin:
```ts
this.platform.ready().then(() => {
  this.faceMe.testPlugin().then((res: any) => {
    this.logMessage(res);
  }).catch((error: any) => {
    this.logMessage(error);
  });
});
```

## Installation
Install `@awesome-cordova-plugins/core` and `@awesome-cordova-plugins/face-me` from github repository `https://github.com/marhano/awesome_cordova_plugins_faceme` this will act as wrapper for the actual plugin

    npm install @awesome-cordova-plugins-core
    npm install https://github.com/marhano/awesome_cordova_plugins_faceme.git

Install the cordova plugin FaceMe and copy `faceme-6.14.0.aar` to `cordova-plugin-faceme/src/android/libs`

    ionic cordova plugin add https://github.com/marhano/cordova_plugin_faceme
    

## Properties

- pluginTest
- initializeSDK
- activateLicense
- deactivateLicense
- detectFace
- enrollFace
- recognizeFace
- deleteFace
- updateFace

## pluginTest

Returns a test string to check if the plugin is working.

Example:

```ts
this.platform.ready().then(() => {
  this.faceMe.pluginTest().then((res: any) => {
    console.log(res);
  }).catch((error: any) => {
    console.log(error);
  });
});
```

## initializeSDK

Initialize the FaceMeSdk with a lincese key. Verify and register the license return FaceMeSDK ReturnCode.

## detectFace

Detect face and returns face holder data(FaceFeature, FaceAttribute, FaceInfo, FaceLandmark, FaceBitmap)

## enrollFace

Add a face collection in you local database. Accepts a unique string and returns face data.

## recognizeFace

Check if scanned face are inside the database. Return true or false.

## deleteFace

Delete a face.

## updateFace

Update a face




