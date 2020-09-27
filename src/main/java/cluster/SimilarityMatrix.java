package cluster;

import git.fileDiff.group.hash.StatementHash;
import git.fileDiff.method.MethodDiff;
import javafx.util.Pair;
import git.util.SerializeTool;

import java.io.File;
import java.io.Serializable;
import java.util.*;

/**
 * Created by kvirus on 2019/7/4 15:57
 * Email @ caoyingkui@pku.edu.cn
 * <p>
 * |   *******    **      **     **     **
 * |  **            **  **       **  **
 * |  **              **         ***
 * |  **              **         **  **
 * |   *******        **         **     **
 */
public class SimilarityMatrix implements Serializable{

    public static class Calculate implements Runnable {

        public int start = 0;
        public int end = 0;
        public SimilarityMatrix matrix;
        public List<List<StatementHash>> methods;

        public Calculate(SimilarityMatrix matrix, List<List<StatementHash>> methods, int start, int end) {
            this.matrix = matrix;
            this.methods = methods;

            this.start = start;
            this.end = end;
        }

        @Override
        public void run() {
            int count = 0;
            for (int i = start; i < end; i ++) {
                count ++;
                if (count % 1000 == 0) System.out.println(start + "->" + end + ": " + count);
                for (int j = start + 1; j < methods.size(); j++) {

                    double weight = MethodDiff.isSimilar(methods.get(i), methods.get(j));
                    if (weight > 0) {
                        matrix.connect(start + i, j, weight);
                    }
                }
            }
        }
    }

    int size = 0;

    private Map<Integer, Set<Integer>> graph = new HashMap<>();

    private Map<String, Double> edges = new HashMap<>();

    private List<List<Integer>> results = null;

    public SimilarityMatrix(int size) {
        this.size = size;
    }

