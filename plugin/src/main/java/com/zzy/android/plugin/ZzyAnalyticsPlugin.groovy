/*
 * Created by wangzhuozhou on 2015/08/12.
 * Copyright 2015－2022 Zzy Data Inc.
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

import com.android.build.gradle.AppExtension
import com.android.build.gradle.BaseExtension
import com.android.build.gradle.LibraryExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.internal.reflect.Instantiator
import org.gradle.invocation.DefaultGradle

class ZzyAnalyticsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        Instantiator ins = ((DefaultGradle) project.getGradle()).getServices().get(Instantiator)
        def args = [ins] as Object[]
        ZzyAnalyticsConfigExtension extension = project.extensions.create("proxyConfig", ZzyAnalyticsConfigExtension, args)
        Map<String, ?> properties = project.getProperties()
        boolean disableZzyAnalyticsMultiThreadBuild = Boolean.parseBoolean(properties.getOrDefault("zzyAnalytics.disableMultiThreadBuild", "false"))
        boolean disableZzyAnalyticsIncrementalBuild = Boolean.parseBoolean(properties.getOrDefault("zzyAnalytics.disableIncrementalBuild", "false"))
        boolean disableZzyAnalyticsPlugin = Boolean.parseBoolean(properties.getOrDefault("zzyAnalytics.disablePlugin", "false"))
        if (!disableZzyAnalyticsPlugin) {
            BaseExtension baseExtension
            if (project.getPlugins().hasPlugin("com.android.application")) {
                baseExtension = project.extensions.findByType(AppExtension.class)
            } else if (project.getPlugins().hasPlugin("com.android.library")) {
                baseExtension = project.extensions.findByType(LibraryExtension.class)
            }
            if (null != baseExtension) {
                ZzyAnalyticsTransformHelper transformHelper = new ZzyAnalyticsTransformHelper(extension, baseExtension)
                transformHelper.disableZzyAnalyticsIncremental = disableZzyAnalyticsIncrementalBuild
                transformHelper.disableZzyAnalyticsMultiThread = disableZzyAnalyticsMultiThreadBuild

                baseExtension.registerTransform(new ZzyAnalyticsTransform(transformHelper, baseExtension instanceof LibraryExtension))
            } else {
                Logger.error("------------java-aop plugin 当前不支持您的项目--------------")
            }
        } else {
            Logger.error("------------您已关闭了java-aop插件--------------")
        }
    }
}