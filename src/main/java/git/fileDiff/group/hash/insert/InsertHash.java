package git.fileDiff.group.hash.insert;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.EntityType;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.Insert;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.group.hash.visitor.MethodVisitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;

/**
 * Created by kvirus on 2019/6/15 22:05
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public abstract class InsertHash extends StatementHash {
    public InsertHash(SourceCodeChange change) {
        content = change.getChangedEntity().getUniqueName();
    }

    public static InsertHash getHash(SourceCodeChange change) {
        EntityType type = change.getChangedEntity().getType();
        InsertHash result = null;
        if (type == JavaEntityType.ASSIGNMENT)                           result = new IAssignment(change);
        else if (type == JavaEntityType.CATCH_CLAUSE)                    result = new ICatch(change);
        else if (type == JavaEntityType.VARIABLE_DECLARATION_STATEMENT)  result = new IDeclaration(change);
        else if (type == JavaEntityType.IF_STATEMENT)                    result = new IIf(change);
        else if (type == JavaEntityType.METHOD_INVOCATION)               result = new IMethodInvocation(change);
        else if (type == JavaEntityType.RETURN_STATEMENT)                result = new IReturn(change,null, null);
        else if (type == JavaEntityType.TRY_STATEMENT)                   result = new ITry(change);
        else result = new IDefault(change);

        if (change instanceof Delete) result.hashes[0] = StatementHash.DELETE;
        return result;
    }

    public String toString() {
        return content;
    }
}
