package git.fileDiff.group.hash.visitor;

import git.fileDiff.group.hash.StatementHash;
import org.eclipse.jdt.core.dom.*;

/**
 * Created by kvirus on 2019/6/16 9:06
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class DeclarationVisitor extends ASTVisitor {
    public String dataType = "";
    public String variableName = "";

    public int init = 0;
    public String initExpression = "";

    @Override
    public boolean visit(VariableDeclarationStatement node) {
        dataType = node.getType().toString();

        VariableDeclarationFragment f = (VariableDeclarationFragment)(node.fragments().get(0));

        variableName = f.getName().toString();
        Expression expression = f.getInitializer();


        if (expression == null) {
            initExpression = "";
            init = 0;
        } else {
            if(expression instanceof MethodInvocation) {
                initExpression = ((MethodInvocation) expression).getName().toString();
            } else if (expression instanceof CastExpression) {
                Expression e = ((CastExpression) expression).getExpression();
                if (e instanceof MethodInvocation)
                    initExpression = ((MethodInvocation) e).getName().toString();
                else
                    initExpression = e.toString();

            } else {
                initExpression = expression.toString();
            }

            init = initExpression.hashCode();

        }

        return true;
    }
}
