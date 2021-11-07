package com.mitsuki.armory.piledriver.exectiming

import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.commons.AdviceAdapter

@Suppress("SpellCheckingInspection")
class MyMethodVisitor(
    api: Int,
    methodVisitor: MethodVisitor?,
    access: Int,
    name: String?,
    descriptor: String?,
    private val methodName: String
) : AdviceAdapter(api, methodVisitor, access, name, descriptor) {

    override fun onMethodEnter() {
        super.onMethodEnter()

        mv.visitFieldInsn(
            GETSTATIC,
            "com/mitsuki/armory/timetrace/TimeTracer",
            "INSTANCE",
            "Lcom/mitsuki/armory/timetrace/TimeTracer;"
        )
        mv.visitLdcInsn(methodName)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "com/mitsuki/armory/timetrace/TimeTracer",
            "start",
            "(Ljava/lang/String;)V",
            false
        )

        println("[ExecTiming] $methodName")
    }

    override fun onMethodExit(opcode: Int) {
        super.onMethodExit(opcode)

        mv.visitFieldInsn(
            GETSTATIC,
            "com/mitsuki/armory/timetrace/TimeTracer",
            "INSTANCE",
            "Lcom/mitsuki/armory/timetrace/TimeTracer;"
        )
        mv.visitLdcInsn(methodName)
        mv.visitMethodInsn(
            INVOKEVIRTUAL,
            "com/mitsuki/armory/timetrace/TimeTracer",
            "end",
            "(Ljava/lang/String;)V",
            false
        )
    }
}