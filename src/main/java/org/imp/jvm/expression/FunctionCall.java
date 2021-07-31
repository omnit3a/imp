package org.imp.jvm.expression;

import org.imp.jvm.compiler.DescriptorFactory;
import org.imp.jvm.compiler.Logger;
import org.imp.jvm.domain.ImpFile;
import org.imp.jvm.domain.scope.Identifier;
import org.imp.jvm.domain.scope.LocalVariable;
import org.imp.jvm.domain.scope.Scope;
import org.imp.jvm.exception.SemanticErrors;
import org.imp.jvm.types.BuiltInType;
import org.imp.jvm.types.FunctionType;
import org.imp.jvm.types.StructType;
import org.imp.jvm.types.Type;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class FunctionCall extends Expression {
    public Function function;
    public final List<Expression> arguments;
    public final ImpFile owner;

    public List<Type> argTypes;
    public String name;


    public FunctionCall(String name, List<Expression> arguments, ImpFile owner) {
        this.name = name;
        this.arguments = arguments;

        this.function = null;
        this.owner = owner;
    }

    @Override
    public void validate(Scope scope) {
        // Find the types of each of the arguments
        for (var arg : arguments) {
            arg.validate(scope);
        }
        argTypes = arguments.stream().map(expression -> expression.type).collect(Collectors.toList());

        if (name.equals("log")) {
            return;
        }

        // Find a FunctionType in the current scope by name
        FunctionType functionType = scope.findFunctionType(this.name);
        if (functionType == null) {
            Logger.syntaxError(SemanticErrors.FunctionNotFound, getCtx());
            return;
        }

        // Find a function that exists in the current scope that matches the FunctionSignature

        function = functionType.getSignatureByTypes(this.argTypes);
        if (function == null) {
            Logger.syntaxError(SemanticErrors.FunctionSignatureMismatch, getCtx());
            return;
        }
//        var lvr = new VariableReference(scope.getLocalVariable("g"));
//        lvr.type = BuiltInType.STRUCT;
//        lvr.localVariable.type = BuiltInType.STRUCT;
//        functionType.closures.add(lvr);
        this.type = function.functionType;
    }

    public void generate(MethodVisitor mv, Scope scope) {
        // generate arguments

        if (name.equals("log")) {
            mv.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out", "Ljava/io/PrintStream;");
            arguments.get(0).generate(mv, scope);

            // get the variation of println to call
            Type argType = arguments.get(0).type;

            if (argType instanceof FunctionType) {
                argType = BuiltInType.STRING;
            }

            String descriptor = "(" + argType.getDescriptor() + ")V";


            if (argType instanceof StructType) {
                descriptor = "(Ljava/lang/Object;)V";
            }


//            descriptor = org.objectweb.asm.Type.getDescriptor(java.io.PrintStream.class);

            String owner = "java/io/PrintStream";
            name = "println"; // name of the method we call

            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, owner, name, descriptor, false);
        } else {
            // 0. If the First Class Function has not been initialized, do so.
            String localVariableName = this.type.getName();
            if (scope.getLocalVariable(localVariableName) == null) {
                // Initialize the first-class function closure object
                String ownerDescriptor = this.type.getInternalName();
                mv.visitTypeInsn(Opcodes.NEW, ownerDescriptor);
                mv.visitInsn(Opcodes.DUP);

                // Call constructor on first-class function closure object
                List<Identifier> params = Collections.emptyList();
                String methodDescriptor = DescriptorFactory.getMethodDescriptor(params, BuiltInType.VOID);
                mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ownerDescriptor, "<init>", methodDescriptor, false);

                // Store as new local variable
                scope.addLocalVariable(new LocalVariable(localVariableName, function.functionType));
                mv.visitVarInsn(Opcodes.ASTORE, scope.getLocalVariableIndex(localVariableName));
            }

            // 1. Load the First Class Function object
            String ownerDescriptor = this.type.getInternalName();
            int index = scope.getLocalVariableIndex(localVariableName);
            mv.visitVarInsn(Opcodes.ALOAD, index);

            // 2. Load the variables that must be passed to the closure
            var s = function.functionType.signatures.getValue(0).block.scope;
            List<Identifier> closureParams = new ArrayList<>();
            for (var p : s.closures.values()) {
                var identifier = new Identifier(p.getName(), BuiltInType.BOX);
                closureParams.add(identifier);
                int i = scope.getLocalVariableIndex(p.getName());
                mv.visitVarInsn(Opcodes.ALOAD, i);
            }

            // 3. Call the closure() method on the First Class Function object
            String methodDescriptor = DescriptorFactory.getMethodDescriptor(closureParams, BuiltInType.VOID);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ownerDescriptor, "closure", methodDescriptor, false);

            // 4. Generate arguments for the function itself
            index = scope.getLocalVariableIndex(localVariableName);
            mv.visitVarInsn(Opcodes.ALOAD, index);
            for (var arg : arguments) {
                arg.generate(mv, scope);
            }

            // 5. Call the appropriate invoke method on the First Class Function object
            List<Identifier> params = arguments.stream().map(arg -> new Identifier(arg.type.getName(), arg.type)).collect(Collectors.toList());
            methodDescriptor = DescriptorFactory.getMethodDescriptor(params, BuiltInType.VOID);
            mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ownerDescriptor, "invoke", methodDescriptor, false);
        }
    }
}
