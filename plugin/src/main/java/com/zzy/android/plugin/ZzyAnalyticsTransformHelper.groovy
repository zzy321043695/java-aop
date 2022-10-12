/*
 * Created by renqingyou on 2018/12/01.
 * Copyright 2015－2022 Sensors Data Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.zzy.android.plugin

import com.android.build.gradle.BaseExtension

class ZzyAnalyticsTransformHelper {
    ZzyAnalyticsConfigExtension extension
    BaseExtension android
    public RN_STATE rnState = RN_STATE.NOT_FOUND
    public String rnVersion = ""
    boolean disableSensorsAnalyticsMultiThread
    boolean disableSensorsAnalyticsIncremental
    public boolean isHookOnMethodEnter

    URLClassLoader urlClassLoader

    ZzyAnalyticsTransformHelper(ZzyAnalyticsConfigExtension extension, BaseExtension android) {
        this.extension = extension
        this.android = android
    }

    File androidJar() throws FileNotFoundException {
        String path = getSdkJarDir()
        File jar = new File(path, "android.jar")
        if (!jar.exists()) {
            throw new FileNotFoundException("Android jar not found!\r\n 请确定路径 " + path + " 下是否存在 android.jar 文件")
        }
        return jar
    }

    private String getSdkJarDir() {
        String compileSdkVersion = android.getCompileSdkVersion()
        return String.join(File.separator, android.getSdkDirectory().getAbsolutePath(), "platforms", compileSdkVersion)
    }

    void onTransform() {
        println("sensorsAnalytics {\n" + extension + "\n}")
        ArrayList<String> excludePackages = extension.exclude
        if (excludePackages != null) {
            exclude.addAll(excludePackages)
        }
        ArrayList<String> includePackages = extension.include
        if (includePackages != null) {
            include.addAll(includePackages)
        }
        SAConfigHookHelper.initSDKConfigCells(extension.sdk)
    }

    ClassNameAnalytics analytics(String className) {
        ClassNameAnalytics classNameAnalytics = new ClassNameAnalytics(className)
        if (classNameAnalytics.isSDKFile()) {
            SAConfigHookHelper.initConfigCellInClass(className)
            if (SAConfigHookHelper.sClassInConfigCells.size() > 0 || classNameAnalytics.isSensorsDataAPI
                    || (classNameAnalytics.isAppWebViewInterface && (extension.addUCJavaScriptInterface || extension.addXWalkJavaScriptInterface))
                    || classNameAnalytics.isKeyboardViewUtil || classNameAnalytics.isSensorsDataVersion) {
                classNameAnalytics.isShouldModify = true
            }
        } else if (!classNameAnalytics.isAndroidGenerated()) {
            for (pkgName in special) {
                if (className.startsWith(pkgName)) {
                    classNameAnalytics.isShouldModify = true
                    return classNameAnalytics
                }
            }
            if (extension.useInclude) {
                for (pkgName in include) {
                    if (className.startsWith(pkgName)) {
                        classNameAnalytics.isShouldModify = true
                        break
                    }
                }
            } else {
                classNameAnalytics.isShouldModify = true
                if (!classNameAnalytics.isLeanback()) {
                    for (pkgName in exclude) {
                        if (className.startsWith(pkgName)) {
                            classNameAnalytics.isShouldModify = false
                            break
                        }
                    }
                    if (classNameAnalytics.isShouldModify && extension.disableTrackKeyboard) {
                        for (String ignore : ignoreClass) {
                            if (className.toLowerCase().contains(ignore)) {
                                classNameAnalytics.isShouldModify = false
                                break
                            }
                        }
                    }
                }
            }
        }
        return classNameAnalytics
    }

    enum RN_STATE {
        NOT_FOUND, NO_VERSION, HAS_VERSION
    }
}

