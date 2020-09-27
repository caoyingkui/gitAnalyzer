package cluster;

import git.fileDiff.Change;
import git.fileDiff.Diff;
import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.method.ChangedMethod;
import git.fileDiff.method.MethodDiff;
import git.git.GitAnalyzer;
import me.tongfei.progressbar.ProgressBar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by kvirus on 2020/9/25 14:51
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Miner {

    private static List<List<StatementHash>> getHashes(List<Change<String>> methods) {
        List<List<StatementHash>> hashes = new ArrayList<>();
        for (Change<String> method : ProgressBar.wrap(methods, "解析代码")) {
            hashes.add(MethodDiff.getHashes(method.NEW, method.OLD));
        }
        return hashes;
    }

    private static SimilarityMatrix mine(List<List<StatementHash>> methods) {
        System.out.println("开始挖掘模式...");
        SimilarityMatrix matrix = new SimilarityMatrix(methods);
        matrix.getResults();
        return matrix;
    }
    
    public static List<List<Integer>> mine(List<Change<String>> methods,  List<List<Integer>> targets) {
        List<List<StatementHash>> hashes = getHashes(methods);
        SimilarityMatrix matrix = mine(hashes);
        return matrix.getResults();
    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer("K:\\Data_for_mining_similar_code_change\\lucene-solr");
        List<ChangedMethod> methods = new ArrayList<>();
        git.getCommits().stream().limit(100).forEach(commit -> {
            try {
                Diff diff = new Diff(git, commit);
                diff.getClasses().stream().forEach(file -> {
                    for (MethodDiff method : file.getMethods()) {
                        if (method instanceof ChangedMethod) {
                            ChangedMethod cMethod = (ChangedMethod) method;
                            methods.add(cMethod);
                        }
                    }
                });
            } catch (Exception e) {

            }
        });

        List<Change<String>> changes = new ArrayList<>();
        for(ChangedMethod method: methods) {
            //在这里只需要

            changes.add(method.content);
        }

        Object object = Miner.mine(changes, null);
        int a = 2;
    }




}
