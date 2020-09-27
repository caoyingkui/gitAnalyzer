package git.fileDiff.group.hash.update;

import git.analyzer.commit.Re;
import ch.uzh.ifi.seal.changedistiller.model.classifiers.java.JavaEntityType;
import ch.uzh.ifi.seal.changedistiller.model.entities.SourceCodeChange;
import ch.uzh.ifi.seal.changedistiller.model.entities.Update;
import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.group.hash.visitor.ForeachVisitor;
import git.fileDiff.group.hash.visitor.RenameVisitor;
import git.gumtree.GumTree;
import gumtreediff.actions.model.Action;
import gumtreediff.tree.ITree;
import gumtreediff.tree.Tree;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Expression;
import git.util.CompileTool;


import java.util.*;

/**
 * Created by kvirus on 2019/6/5 20:41
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class UUpdate extends UpdateHash{
    public static double threshold = 0.9;

    List<ActionHash> actionHashes = new ArrayList<>();

    public UUpdate(SourceCodeChange change) {
        super(change);
        hashes = new int[4];
        hashes[0] = typeHash(change);
        hashes[1] = StatementHash.statementHash(change.getChangedEntity().getType());
        if (change instanceof Update) {
            Update update = (Update) change;
            updateToHash(update);
            hashes[2] = update.getChangedEntity().getUniqueName().toString().replaceAll("\\s", "").hashCode();
            hashes[3] = update.getNewEntity().getUniqueName().toString().replaceAll("\\s", "").hashCode();
        }
    }

    private ITree getCommonRoot(List<Action> actions) {
        if (actions.size() == 1) return actions.get(0).getNode().getParent();
        List<List<ITree>> paths = new ArrayList<>();
        int len = 0;
        for(Action action: actions) {
            List<ITree> path = new ArrayList<>();
            ITree node = action.getNode();
            while (node != null) {
                if (node instanceof Tree) {
                    path.add(0, node);
                }
                node = node.getParent();
            }
            len = (len == 0 ? path.size() : Math.min(len, path.size()));
            paths.add(path);
        }

        ITree targetNode = null;
        try {
            outLoop:
            for (int i = 0; i < len; i++) {
                for (int j = 1; j < paths.size(); j++) {
                    if (paths.get(j).get(i).getType() != paths.get(j - 1).get(i).getType()) break outLoop;
                }
                targetNode = paths.get(0).get(i);
            }
        } catch (Exception e) {
            int a = 2;
        }
        return targetNode;
    }

    private void updateToHash(Update update) {
        String newContent = update.getNewEntity().getUniqueName();
        String oldContent = update.getChangedEntity().getUniqueName();

        // foreach 中获取的content为整个括号里面的内容，因此没有办法直接编译
        if (update.getChangedEntity().getType() == JavaEntityType.FOREACH_STATEMENT) {
            newContent = "for (" + newContent + ") {}";
            oldContent = "for (" + oldContent + ") {}";
        }

        //RenameVisitor visitor = new RenameVisitor();
        //newContent = RenameVisitor.rename(newContent, visitor);
        //oldContent = RenameVisitor.rename(oldContent, visitor);


        List<Action>  actions = null;

        actions = GumTree.getActions(newContent, oldContent, ASTParser.K_STATEMENTS);
        if (actions.size() == 0) actions = GumTree.getActions(newContent, oldContent, ASTParser.K_EXPRESSION);
        if (actions.size() == 0) actions = GumTree.getActions(newContent, oldContent, ASTParser.K_CLASS_BODY_DECLARATIONS);

        Set<Integer> ids = new HashSet<>();
        for (Action a: actions) ids.add(a.getNode().getId());
        List<Action> subActions = new ArrayList<>();

        for (Action action: actions) {
            int pid = action.getNode().getParent().getId();
            if (ids.contains(pid)) continue;
            subActions.add(action);
        }
        ITree root = getCommonRoot(subActions);
        if (root == null) {
            int a = 2;
        }
        root = getCommonRoot(subActions);
        for (Action action: actions) {
            actionHashes.add(new ActionHash(action,root));
        }
    }

    @Override
    public boolean equals(StatementHash hash) {
        if (! (hash instanceof UUpdate)) return false;

        if (hashes[2] == hash.hashes[2] || hashes[3] == hash.hashes[3]) return true;

        UUpdate sHash = (UUpdate) hash;

        Set<ActionHash> set1 = new HashSet<>();
        Set<ActionHash> set2 = new HashSet<>();
        for (ActionHash a1: actionHashes) {
            for (ActionHash a2: sHash.actionHashes) {
                if (a1.isSimilar(a2, 3)) {
                    set1.add(a1);
                    set2.add(a2);
                }
            }
        }

        if(actionHashes.size() == 0 || sHash.actionHashes.size() == 0) return false;
        //boolean condition1 = set1.size() >= actionHashes.size() * 0.9 && set2.size() >= sHash.actionHashes.size() * 0.9;
        boolean condition1 = set1.size() >= actionHashes.size() * threshold && set2.size() >= sHash.actionHashes.size() * threshold;
        return condition1;

        /*
        int count = 0;
        HashSet<ActionHash> set = new HashSet<>();
        for (ActionHash a1: actionHashes) {
            for (ActionHash a2: sHash.actionHashes) {
                if (a1.isSimilar(a2, 3) && ! set.contains(a2)) {
                    count ++;
                    set.add(a2);
                    break;
                }
            }
        }



        int smaller = Math.min(this.actionHashes.size(), ((UUpdate) hash).actionHashes.size()),
                larger = Math.max(this.actionHashes.size(), ((UUpdate) hash).actionHashes.size());

        return count >= 0.75 * smaller && larger * 0.75 <= smaller ||
                (larger <= 3 && smaller <= 3 && count >= 1);
                */
    }

}
