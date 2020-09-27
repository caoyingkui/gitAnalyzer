package cluster;

import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.method.MethodDiff;
import org.json.JSONArray;
import org.json.JSONObject;
import git.util.SetTool;
import git.util.WriterTool;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;

/**
 * Created by kvirus on 2019/7/23 14:46
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class Result {

    static int target = 1008;

    private List<List<StatementHash>> hashes;

    // 每一个元素都是一组index，代表同属一个类的结果
    public List<List<Integer>> groundTruth  = new ArrayList<>();

    // 每一个元素都是一组index，代表分类结果中，同属一个类的结果
    public List<List<Integer>> miningResult = new ArrayList<>();

    // 每一个元素维度是2，
    // 第一维为groundTruth中的index, 第二维为miningResult中的index。
    public List<List<Integer>> identical    = new ArrayList<>();

    // 每一个元素维度是1，
    // 都是一groundTruth的组的index,代表分类为识别出。
    public List<List<Integer>> missing      = new ArrayList<>();

    // 每一个元素维度是2，
    // 第一个维度是groundTruth中类的index,第二个维度是对应的分类结果的index
    // 代表分类结果是ground truth的子集
    // 例如: ground truth为 {1,2,3,4}， 分类结果为 {1,2,3}
    public List<List<Integer>> sub          = new ArrayList<>();

    // 每一个元素维度是2，
    // 第一个维度是groundTruth中类的index,第二个维度是对应的分类结果的index
    // 代表分类结果是ground truth的超集
    // 例如: ground truth为 {1,2,3,4}， 分类结果为 {1,2,3,4,5}
    public List<List<Integer>> extend       = new ArrayList<>();

    // 每一个元素维度多维的
    // 第一个维度是groundTruth中类的index,之后的每一个维度代表结果的index
    // 代表分类结果是ground truth的被分割为多个子集，这多个子集的并集为ground truth
    // 例如: ground truth为 {1,2,3,4}， 分类结果为 {1,2}{3,4}
    public List<List<Integer>> split        = new ArrayList<>();

    // 每一个元素维度是2，
    // 第一个维度是groundTruth中类的index,之后的每一个维度代表结果的index
    // 代表分类结果是ground truth的被分割为多个子集，这多个子集的并集为ground truth有交集
    // 且所有的子集中不存在被分到其他类别的情况
    // 例如： {1，2，3} 被分类为 {1,3,4}，{2,5}， 4 和 5 均被识别到其他任何的类别中。
    public List<List<Integer>> overlap      = new ArrayList<>();

    // 其他情况，可以初步认为是不正确的类别
    public List<List<Integer>> others       = new ArrayList<>();

    public List<Integer> newClasses         = new ArrayList<>();

    public Result(List<List<StatementHash>> hashes, List<List<Integer>> groundTruth, List<List<Integer>>miningResult) {
        this.hashes       = hashes;
        this.groundTruth  = groundTruth;
        this.miningResult = miningResult;
        analysize();
    }

    public Result(List<List<Integer>> groundTruth, List<List<Integer>>miningResult) {
        this.groundTruth  = groundTruth;
        this.miningResult = miningResult;
        analysize();
    }

    public void analysize() {

        Map<Integer, Set<Integer>> matrix = new HashMap<>();
        for (int index = 0; index < groundTruth.size(); index ++) {
            List<Integer> group = groundTruth.get(index);
            for (int i = 0; i < group.size(); i++) {
                int ni = group.get(i);
                if (!matrix.containsKey(ni)) matrix.put(ni, new HashSet<>());
                for (int j = i + 1; j < group.size(); j++) {
                    int nj = group.get(j);
                    if (!matrix.containsKey(nj)) matrix.put(nj, new HashSet<>());

                    matrix.get(ni).add(nj);
                    matrix.get(nj).add(ni);

                }
            }

        }


        // ground truth中，每一个函数对应的所属的类别的编号
        Map<Integer, Integer> gtMap = new HashMap<>();
        for (int index = 0; index < groundTruth.size(); index++) {
            for (int item: groundTruth.get(index))
                gtMap.put(item, index);
        }


        // mining result中，每一个函数对应的所属的类别的编号
        Map<Integer, Integer> mrMap = new HashMap<>();
        for (int index = 0; index < miningResult.size(); index++) {
            for (int item: miningResult.get(index))
                mrMap.put(item, index);
        }


        // 记录mining result中有对应的ground truth类别，那么其余的就是new class
        Set<Integer> visited = new HashSet<>();

        for (int index = 0; index < groundTruth.size(); index++) {

            if (index == target) {
                int a = 2;
            }

            List<Integer> result = new ArrayList<>();
            result.add(index);

            List<Integer> ground = groundTruth.get(index);

            //记录ground中所有元素对应的被分到哪些类（编号）中
            Set<Integer> set1 = new HashSet<>();
            //记录分类结果中的元素对应的来自哪些类（编号）
            Set<Integer> set2 = new HashSet<>();
            //记录ground中所有元素对应的被分到哪些类（编号）中包含的所有的元素
            List<Integer> all = new ArrayList<>();

            for(int item: ground) {
                int g = mrMap.getOrDefault(item, -1);
                if (g == -1) continue;

                visited.add(g);
                set1.add(g);
                for (int i: miningResult.get(g)) {
                    all.add(i);
                    if (gtMap.containsKey(i))
                        set2.add(gtMap.get(i));
                }
            }


            result.addAll(set1);
            // missing
            if (set1.size() == 0) {
                missing.add(result);
                continue;
            }


            int type = SetTool.difference(groundTruth.get(index), all);

            if (type == 0 && set1.size() == 1)      identical.add(result);
            else if (type == 0 && set1.size() > 1)  split.add(result);
            else if (type == 1)     sub.add(result);
            else {
                // 分类结果中没有在ground truth中
                Set<Integer> set = new HashSet<>();
                for (int n: all)
                    if (!ground.contains(n)) set.add(n);

                boolean s = true;
                for (int n: set) {
                    if (!gtMap.containsKey(n)) continue;

                    boolean isConnected = false;
                    for (int g: ground) {
                        if (matrix.get(g).contains(n)) {
                            isConnected = true;
                            break;
                        }
                    }
                    if (!isConnected) {
                        s = false;
                        break;
                    }
                }
                if (!s) others.add(result);
                else if (type == -1) extend.add(result);
                else if (type == 2) overlap.add(result);
            }

        }

        for (int index = 0; index < miningResult.size(); index++) {
            if (!visited.contains(index))
                newClasses.add(index);
        }
    }

    public void analyze() {
        Map<Integer, Integer> map1= new HashMap<>();
        Map<Integer, Integer> map2 = new HashMap<>();
        for (int i = 0; i < groundTruth.size(); i++) {
            for (Integer j: groundTruth.get(i)) {
                map1.put(j, i);
            }
        }

        Set<Integer> myset = new HashSet<>();
        int identical = 0;
        int sub = 0;
        int extend = 0;
        int missing = 0;
        int overlap = 0;
        int newc = 0;
        for (int i = 0; i < miningResult.size(); i++) {
            Set<Integer> set = new HashSet<>();
            for (Integer j: miningResult.get(i)){
                if (map1.containsKey(j)) set.add(map1.get(j));
            }
            boolean s1 = false, s2 = false, s3 = false, s4 = false;
            if (set.size() > 0) {
                myset.addAll(set);
                for (Integer s: set) {
                    int r = SetTool.difference(groundTruth.get(s), miningResult.get(i));
                    if (r == 0) s1 = true;
                    else if (r == 1) s2 = true;
                    else if (r == -1) s3 = true;
                    else if (r == 2) s4 = true;
                    else System.out.println("error");
                }
                if (s1) identical ++;
                else if (s3) extend ++;
                else if (s4) overlap ++;
                else if (s2) sub ++;
            } else {
                newc ++;
            }
        }

        missing = groundTruth.size() - myset.size();

        System.out.println("identical: " + identical);
        System.out.println("sub: " + sub);
        System.out.println("extend: " +extend);
        System.out.println("overlap: " + overlap);
        System.out.println("missing: " + missing);
        System.out.println("new: " + newc);
        System.out.println("ground: " + groundTruth.size());
        System.out.println("mining: " + miningResult.size());
    }

    public void printBrief() {
        System.out.println("gold: " + groundTruth.size());
        System.out.println("identical: " + identical.size());
        System.out.println(identical.size() - missing.size());
        System.out.println("missing: " + missing.size());
        System.out.println("sub: " + sub.size());
        System.out.println("extend: " + extend.size());
        System.out.println("split: " + split.size());
        System.out.println("overlap: " + overlap.size());
        System.out.println("others: " + others.size());
        System.out.println();
    }

    public void print() {

        try {
            System.setOut(new PrintStream(new FileOutputStream("system_out.txt")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        printBrief();

        System.out.println("identical: " + identical.size());
        print(identical);

        System.out.println("missing: " + missing.size());
        print(missing);

        System.out.println("sub: " + sub.size());
        print(sub);

        System.out.println("extend: " + extend.size());
        print(extend);

        System.out.println("split: " + split.size());
        print(split);

        System.out.println("overlap: " + overlap.size());
        print(overlap);

        System.out.println("others: " + others.size());
        print(others);
    }

    private void print(List<List<Integer>> result) {
        for ( int j = 0; j < 150 && j < result.size(); j ++ ){
            List<Integer> r = result.get(j);
            System.out.println("id: " + j);
            System.out.println(r.get(0));
            System.out.println(groundTruth.get(r.get(0)).toString());
            if (r.size() == 1) {
                System.out.println("[-1]\n");
            } else {
                String str = "";
                for (int i = 1; i < r.size(); i++) {
                    str += miningResult.get(r.get(i)).toString() + ", ";
                }
                System.out.println(str.substring(0, str.length() - 2) + "\n");
            }
        }
    }

    public void output(List<Method> methods) {

        new File ("E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_All\\").mkdir();

        for (int i = 0; i < miningResult.size(); i++) {
            List<Integer> group = miningResult.get(i);
            output("E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_All\\", methods, group, i);

        }

        new File("E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_New\\").mkdir();
        outputNew(methods, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_New\\");

        output(methods, identical, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Identical\\");
        output(methods, sub, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Sub\\");
        output(methods, missing, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Miss\\");
        output(methods, extend, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Extend\\");
        output(methods, others, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Others\\");
        output(methods, overlap, "E:\\Intellij workspace\\DataMiner\\similarCodeChanges\\rawdata\\Result_Overlap\\");
    }

    public void output(String path, List<Method> methods, List<Integer> group, int index) {
        JSONObject res = new JSONObject();

        res.put("similarChanges", new JSONArray());

        res.put("index", index);

        JSONArray items = new JSONArray();
        int count = 0;
        for (int methodIndex: group) {
            String msg = methodIndex + ": ";
            Method m1 = methods.get(methodIndex);
            for (int other: group) {
                if (other == methodIndex) continue;
                Method m2 = methods.get(other);

                double sim = -0.5;

                try {
                    sim = MethodDiff.isSimilar(
                            hashes.get(methodIndex),
                            hashes.get(other));
                } catch (Exception e) {;}
                msg += other + ":" + sim + " ";
            }

            Method method = methods.get(methodIndex);
            JSONObject item = new JSONObject();
            item.put("id", ++count);
            item.put("project", method.gitPath);
            item.put("commit", method.commitId);
            item.put("msg", msg);
            item.put("methodName", "");
            item.put("newStr", method.newContent);
            item.put("oldStr", method.oldContent);
            items.put(item);
        }
        res.put("changes", items);

        WriterTool.write(path +  index, res.toString());
    }


    public void outputNew(List<Method> methods, String path) {
        for (int i = 0; i < newClasses.size(); i++) {
            List<Integer> group = miningResult.get(newClasses.get(i));
            output(path, methods, group, i);

        }
    }

    public void output(List<Method> methods, List<List<Integer>> results, String path) {
        File dir = new File(path);

        if (!dir.exists() || dir.isFile()) {
            dir.mkdir();
        }

        int index = 0;
        for (int i = 0; i < results.size(); i ++) {
            List<Integer> result = results.get(i);
            if (result.size() == 1) {
                index ++;
                List<Integer> group = groundTruth.get(result.get(0));
                output(path, methods, group, index);
            } else {
                for (int j = 1; j < result.size(); j++) {
                    List<Integer> group = miningResult.get(result.get(j));
                    index++;
                    output(path, methods, group, index);
                }
            }

        }

    }

}
