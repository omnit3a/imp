package org.imp.jvm;

import org.apache.commons.io.FilenameUtils;
import org.imp.jvm.domain.SourceFile;
import org.imp.jvm.tool.Compiler;
import org.imp.jvm.types.StructType;
import org.imp.jvm.visitors.CodegenVisitor;
import org.javatuples.Pair;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.GeneratorAdapter;
import org.objectweb.asm.commons.Method;

import java.util.Map;
import java.util.stream.Collectors;

public class BytecodeGenerator {

    public static void addToString(ClassWriter cw, StructType st, String constructorDescriptor, String ownerInternalName) {
        var _mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;", null, null);
        var ga = new GeneratorAdapter(_mv, Opcodes.ACC_PUBLIC, "toString", "()Ljava/lang/String;");

        if (st.parameters.size() > 0) {

            String owner = "java/lang/invoke/StringConcatFactory";
            String name = "makeConcatWithConstants";
            String descriptor = "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/invoke/CallSite;";

            var handle = new Handle(Opcodes.H_INVOKESTATIC, owner, name, descriptor, false);

            for (var pair : st.parameters) {
                var pName = pair.getValue0();
                var pType = pair.getValue1();
                ga.loadThis();
                ga.getField(Type.getType("L" + ownerInternalName + ";"), pName, Type.getType(pType.getDescriptor()));
            }

            String recipe = st.name + "[" + st.parameters.stream().map(p -> p.getValue0() + "=\u0001").collect(Collectors.joining(", ")) + "]";

            ga.invokeDynamic(
                    name,
                    "(" + constructorDescriptor + ")Ljava/lang/String;",
                    handle,
                    recipe
            );

        } else {
            ga.push(st.name + "[]");
        }
        ga.visitInsn(Opcodes.ARETURN);
        ga.endMethod();

    }

    public Pair<ClassWriter, Map<StructType, ClassWriter>> generate(Compiler compiler, SourceFile source) {
        var cw = new ClassWriter(CodegenVisitor.flags);

        String qualifiedName = FilenameUtils.removeExtension(source.getFullRelativePath());
        cw.visit(CodegenVisitor.CLASS_VERSION, Opcodes.ACC_PUBLIC + Opcodes.ACC_SUPER, qualifiedName, null, "java/lang/Object", null);

        // Add constructor
        var initMv = cw.visitMethod(Opcodes.ACC_PUBLIC, Constants.Init, "()V", null, null);
        var ga = new GeneratorAdapter(initMv, Opcodes.ACC_PUBLIC, Constants.Init, "()V");
        ga.visitVarInsn(Opcodes.ALOAD, 0);
        ga.visitMethodInsn(
                Opcodes.INVOKESPECIAL,
                "java/lang/Object",
                Constants.Init,
                "()V",
                false
        );
        ga.returnValue();
        ga.endMethod();

        // Add instance field
        cw.visitField(Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, "instance", "L" + qualifiedName + ";", null, null);

        var mvStatic = cw.visitMethod(Opcodes.ACC_STATIC, Constants.Clinit, "()V", null, null);
        ga = new GeneratorAdapter(mvStatic, Opcodes.ACC_PUBLIC + Opcodes.ACC_STATIC, Constants.Clinit, "()V");
        var t = Type.getType("L" + qualifiedName + ";");
        ga.newInstance(t);
        ga.dup();
        ga.invokeConstructor(t, new Method(Constants.Init, "()V"));

        mvStatic.visitFieldInsn(Opcodes.PUTSTATIC, qualifiedName, "instance", "L" + qualifiedName + ";");
        mvStatic.visitInsn(Opcodes.RETURN);
        ga.endMethod();

        // Generate bytecode for each Type defined in the Imp file
        var codegenVisitor = new CodegenVisitor(compiler, source.rootEnvironment, source, cw);

        source.acceptVisitor(codegenVisitor);

        cw.visitEnd();

        return new Pair<>(cw, codegenVisitor.structWriters);
    }

}
