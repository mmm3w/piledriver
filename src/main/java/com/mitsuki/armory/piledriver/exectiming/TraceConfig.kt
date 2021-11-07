package com.mitsuki.armory.piledriver.exectiming

import java.io.File
import java.io.FileNotFoundException

class TraceConfig(configPath: String) {

    private val mKeepPackage: HashSet<String> by lazy { hashSetOf() }
    private val mKeepFile: HashSet<String> by lazy { hashSetOf() }
    private val mPickPackage: HashSet<String> by lazy { hashSetOf() }
    private val mPickFile: HashSet<String> by lazy { hashSetOf() }

    private val keepClass by lazy { arrayOf("R.class", "R$", "Manifest", "BuildConfig") }

    init {
        try {
            File(configPath).useLines {
                it.forEach { line ->
                    when {
                        line.startsWith("-pick") -> {
                            line.split(" ").apply {
                                if (size > 1) {
                                    val target = this[1]
                                    if (target.startsWith("@")) {
                                        mPickFile.add(target.replace("@", ""))
                                    } else {
                                        mPickPackage.add(target)
                                    }
                                }
                            }
                        }
                        line.startsWith("-keep") -> {
                            line.split(" ").apply {
                                if (size > 1) {
                                    val target = this[1]
                                    if (target.startsWith("@")) {
                                        mKeepFile.add(target.replace("@", ""))
                                    } else {
                                        mKeepPackage.add(target)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (inner: FileNotFoundException) {
            inner.printStackTrace()
        }
        //定番
        @Suppress("SpellCheckingInspection")
        mKeepPackage.add("com/mitsuki/armory/timetrace")
    }


    fun isKeep(name: String): Boolean {
        if (name.endsWith(".class")) {
            keepClass.forEach {
                if (name.contains(it)) return true
            }
            return false
        }
        return true
    }

    fun classTrace(name: String?): Boolean {
        if (name == null) return false
        //在keep包中直接返回 false
        mKeepPackage.forEach {
            if (name.contains(it)) return false
        }
        //在keep类中直接返回 false
        mKeepFile.forEach {
            if (name == it) return false
        }

        if (mPickPackage.isEmpty() && mPickFile.isEmpty()) {
            //未配置pick时全部插桩位
            return true
        } else {
            //配置pick时按配置来
            mPickPackage.forEach {
                if (name.contains(it)) return true
            }

            mPickFile.forEach {
                if (name == it) return true
            }
            return false
        }
    }
}