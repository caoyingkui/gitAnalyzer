package git.fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.Move;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.treedifferencing.matching.measure.NGramsCalculator;
import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.group.hash.visitor.RenameVisitor;
import org.eclipse.jdt.core.dom.*;
import git.util.CompileTool;

/**
 * Created by kvirus on 2019/6/16 13:28
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class IIf extends InsertHash{
    public final int ACTION     = 0;
    public final int STATEMENT  = 1;
    public final int PARENT     = 2;
    public final int CONDITION  = 3;
    public final String condition;

    public IIf(SourceCodeChange change) {
        super(change);
        assert (change instanceof Insert || change instanceof Delete || change instanceof Move)&&
                ( change.getChangedEntity().getType() == JavaEntityType.IF_STATEMENT ||
                    change.getChangedEntity().getType() == JavaEntityType.ELSE_STATEMENT) ;

        hashes = new int[4];

        hashes[ACTION]      = typeHash(change);
        hashes[STATEMENT]   = statementHash();
        hashes[PARENT]      = blockStatementHash(change.getParentEntity().getType());
        condition = change.getChangedEntity().getUniqueName();
        hashes[CONDITION]   = getConditionHash(condition);
    }

    private int statementHash() {
        return getCode(ASTNode.IF_STATEMENT);
    }

    private int getConditionHash(String conditionExpression) {
        Expression expression = CompileTool.getExpression(conditionExpression);

        String s = "";
        if (expression instanceof MethodInvocation) {
            s = ((MethodInvocation)expression).getName().toString();
        } else if (expression instanceof SimpleName){
            s = conditionExpression;
        } else {
            RenameVisitor visitor = new RenameVisitor();
            expression.accept(visitor);
            s = expression.toString();
        }
        return s.hashCode();
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
            if (hashes[CONDITION] == hash.hashes[CONDITION]) return true;

            return false;
            /*NGramsCalculator calculator = new NGramsCalculator(2);
            return calculator.calculateSimilarity(condition, ((IIf)hash).condition) >= 0.6;*/
            //return true;
        }
    }
}
