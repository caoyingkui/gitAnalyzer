package git.fileDiff.modify.type.signatureChange;

import ch.uzh.ifi.seal.changedistiller.model.classifiers.ChangeType;
import ch.uzh.ifi.seal.changedistiller.model.entities.Delete;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeEntity;
import git.fileDiff.file.ChangedClass;
import git.fileDiff.method.ChangedMethod;
import git.fileDiff.method.MethodDiff;
import git.fileDiff.modify.type.Modify;
import git.fileDiff.modify.type.Order;

/**
 * Created by kvirus on 2019/5/19 20:31
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
@Order(order = Order.OrderValue.SIGNATURE)
public class ReturnTypeDelete extends Modify {
    public static int count = 0;
    public String returnType = "";
    public static ReturnTypeDelete match(MethodDiff method) {
        if (!(method instanceof ChangedMethod)) return null;

        boolean s = false;
        ReturnTypeDelete result = new ReturnTypeDelete();
        ChangedMethod cMethod = (ChangedMethod) method;
        for (SourceCodeChange change : cMethod.getSourceCodeChanges()) {
            if (change.getChangeType() == ChangeType.RETURN_TYPE_DELETE) {
                Delete delete = (Delete)change;
                SourceCodeEntity entity = delete.getChangedEntity();
                result.returnType = ((ChangedClass)method.file).content.OLD.substring(entity.getStartPosition(), entity.getEndPosition() + 1);
                s = true;
                break;
            }
        }
        return s && (++count) > 0 ? result : null;
    }

    @Override
    protected void build() {

    }

    @Override
    public String getContent() {
        return super.getContent();
    }

    @Override
    public void extend(String str) {
        super.extend(str);
    }

    @Override
    public boolean equals(Object obj) {
        return false;
    }
}
