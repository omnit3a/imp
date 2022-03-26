package org.imp.jvm.types;

import org.imp.jvm.Util;
import org.imp.jvm.domain.Identifier;
import org.imp.jvm.domain.Modifier;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.commons.GeneratorAdapter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FuncType implements ImpType, Serializable {
    public final Modifier modifier;
    public final List<Identifier> parameters;
    public final int localOffset = 0;
    //    public final String[] fieldNames;
//    public final Type[] fieldTypes;
    public String name;
    public ImpType returnType = BuiltInType.VOID;
    public MethodVisitor mv = null; // careful!
    public GeneratorAdapter ga = null;
    public boolean glue = false;


    public boolean hasReturn2 = false;

    public Map<String, Integer> localMap = new HashMap<>();
    public Map<String, Integer> argMap = new HashMap<>();

    public boolean isPrefixed = false;
    public String owner;

    public FuncType(String name, Modifier modifier, List<Identifier> parameters) {
        this.name = name;
        this.modifier = modifier;
        this.parameters = parameters;

    }

    public int addLocal(String name, ImpType type) {
        return 0;
    }

    @Override
    public int getAddOpcode() {
        return 0;
    }

    @Override
    public Object getDefaultValue() {
        return null;
    }

    @Override
    public String getDescriptor() {
        return null;
    }

    @Override
    public int getDivideOpcode() {
        return 0;
    }

    @Override
    public String getInternalName() {
        return null;
    }

    @Override
    public int getLoadVariableOpcode() {
        return 0;
    }

    @Override
    public int getMultiplyOpcode() {
        return 0;
    }

    @Override
    public String getName() {
        return null;
    }

    @Override
    public int getNegOpcode() {
        return 0;
    }


    @Override
    public int getReturnOpcode() {
        return 0;
    }

    @Override
    public int getStoreVariableOpcode() {
        return 0;
    }

    @Override
    public int getSubtractOpcode() {
        return 0;
    }

    @Override
    public Class<?> getTypeClass() {
        return null;
    }

    @Override
    public boolean isNumeric() {
        return false;
    }

    @Override
    public String kind() {
        return "function";
    }

    @Override
    public String toString() {
        return "func " + name + "(" + Util.parameterString(parameters) + ") " + returnType;
    }
}
