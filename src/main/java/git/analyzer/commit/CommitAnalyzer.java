package git.analyzer.commit;

import git.analyzer.histories.HistoryAnalyzer;
import git.analyzer.histories.Issue;
import git.analyzer.histories.variation.MethodMutantType;
import git.git.ClassParser;
import git.git.GitAnalyzer;
import git.git.Method;
import javafx.util.Pair;
import org.eclipse.jgit.revwalk.RevCommit;
import git.util.WriterTool;

import java.util.*;

public class CommitAnalyzer {

    GitAnalyzer git;

    public CommitAnalyzer(GitAnalyzer git) {
        this.git = git;
    }

    public List<ChangedMethod> start(String commitId) {
        List<ChangedMethod> methods = getMethods(commitId);
        analyzeMethodType(methods);
        return methods;
    }

    public Map<String, Pair<String, String>> getAllFiles(String commitId) {
        Map<String, Pair<String, String>> result = new HashMap<>();
        for (String fileName: git.getAllFilesModifiedByCommit(commitId,  ".java")) {
            Pair<String, String> pair = new Pair<String, String>(
                    git.getFileFromCommit(git.getId(commitId + "^"), fileName),
                    git.getFileFromCommit(git.getId(commitId), fileName)
            );
            result.put(fileName, pair);
        }
        return result;
    }

    public List<ChangedMethod> getMethods(String commitId) {
        List<ChangedMethod> result = new ArrayList<>();
        Map<String, Pair<String, String>> files = getAllFiles(commitId);

        for (String file: files.keySet()) {
            String temp = file.substring(0, file.lastIndexOf("/"));
            if (temp.toLowerCase().startsWith("test"))
                continue;
            Pair<String, String> pair = files.get(file);
            String oldContent = pair.getKey(), newContent = pair.getValue();
            ClassParser newParser = new ClassParser().setSourceCode(newContent), oldParser = new ClassParser().setSourceCode(oldContent);

            Map<String, Method> changeMethods = newParser.getChangedMethod(git.getEditList(oldContent, newContent), true);
            for (String methodName: changeMethods.keySet()) {
                Method method = changeMethods.get(methodName);
                ChangedMethod changedMethod = new ChangedMethod();
                changedMethod.filePath = file;
                try {
                    temp = methodName.substring(0, methodName.indexOf("("));
                    changedMethod.className = temp.substring(temp.lastIndexOf(".") + 1, temp.lastIndexOf(":"));
                    changedMethod.methodName = temp.substring(methodName.indexOf(":") + 1);
                }catch (Exception e) {
                    System.out.println(methodName);
                }
                changedMethod.setFileContent(method.methodContent);

                changedMethod.newlyAdded = !oldParser.contains(methodName);
                result.add(changedMethod);
            }
        }
        return result;
    }

    public void analyzeMethodType(List<ChangedMethod> methods) {
        for (ChangedMethod method1: methods) {
            String log = method1.className + ":\n  call:\n";
            String call = "";
            String dep = "";
            for (ChangedMethod method2: methods) {
                if (method2.contains(method1.className) && method2.contains(method1.methodName)) {
                    call += "    " + method2.filePath + " " + method2.methodName + "\n";
                    method1.beCalled = true;
                    //method2与method1不在同一个路径下
                    if (!method2.fileContent.equals(method1.fileContent)) {
                        method1.externalCalled = true;
                    }
                }

                if (method1.contains(method2.methodName) && method2.contains(method2.className)) {
                    method1.dependent ++;
                    dep += "   " + method2.filePath + " " + method2.methodName + "\n";
                }
            }
            method1.type = MethodMutantType.toType(method1.newlyAdded, method1.beCalled, method1.externalCalled, method1.dependent);
            /*System.out.println(method1.filePath + " " + method1.methodName + ":");
            System.out.println("  call:") ;
            System.out.println(call);
            System.out.println("  dependent:");
            System.out.println(dep);*/
        }
    }

    public static void main(String[] args) {
        GitAnalyzer git = new GitAnalyzer("C:\\Users\\oliver\\Downloads\\lucene-solr-master\\lucene-solr");
        CommitAnalyzer commitAnalyzer = new CommitAnalyzer(git);
        Map<String, Re> issueTypes = new HashMap<>();
        Map<String, Integer> issueTypeSet = new HashMap<>();
        int typeCount = 0;
        Set<String> status = new HashSet<>();
        for (int i = 1; i < 13000 + 8600; i ++) {
            String issueId = "";
            if (i <= 13000)


                continue;
                //issueId = "SOLR-" + i;
            else
                issueId = "LUCENE-" + (i - 13000);
            //System.out.println(issueId);
            Issue issue = new Issue(issueId);
            if (issue.description != null && issue.description.contains("Double") && issue.description.contains("Long") ){//&& issue.description.contains("Integer")) {
                System.out.println(issueId);
            }
        }
    }
}
