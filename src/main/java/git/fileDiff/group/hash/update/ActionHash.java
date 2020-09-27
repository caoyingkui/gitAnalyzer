package git.fileDiff.group.hash.update;

import git.fileDiff.group.hash.StatementHash;
import gumtreediff.actions.model.*;
import gumtreediff.tree.ITree;
import gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import git.util.CompileTool;

import java.io.Serializable;

/**
 * Created by kvirus on 2019/6/5 20:48
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class ActionHash implements Serializable {
    public static final int LOOSE = 1;
    public static final int MEDIUM = 2;
    public static final int STRICT = 3;

    final Action action;

    final int typeHash;
    final int rootHash;
    final int nodeHash;
    final int posHash;
    final int valueHash;
    final int parentHash;
    final int parentValueHash;

    final String value;
    final String parentValue;

    public ActionHash(Action action, ITree root) {
        this.action     = action;
        this.typeHash   = getTypeHash(action);
        this.rootHash   = root == null ? 0 : getASTTypeHash(root.getType());
        this.nodeHash   = getASTTypeHash(action.getNode().getType());
        this.posHash    = getPosHash(root, action);
        this.value      = getValue(action);
        this.valueHash  = value.hashCode();

        Tree parent    = (Tree)action.getNode().getParent();
        this.parentHash = getASTTypeHash(parent.getType());
        if (parentHash == StatementHash.getCode(ASTNode.METHOD_INVOCATION)) {
            Expression e = CompileTool.getExpression(parent.content);
            if (e instanceof MethodInvocation) {
                parentValue = ((MethodInvocation) e).getName().toString();
            } else {
                parentValue = "";
            }
            parentValueHash = parentValue.hashCode();
        } else {
            parentValue = "";
            parentValueHash = 0;
        }

    }

    public boolean isSimilar(ActionHash hash, int level) {
        if (level == STRICT) {
            if (typeHash == hash.typeHash &&
                    nodeHash == hash.nodeHash &&
                    valueHash == hash.valueHash ) return true;

            // 13150 是methodInvocation
            if (parentHash == 13150) {
                return typeHash == hash.typeHash &&
                        nodeHash == hash.nodeHash &&
                        posHash == hash.posHash &&
                        parentValueHash == hash.parentValueHash;
            }

            return false;

        } else if (level == MEDIUM) {
            return typeHash == hash.typeHash &&
                    rootHash == hash.rootHash &&
                    nodeHash == hash.nodeHash &&
                    posHash == hash.posHash;
        } else if (level == LOOSE) {
            return typeHash == hash.typeHash &&
                    Math.abs(rootHash - hash.rootHash) < 1000 &&
                    valueHash == hash.valueHash;
        }

        return false;
    }

    public static int getTypeHash(Action action) {
        if (action instanceof Insert) return 2;
        else if (action instanceof Delete) return 4;
        else if (action instanceof Update) return 8;
        else if (action instanceof Move) return 6;
        else return 0;
    }

    public static int getASTTypeHash(int type) {
        return StatementHash.getCode(type);
    }

    public static int getPosHash(ITree node, Action action) {
        if (node == null) return 0;
        return action.getNode().getDepth() - node.getDepth();
    }

    public static String getValue(Action action) {
        if (action instanceof Insert) return ((Tree) action.getNode()).content;
        else if (action instanceof Delete) return ((Tree) action.getNode()).content;
        else if (action instanceof Move) return ((Tree) action.getNode()).content;
        else {
            Update update = (Update)action;
            String value = update.getNode().getLabel() + "->" + update.getValue();
            return value;
        }
    }

}
