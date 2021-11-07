package org.imp.jvm.parser;

import org.imp.jvm.Expr;
import org.imp.jvm.Stmt;

import java.util.List;
import java.util.stream.Collectors;

public class ASTPrinter implements Expr.Visitor<String>, Stmt.Visitor<String> {
    String print(Expr expr) {
        return expr.accept(this);
    }

    String print(Stmt stmt) {
        return stmt.accept(this);
    }

    String print(List<Stmt> stmts) {
        StringBuilder sb = new StringBuilder();
        for (var stmt : stmts) {
            sb.append(stmt.accept(this)).append("\n");
        }
        return sb.toString();
    }

    private String parenthesize(String name, Expr... exprs) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Expr expr : exprs) {
            builder.append(" ");
            builder.append(expr.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, Stmt... stmts) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Stmt stmt : stmts) {
            builder.append(" ");
            builder.append(stmt.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }

    private String parenthesize(String name, List<Stmt> stmts) {
        StringBuilder builder = new StringBuilder();

        builder.append("(").append(name);
        for (Stmt stmt : stmts) {
            builder.append(" ");
            builder.append(stmt.accept(this));
        }
        builder.append(")");

        return builder.toString();
    }


    @Override
    public String visitAssignExpr(Expr.Assign expr) {
        return parenthesize("=", expr.left(), expr.right());
    }

    @Override
    public String visitBinaryExpr(Expr.Binary expr) {
        return parenthesize(expr.operator().representation(), expr.left(), expr.right());
    }

    @Override
    public String visitGroupingExpr(Expr.Grouping expr) {
        return parenthesize("group", expr.expr());
    }

    @Override
    public String visitIdentifierExpr(Expr.Identifier expr) {
        return null;
    }

    @Override
    public String visitLogicalExpr(Expr.Logical expr) {
        return null;
    }

    @Override
    public String visitLiteralExpr(Expr.Literal expr) {
        return expr.literal().source();
    }

    @Override
    public String visitUnaryExpr(Expr.Unary expr) {
        return null;
    }

    @Override
    public String visitPostfixExpr(Expr.Postfix expr) {
        return null;
    }

    @Override
    public String visitBlockStmt(Stmt.Block stmt) {
        return parenthesize("block", stmt.statements());
    }

    @Override
    public String visitExport(Stmt.Export stmt) {
        return parenthesize("export", stmt.stmt());
    }

    @Override
    public String visitEnum(Stmt.Enum stmt) {
        StringBuilder result = new StringBuilder("(enum " + stmt.name().source() + " (");


        result.append(stmt.values().stream().map(val -> val.source()).collect(Collectors.joining(", ")));

        result.append("))");
        return result.toString();
    }

    @Override
    public String visitStruct(Stmt.Struct stmt) {
        StringBuilder result = new StringBuilder("(struct " + stmt.name().source() + " (");


        result.append(stmt.fields().stream().map(this::print).collect(Collectors.joining(", ")));

        result.append("))");
        return result.toString();
    }

    @Override
    public String visitExpressionStmt(Stmt.Expression stmt) {
        return null;
    }

    @Override
    public String visitFunctionStmt(Stmt.Function stmt) {
        StringBuilder result = new StringBuilder("(func " + stmt.name().source());


        result.append(" (");

        result.append(stmt.parameters().stream().map(this::print).collect(Collectors.joining(", ")));

        if (stmt.returnType() != null) result.append(") ").append(stmt.returnType().source());
        result.append(")");
        return result.toString();
    }

    @Override
    public String visitIf(Stmt.If stmt) {
        StringBuilder sb = new StringBuilder();
        if (stmt.falseStmt() == null) {
            sb.append("(if ").append(print(stmt.condition())).append(print(stmt.trueStmt()));
        } else {
            sb.append("(if-else ").append(print(stmt.condition())).append(print(stmt.trueStmt())).append(print(stmt.falseStmt()));
        }
        return sb.toString();
    }

    @Override
    public String visitReturnStmt(Stmt.Return stmt) {
        return null;
    }

    @Override
    public String visitVariable(Stmt.Variable stmt) {
        return parenthesize(stmt.mutability().source(), stmt.expr());
    }


    @Override
    public String visitParameterStmt(Stmt.Parameter stmt) {
        return stmt.name().source() + " " + stmt.type().source();
    }

    @Override
    public String visitForInLoop(Stmt.ForInLoop stmt) {
        return null;
    }

    @Override
    public String visitForLoop(Stmt.ForLoop stmt) {
        return null;
    }


    @Override
    public String visitTypeAlias(Stmt.TypeAlias stmt) {
        return null;
    }
}
