package com.mitsuki.armory.piledriver.exectiming

import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

class MyClassVisitor(api: Int, classVisitor: ClassVisitor, private val config: TraceConfig) :
    ClassVisitor(api, classVisitor) {

    private var traceFlag = true
    private var className: String? = null

    override fun visit(
        version: Int,
        access: Int,
        name: String?,
        signature: String?,
        superName: String?,
        interfaces: Array<out String>?
    ) {
        super.visit(version, access, name, signature, superName, interfaces)
        this.className = name

        if (access and Opcodes.ACC_INTERFACE > 0) {
            traceFlag = false
        }

        if (!config.classTrace(name?.replace(".", "/"))) {
            traceFlag = false
        }
    }

    override fun visitMethod(
        access: Int,
        name: String?,
        descriptor: String?,
        signature: String?,
        exceptions: Array<out String>?
    ): MethodVisitor {

        val isConstructor = name?.let {
            it.contains("<clinit>") || it.contains("<init>")
        } ?: false

        val isAbs = access and Opcodes.ACC_ABSTRACT > 0

        if (traceFlag && !isConstructor && !isAbs) {
            val longName = if (access and Opcodes.ACC_NATIVE != 0 || descriptor == null) {
                "${className?.replace("/", ".")}.$name"
            } else {
                "${className?.replace("/", ".")}.$name.${descriptor.replace("/", ".")}"
            }

            return MyMethodVisitor(
                api,
                cv.visitMethod(access, name, descriptor, signature, exceptions),
                access,
                name,
                descriptor,
                longName
            )
        }
        return super.visitMethod(access, name, descriptor, signature, exceptions)
    }
}