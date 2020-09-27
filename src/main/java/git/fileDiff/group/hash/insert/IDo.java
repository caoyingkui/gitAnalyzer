package git.fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.group.hash.visitor.RenameVisitor;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Expression;
import git.util.CompileTool;

/**
 * Created by kvirus on 2019/7/24 23:50
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IDo extends InsertHash{
    public final int ACTION     = 0;
    public final int STATEMENT  = 1;
    public final int PARENT     = 2;
    public final int CONDITION  = 3;

    public IDo(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move)&&
                change.getChangedEntity().getType() == JavaEntityType.DO_STATEMENT ;

        hashes = new int[4];

        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());
        hashes[CONDITION]   = getConditionHash(change.getChangedEntity().getUniqueName());
    }

    private int statementHash() {
        return getCode(ASTNode.DO_STATEMENT);
    }

    private int getConditionHash(String conditionExpression) {
        Expression expression = CompileTool.getExpression(conditionExpression);
        RenameVisitor visitor = new RenameVisitor();
        expression.accept(visitor);
        return expression.toString().hashCode();
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (!super.equals(hash)) return false;

        if (strict) {
            int len = hashes.length;
            for (int i = 2; i < len; i ++)
                if (hashes[i] != hash.hashes[i]) return false;
            return true;
        } else {
            return hashes[CONDITION] == hash.hashes[CONDITION];
            //return true;
        }
    }
}
