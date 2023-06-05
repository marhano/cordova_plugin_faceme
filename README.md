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
You need to declare cordova: any

```ts
declare cordova var: any;

constructor(private platform : Platform){}
ngOnInit(){
  const success = (result: any) => {
    this.logMessage(result);
  };
  this.platform.ready().then(() => {
    cordova.exec(success, null, 'FaceMe', 'initializeSDK', []);
  });
}
```

## Installation

Copy `faceme-6.14.0.aar` to `cordova-plugin-faceme/src/android/libs`

    cordova plugin add https://github.com/marhano/cordova_plugin_faceme
    

## Properties

- pluginTest
- initializeSDK

## initializeSDK

Initialize the FaceMeSdk with a lincese key. Verify and register the license returns true or false.

Example:

```ts
const success = (result: any) => {
  alert(result);
};
this.platform.ready().then(() => {
  cordova.exec(success, null, 'FaceMe', 'initializeSDK', []);
});
```