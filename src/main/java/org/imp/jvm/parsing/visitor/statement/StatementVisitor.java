package org.imp.jvm.parsing.visitor.statement;

import org.imp.jvm.ImpParser;
import org.imp.jvm.ImpParserBaseVisitor;
import org.imp.jvm.domain.scope.FunctionSignature;
import org.imp.jvm.domain.scope.Identifier;
import org.imp.jvm.domain.scope.LocalVariable;
import org.imp.jvm.domain.scope.Scope;
import org.imp.jvm.domain.statement.IfStatement;
import org.imp.jvm.domain.types.BuiltInType;
import org.imp.jvm.domain.types.Type;
import org.imp.jvm.domain.types.TypeResolver;
import org.imp.jvm.expression.Expression;
import org.imp.jvm.parsing.visitor.expression.ExpressionVisitor;
import org.imp.jvm.statement.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

// http://www.ist.tugraz.at/_attach/Publish/Cb/typechecker_2017.pdf
// https://web.stanford.edu/class/archive/cs/cs143/cs143.1128/lectures/09/Slides09.pdf
public class StatementVisitor extends ImpParserBaseVisitor<Statement> {
    private final ExpressionVisitor expressionVisitor;

    private final Scope scope;

    public StatementVisitor(Scope scope) {
        this.scope = scope;
        expressionVisitor = new ExpressionVisitor(scope);
    }


    @Override
    public Block visitBlock(ImpParser.BlockContext ctx) {
        if (ctx.statementList() != null) {
            List<ImpParser.StatementContext> blockStatementsCtx = ctx.statementList().statement();

            // Child blocks inherit the parent block's scope
            Scope newScope = new Scope(scope);

            StatementVisitor statementVisitor = new StatementVisitor(newScope);
            List<Statement> statements = blockStatementsCtx.stream().map(stmt -> stmt.accept(statementVisitor)).collect(Collectors.toList());
            return new Block(statements, newScope);
        }
        return new Block();
    }

    @Override
    public Function visitFunctionStatement(ImpParser.FunctionStatementContext ctx) {
        // Identifier
        String name = ctx.identifier().getText();

        // Block
        BlockVisitor blockVisitor = new BlockVisitor(scope);
        ImpParser.BlockContext blockContext = ctx.block();
        Block block = Optional.ofNullable(blockContext.accept(blockVisitor)).orElse(new Block());

        // Arguments
        List<Identifier> arguments = new ArrayList<>();
        ImpParser.ArgumentsContext argumentsContext = ctx.arguments();
        if (argumentsContext != null) {
            arguments = argumentsContext.accept(new ArgumentsVisitor());
        }
//        addParametersAsLocalVariables(arguments);
        arguments.forEach(param -> scope.addLocalVariable(new LocalVariable(param.name, param.type)));

        // Return type
        ImpParser.TypeContext typeContext = ctx.type();
        Type returnType = BuiltInType.VOID;
        if (typeContext != null) {
            // ToDo: parse multiple returns
            returnType = TypeResolver.getFromTypeName(typeContext);
        }


        // Todo: scope.addLocalVariable(new LocalVariable("this",scope.getClassType()));
        // Todo: addParametersAsLocalVariables(signature);
        FunctionSignature signature = new FunctionSignature(name, arguments, returnType);
        scope.addSignature(signature);

        return new Function(signature, block);
    }

    @Override
    public Return visitReturnStatement(ImpParser.ReturnStatementContext ctx) {
        Expression expression = ctx.expression().accept(expressionVisitor);
        return new Return(expression);
    }

    @Override
    public Statement visitIfStatement(ImpParser.IfStatementContext ctx) {
        Expression condition = null;
        if (ctx.expression() != null) {
            condition = ctx.expression().accept(expressionVisitor);
        }
        Block block = ctx.block(0).accept(this);
        If elseIf = null;

        return new If(condition, block, elseIf);
    }

    @Override
    public Loop visitLoopStatement(ImpParser.LoopStatementContext ctx) {
        ImpParser.LoopConditionContext conditionContext = ctx.loopCondition();
        ImpParser.BlockContext blockContext = ctx.block();

        Expression condition = null;
        Block block = null;

        if (conditionContext != null) {
            condition = conditionContext.accept(expressionVisitor);
            block = blockContext.accept(this);
        }

        return new Loop(condition, block);
    }

    @Override
    public Statement visitVariableStatement(ImpParser.VariableStatementContext ctx) {
        return variableVisitor.visitVariableStatement(ctx);
    }


    static class ArgumentsVisitor extends ImpParserBaseVisitor<List<Identifier>> {
        @Override
        public List<Identifier> visitArguments(ImpParser.ArgumentsContext ctx) {
            var argumentsCtx = ctx.argument();

            var arguments = new ArrayList<Identifier>();
            for (var argCtx : argumentsCtx) {
                var identifier = new Identifier();
                identifier.name = argCtx.identifier().getText();
                identifier.type = TypeResolver.getFromTypeName(argCtx.type());
                arguments.add(identifier);
            }

            return arguments;
        }
    }


    @Override
    public Statement visitAssignment(ImpParser.AssignmentContext ctx) {
        return assignmentVisitor.visitAssignment(ctx);
    }


    @Override
    public Statement visitClassStatement(ImpParser.ClassStatementContext ctx) {
        return super.visitClassStatement(ctx);
    }

    @Override
    public Statement visitImportStatement(ImpParser.ImportStatementContext ctx) {
        return super.visitImportStatement(ctx);
    }

    @Override
    public Statement visitExportStatement(ImpParser.ExportStatementContext ctx) {
        return super.visitExportStatement(ctx);
    }


}