    public SimilarityMatrix(List<List<StatementHash>> methods) {
        try {
            this.size = methods.size();

            int count = 0;
            for (int i = 0; i < size; i ++) {
                count ++;
                if (count % 1000 == 0) System.out.println("complete: " + count);
                for (int j = i + 1; j < i + 1000 && j < size; j++) {

                    double weight = MethodDiff.isSimilar(methods.get(i), methods.get(j));
                    if (weight > 0) {
                        this.connect(i, j, weight);
                    }
                }
            }

            /*
            int threadSize = 20, batch = methods.size() / threadSize / 1000 * 1000 + 1000;
            ExecutorService fixedThreadPool = Executors.newFixedThreadPool(threadSize);
            for (int i = 0; i < size; i+= batch) {
                Calculate calculate = new Calculate(this, methods, i, Math.min(i + batch, size));
                fixedThreadPool.execute(calculate);
            }
            // No more threads can be submitted to the executor service!
            fixedThreadPool.shutdown();

            // Blocks until all 100 submitted threads have finished!
            fixedThreadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.MINUTES);*/
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public synchronized void connect(int x, int y, double weight) {
        if (!graph.containsKey(x)) graph.put(x, new HashSet<>());
        graph.get(x).add(y);

        if (!graph.containsKey(y)) graph.put(y, new HashSet<>());
        graph.get(y).add(x);

        edges.put(getEdge(x, y), weight);
    }

    public String getEdge(int x, int y) {
        if (x > y) {
            int temp = x;
            x = y;
            y = temp;
        }
        return x + "_" + y;
    }

    public Set<Integer> findConnectedGraph(int node) {
        Set<Integer> result     = new HashSet<>();
        Set<Integer> toVisit    = new HashSet<>();

        toVisit.add(node);
        while (toVisit.size() > 0) {
            int next = toVisit.iterator().next();
            result.add(next);

            for (int neighbor: graph.get(next)) {
                if ( !result.contains(neighbor)) toVisit.add(neighbor);
            }
            toVisit.remove(next);
        }
        return result;
    }

    /**
     * 寻找包含节点node的完全图
     * @param node 节点的id
     * @param hasVisited 目前已经访问过的所有节点
     * @return 返回一个集合，该集合包含的元素是完全图的所有节点的ID
     */
    public Pair<Double, List<Integer>> findCompleteSubGraph(int node, Set<Integer> hasVisited) {
        // 记录与node连接的节点，与node之间的权重
        List<Pair<Integer, Double>> connected = new ArrayList<>();

        graph.get(node).stream()
                .filter(n -> !hasVisited.contains(n))
                .forEach(n -> {
                    double weight = edges.get(getEdge(node, n));
                    connected.add(new Pair<Integer, Double>(n, weight));
                });
        connected.sort((a, b) -> -Double.compare(a.getValue(), b.getValue()));

        ArrayList<Integer> result = new ArrayList<>();
        result.add(node);

        // 用来记录本次抽取的node的完全图时，其中完全图中与node连接的节点的最大的权重
        double largest = -1;

        // 记录每次循环之前，当前已经生成的图中的最大和最小的权重。
        double min = Integer.MAX_VALUE, max = 0;

        for (int i = 0; i < connected.size(); i++) {
            int next = connected.get(i).getKey(); // 即将要添加的节点
            double temp = min; // 记录之前最小权重的边

            // if (edges.get(getEdge(node, next)).intValue() < 2) break;

            max = 0;
            for(int n: result) {
                double weight = edges.getOrDefault(getEdge(n, next), 0.0).doubleValue();
                if (weight <= 0) {
                    max = -1;
                    break;
                } else if (weight > max) {
                    max = weight;
                }

                if (weight < min) {
                    min = weight;
                }
            }
            if (max == -1) break;

            // 新增加的节点至少有一条边的权重，大于之前所有权重的最小值。
            if (result.size() > 2 && max < temp) break;

            // 因为不加这条限制的话，就以为着找到的图至少三个点，可能会引发错误
            // 一种情况是 比方说现在有三个点： 1， 2 ，3
            // 1和2的权重是5， 1和3的权重只有1， 这样的话，可能3和1,2并不一定是相似的
            // 加了这条限制之后，一定程度上避免了这种情况
            if (result.size() == 2 && temp * 0.5 > max) break;


            //因为max一定是node和第一个加入的节点间的权重
            if (largest == -1) largest = max;
            result.add(next);
        }

        if (largest == -1)
            return null;
        else
            return new Pair<Double, List<Integer>>(largest, result);
    }

    public List<List<Integer>> split(Set<Integer> connectedGraph) {
        List<List<Integer>> result  = new ArrayList<>();
        Set<Integer> toVisit        = new HashSet<>(connectedGraph);
        Set<Integer> hasVisit       = new HashSet<>();

        Map<Integer, Pair<Double, List<Integer>>> graphMap = new HashMap<>();
        Map<Integer, Set<Integer>> nodes = new HashMap<>();
        for (int node: connectedGraph) {
            Pair<Double, List<Integer>> pair = findCompleteSubGraph(node, hasVisit);
            if (pair == null) {
                toVisit.remove(node);
                hasVisit.add(node);
                continue;
            }

            graphMap.put(node, pair);
            for (int neighbor: pair.getValue()) {
                if (!nodes.containsKey(neighbor)) nodes.put(neighbor, new HashSet<>());
                nodes.get(neighbor).add(node);
            }
        }


        while (toVisit.size() > 0) {
            System.out.println(new Date().toString());
            System.out.println(toVisit.size());
            List<Integer> maxCompleteGraph = null;
            double maxWeight = 0;

            for (int node: toVisit) {
                Pair<Double, List<Integer>> pair = graphMap.get(node);
                if (maxCompleteGraph == null || pair.getKey() > maxWeight) {
                    maxWeight = pair.getKey();
                    maxCompleteGraph = pair.getValue();
                } else if (pair.getKey() == maxWeight && pair.getValue().size() > maxCompleteGraph.size()) {
                    maxCompleteGraph = pair.getValue();
                }
            }
            toVisit.removeAll(maxCompleteGraph);
            hasVisit.addAll(maxCompleteGraph);

            result.add( new ArrayList<>(maxCompleteGraph));

            Set<Integer> nodesToUpdate = new HashSet<>();
            maxCompleteGraph.forEach(node -> {
                nodes.get(node).forEach(neighbor -> {
                    if (toVisit.contains(neighbor))
                        nodesToUpdate.add(neighbor);
                });
                nodes.remove(node);
            });

            nodesToUpdate.forEach(node -> {
                Pair<Double, List<Integer>> pair = findCompleteSubGraph(node, hasVisit);

                //与node相邻的节点都被分给其他的节点了
                if (pair == null) {
                    toVisit.remove(node);
                    hasVisit.add(node);
                    return;
                }

                graphMap.put(node, pair);
                for (int neighbor: pair.getValue()) {
                    if (!nodes.containsKey(neighbor)) nodes.put(neighbor, new HashSet<>());
                    nodes.get(neighbor).add(node);
                }
            });
        }

        return result;
    }

    public List<List<Integer>> getResults() {
        if (results != null) return results;

        results = new ArrayList<>();

        Set<Integer> toVisit = new HashSet<>();
        toVisit.addAll(graph.keySet());

        while (toVisit.size() > 0) {
            int next = toVisit.iterator().next();

            Set<Integer> connectedGraph = findConnectedGraph(next);
            results.addAll(split(connectedGraph));

            toVisit.removeAll(connectedGraph);
        }

        results.removeIf(result -> result.size() == 1);
        Map<Integer, Integer> method2cluster = new HashMap<>();
        for (int i = 0; i < results.size(); i++) {
            for (int m: results.get(i))
                method2cluster.put(m, i);
        }


        /*
        // 把最终聚合结果中的，只有一个的游离的节点，并入曾经相连的邻居所属的类中。
        for (int node = 0; node < size; node++) {
            if (method2cluster.containsKey(node)) continue;

            for (int neighbor: graph.getOrDefault(node, new HashSet<>())) {
                if (method2cluster.containsKey(neighbor)) {
                    results.get(method2cluster.get(neighbor)).add(node);
                    break;
                }
            }
        }*/

        results.sort((l1, l2) -> -Integer.compare(l1.size(), l2.size()));

        return results;
    }

    public void print() {
        if (results == null) getResults();
        results.stream().forEach(group -> {
            System.out.print(group + ", ");
        });
        System.out.println("");
    }

    public static void test() {
        Scanner scanner = new Scanner(System.in);
        int n = scanner.nextInt();
        int x = 0, y = 0;
        SimilarityMatrix result = new SimilarityMatrix(n);

        while ((x = scanner.nextInt())!= -1 &&
                (y = scanner.nextInt()) != -1) {
            result.connect(x, y, 1);
        }

        result.getResults().stream().forEach(group -> {
            group.stream().forEach(node -> System.out.print(node + " "));
            System.out.println();
        });

        SerializeTool.writer(new File("1"), result);
    }

    public static void main(String[] args) {
        test();
        Result result = (Result)SerializeTool.read(new File("1"));
        result.print();
    }


}
