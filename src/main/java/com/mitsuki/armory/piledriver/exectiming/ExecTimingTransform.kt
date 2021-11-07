package com.mitsuki.armory.piledriver.exectiming

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project
import org.objectweb.asm.ClassReader
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.Opcodes
import java.io.File
import java.io.FileOutputStream
import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream
import java.util.zip.ZipEntry

class ExecTimingTransform(target: Project) : Transform() {

    private val extensions: ExecTimingExtensions =
        target.extensions.getByType(ExecTimingExtensions::class.java)

    override fun getName(): String = "ExecTimingTransform"

    override fun getInputTypes(): MutableSet<QualifiedContent.ContentType> =
        TransformManager.CONTENT_CLASS

    override fun getScopes(): MutableSet<in QualifiedContent.Scope> =
        TransformManager.SCOPE_FULL_PROJECT

    override fun isIncremental(): Boolean = false

    override fun transform(transformInvocation: TransformInvocation?) {
        println("[ExecTiming] isEnable:${extensions.enable}")
        val traceConfig = TraceConfig(extensions.traceRules)
        transformInvocation?.apply {

            outputProvider.deleteAll()
            inputs.forEach { transformInput ->
                traverseDirectoryInputs(
                    transformInput.directoryInputs,
                    outputProvider,
                    traceConfig
                )
                traverseJarInputs(transformInput.jarInputs, outputProvider, traceConfig)
            }

        }
    }

    private fun traverseDirectoryInputs(
        inputs: Collection<DirectoryInput>,
        outputProvider: TransformOutputProvider,
        config: TraceConfig
    ) {
        inputs.forEach { directoryInput ->
            if (extensions.enable) {
                directoryInput.file.walk().filter { it.isFile }
                    .forEach { targetFile ->
                        val name = targetFile.name
                        if (!config.isKeep(name)) {
                            val classReader = ClassReader(targetFile.readBytes())
                            val classWriter = ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                            val cv = MyClassVisitor(Opcodes.ASM6, classWriter, config)
                            classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                            File(targetFile.parentFile, name)
                                .outputStream()
                                .use {
                                    it.write(classWriter.toByteArray())
                                }
                        }
                    }
            }

            outputProvider.getContentLocation(
                directoryInput.name, directoryInput.contentTypes, directoryInput.scopes,
                Format.DIRECTORY
            ).also {
                FileUtils.copyDirectory(directoryInput.file, it)
            }
        }
    }

    private fun traverseJarInputs(
        inputs: Collection<JarInput>,
        outputProvider: TransformOutputProvider,
        config: TraceConfig
    ) {
        inputs.forEach { jarInput ->

            var jarName = jarInput.name

            val md5Name = DigestUtils.md5Hex(jarInput.file.absolutePath)
            if (jarName.endsWith(".jar")) {
                jarName = jarName.substring(0, jarName.length - 4)
            }

            val dest = outputProvider.getContentLocation(
                jarName + md5Name,
                jarInput.contentTypes,
                jarInput.scopes,
                Format.JAR
            )

            if (extensions.enable) {
                val tmpFile = File(jarInput.file.parent, "classes_temp.jar")
                if (tmpFile.exists()) {
                    tmpFile.delete()
                }

                if (name.contains("com/facebook/react")) {
                    println("[temp] $name")
                }

                JarFile(jarInput.file).use { jarFile ->
                    val enumeration = jarFile.entries()

                    JarOutputStream(FileOutputStream(tmpFile)).use { jarOutputStream ->
                        while (enumeration.hasMoreElements()) {
                            enumeration.nextElement().let { jarEntry ->
                                val jarEntryName = jarEntry.name
                                if (jarEntryName.contains("com/facebook/react")) {
                                    println("[temp2] $jarEntryName")
                                }
                                jarOutputStream.putNextEntry(ZipEntry(jarEntryName))
                                jarFile.getInputStream(jarEntry).use { inputStream ->

                                    if (!config.isKeep(jarEntryName)) {
                                        val classReader =
                                            ClassReader(IOUtils.toByteArray(inputStream))
                                        val classWriter =
                                            ClassWriter(classReader, ClassWriter.COMPUTE_MAXS)
                                        val cv = MyClassVisitor(Opcodes.ASM6, classWriter, config)
                                        classReader.accept(cv, ClassReader.EXPAND_FRAMES)
                                        jarOutputStream.write(classWriter.toByteArray())
                                    } else {


                                        jarOutputStream.write(IOUtils.toByteArray(inputStream))
                                    }
                                }
                            }
                            jarOutputStream.closeEntry()
                        }
                    }
                }

                FileUtils.copyFile(tmpFile, dest)

                tmpFile.delete()
            } else {
                FileUtils.copyFile(jarInput.file, dest)
            }
        }
    }
}