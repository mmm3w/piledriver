package com.mitsuki.armory.piledriver.exectiming

import org.gradle.api.Plugin
import org.gradle.api.Project
import com.android.build.gradle.AppExtension


class ExecTimingPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.extensions.create("execTiming", ExecTimingExtensions::class.java)
        val appExtension: AppExtension = target.extensions.getByType(AppExtension::class.java)
        println("[ExecTiming] apply plugin.")
        appExtension.registerTransform(ExecTimingTransform(target))
    }
}

