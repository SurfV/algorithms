package coding_problems.sedgewick.coursera.course2.week1_graphs;

import edu.princeton.cs.algs4.Digraph;
import edu.princeton.cs.algs4.In;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public class SAP {

    private final Digraph graph;

    // constructor takes a digraph (not necessarily a DAG)
    public SAP(Digraph G) {
        graph = new Digraph(G);
    }

    // do unit testing of this class
    public static void main(String[] args) {
        String digraphPath = "resources/coding_problems/sedgewick/coursera/course2/week1_graphs/digraph_tournament.txt";
        In in = new In(digraphPath);
        Digraph G = new Digraph(in);
        SAP sap = new SAP(G);
//        while (!StdIn.isEmpty()) {
//            int v = StdIn.readInt();
//            int w = StdIn.readInt();
//            int length = sap.length(v, w);
//            int ancestor = sap.ancestor(v, w);
//            StdOut.printf("length = %d, ancestor = %d\n", length, ancestor);
//        }
//        System.out.println(sap.ancestor(Arrays.asList(13, 23, 24), Arrays.asList(6, 16, 17)));
        System.out.println(G);
        System.out.println(sap.length(1, 3));
//        System.out.println(sap.length(3, 3));
    }

    // length of shortest ancestral path between v and w; -1 if no such path
    public int length(int v, int w) {
        ShortestPath shortestPath = shortestPath(v, w);
//        System.out.println(shortestPath);
        return shortestPath.length();
    }

    // a ancestor ancestor of v and w that participates in a shortest ancestral path; -1 if no such path
    public int ancestor(int v, int w) {
        ShortestPath shortestPath = shortestPath(v, w);
//        System.out.println(shortestPath);
        return shortestPath.ancestor;
    }

    // length of shortest ancestral path between any vertex in v and any vertex in w; -1 if no such path
    public int length(Iterable<Integer> v, Iterable<Integer> w) {
        return shortestPath(v, w).length();
    }

    // a ancestor ancestor that participates in shortest ancestral path; -1 if no such path
    public int ancestor(Iterable<Integer> v, Iterable<Integer> w) {
        return shortestPath(v, w).ancestor;
    }

    private ShortestPath shortestPath(Iterable<Integer> vIterable, Iterable<Integer> wIterable) {
        validateNull(vIterable);
        validateNull(wIterable);

        ShortestPath result = ShortestPath.empty();
        for (Integer v : vIterable) {
            result = shortestPath(result, shortestPath(v, wIterable));
        }
        return result;
    }

    private ShortestPath shortestPath(Integer v, Iterable<Integer> wIterable) {
        ShortestPath result = ShortestPath.empty();
        for (Integer w : wIterable) {
            result = shortestPath(result, shortestPath(v, w));
        }
        return result;
    }

    private ShortestPath shortestPath(ShortestPath nullablePath1, ShortestPath path2) {
        if (nullablePath1 == null) {
            return path2;
        }
        if (nullablePath1.length() == -1) {
            return path2;
        }

        if (nullablePath1.length() > path2.length()) {
//            System.out.println("Path1: " + nullablePath1 + ", Path2: " + path2 + ". Shortest: " + path2);
            return path2;
        }
//        System.out.println("Path1: " + nullablePath1 + ", Path2: " + path2 + ". Shortest: " + nullablePath1);
        return nullablePath1;
    }

    private ShortestPath shortestPath(Integer v, Integer w) {
        validateNull(v);
        validateNull(w);
//        System.out.println(v + "\t" + w);

        if (v == w) {
            return new ShortestPath(v, Collections.singletonList(v));
        }

        // use 2 simultaneous BFS {
        Set<Integer> investigated1 = new HashSet<>();
        investigated1.add(v);
        Set<Integer> investigated2 = new HashSet<>();
        investigated2.add(w);

        Map<Integer, Integer> previous1 = new HashMap<>();
        previous1.put(v, v);
        Map<Integer, Integer> previous2 = new HashMap<>();
        previous2.put(w, w);

        Queue<Integer> queue1 = new LinkedList<>();
        queue1.offer(v);

        Queue<Integer> queue2 = new LinkedList<>();
        queue2.offer(w);

        ShortestPath shortestPath = ShortestPath.empty();
        while (!queue1.isEmpty() || !queue2.isEmpty()) {
            ShortestPath w1 = checkNeighbourElementsFromQueue(investigated1, investigated2, previous1, previous2, queue1);
            if (w1 != null && (w1.length() < shortestPath.length() || shortestPath.length() == -1)) {
                shortestPath = w1;
            }
            ShortestPath w2 = checkNeighbourElementsFromQueue(investigated2, investigated1, previous2, previous1, queue2);
            if (w2 != null && (w2.length() < shortestPath.length() || shortestPath.length() == -1)) {
                shortestPath = w2;
            }
        }
        return shortestPath;
    }

    private ShortestPath checkNeighbourElementsFromQueue(Set<Integer> investigated1,
                                                         Set<Integer> investigated2,
                                                         Map<Integer, Integer> previous1,
                                                         Map<Integer, Integer> previous2,
                                                         Queue<Integer> queue) {
        if (queue.isEmpty()) {
            return null;
        }

        int v1 = queue.poll();
        Iterable<Integer> adj1 = graph.adj(v1);
        ShortestPath shortestPath = null;
        for (int w1 : adj1) {
            if (!investigated1.contains(w1)) {
                investigated1.add(w1);
                previous1.put(w1, v1);
                queue.offer(w1);
            }
            if (investigated2.contains(w1)) {
                shortestPath = shortestPath(shortestPath, new ShortestPath(w1, buildPath(previous1, previous2, w1)));
            }
        }
        return shortestPath;
    }

    private List<Integer> buildPath(Map<Integer, Integer> previous1, Map<Integer, Integer> previous2, int ancestor) {
        LinkedList<Integer> result = new LinkedList<>();
        int val = ancestor;
        while (val != (val = previous1.get(val))) {
            result.addFirst(val);
        }
        result.addLast(ancestor);
        val = ancestor;
        while (val != (val = previous2.get(val))) {
            result.addLast(val);
        }
//        System.out.println(result);
        return result;
    }

    private static class ShortestPath {
        final int ancestor;
        final List<Integer> path;

        ShortestPath(int ancestor, List<Integer> path) {
            this.ancestor = ancestor;
            this.path = path;
        }

        static ShortestPath empty() {
            return new ShortestPath(-1, null);
        }

        int length() {
            return path == null ? -1 : path.size() - 1;
        }

        @Override
        public String toString() {
            return "Ancestor: " + ancestor + ". Path: " + path;
        }
    }

    private void validateNull(Object object) {
        if (object == null) throw new IllegalArgumentException(object + " is null");
    }
}
