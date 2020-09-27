package git.fileDiff.group.hash.visitor;

import git.fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/6/16 10:39
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class MethodVisitor extends ASTVisitor {
    public String methodName = "";

    public String qualifiedName = "";

    public int argumentCount = 0;


    @Override
    public boolean visit(ConstructorInvocation node) {
        methodName      = "this";
        qualifiedName   = "";
        argumentCount   = node.arguments().size();
        return false;
    }

    public boolean visit(SuperConstructorInvocation node) {
        methodName      = "super";
        qualifiedName   = "";
        argumentCount   = node.arguments().size();
        return false;
    }

    @Override
    public boolean visit(MethodInvocation node) {
        methodName = node.getName().toString();
        Expression e = node.getExpression();
        qualifiedName = e == null ? "" : e.toString();
        argumentCount   = node.arguments().size();

        return false;
    }

    public static void main(String[] args) {
        String c = "a.b.fun(1,2);";
        StatementHash.parser.setSource(c.toCharArray());
        Block block = (Block)StatementHash.parser.createAST(null);

        MethodVisitor v = new MethodVisitor();
        block.accept(v);

    }
}
