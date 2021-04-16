package org.imp.jvm.parsing.visitor.expression;

import org.imp.jvm.ImpParser;
import org.imp.jvm.ImpParserBaseVisitor;
import org.imp.jvm.domain.expression.IdentifierReference;
import org.imp.jvm.domain.scope.LocalVariable;
import org.imp.jvm.domain.scope.Scope;
import org.imp.jvm.domain.types.BuiltInType;

public class IdentifierReferenceVisitor extends ImpParserBaseVisitor<IdentifierReference> {
    private final Scope scope;

    public IdentifierReferenceVisitor(Scope scope) {
        this.scope = scope;
    }


    @Override
    public IdentifierReference visitIdentifierReferenceExpression(ImpParser.IdentifierReferenceExpressionContext ctx) {
        String name = ctx.getText();
        if (scope.variableExists(name)) {

        }
        // ToDo: reference type parsing
        LocalVariable localVariable = new LocalVariable(name, BuiltInType.INT);
        return new IdentifierReference(localVariable);
    }
}
