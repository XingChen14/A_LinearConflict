//package algorithms;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

public class AStar {

    private final Heuristic heuristic;
    private final int[] goalState;

    private HashMap<Integer, int[]> statesToGoal;
    private int nodesExpanded;
    private long runningTime;

    public AStar(int[] goalState, Heuristic heuristic) {
        this.goalState = goalState.clone();
        this.heuristic = heuristic;
        this.statesToGoal = new HashMap<>();
    }

    private static class Node {
        int[] state;
        int g;
        int f;
        String key;

        Node(int[] state, int g, int f) {
            this.state = state.clone();
            this.g = g;
            this.f = f;
            this.key = Arrays.toString(state);
        }
    }

    public boolean search(int[] initialState) {
        long start = System.currentTimeMillis();

        statesToGoal.clear();
        nodesExpanded = 0;

        String startKey = Arrays.toString(initialState);
        String goalKey = Arrays.toString(goalState);

        PriorityQueue<Node> frontier = new PriorityQueue<>(
            Comparator.comparingInt((Node n) -> n.f)
                      .thenComparingInt(n -> n.g)
        );

        HashMap<String, Integer> cost = new HashMap<>();
        HashMap<String, String> parent = new HashMap<>();
        HashSet<String> explored = new HashSet<>();

        cost.put(startKey, 0);
        parent.put(startKey, null);

        frontier.add(new Node(
            initialState, 0,
            heuristic.heuristic(initialState, goalState)
        ));

        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            if (current.g != cost.getOrDefault(
                    current.key, Integer.MAX_VALUE)) {
                continue;
            }

            if (!explored.add(current.key)) {
                continue;
            }

            nodesExpanded++;

            if (current.key.equals(goalKey)) {
                rebuildPath(parent, goalKey);
                runningTime = System.currentTimeMillis() - start;
                return true;
            }

            for (int[] child : getChildren(current.state)) {
                String childKey = Arrays.toString(child);
                int newCost = current.g + 1;

                if (newCost < cost.getOrDefault(
                        childKey, Integer.MAX_VALUE)) {

                    cost.put(childKey, newCost);
                    parent.put(childKey, current.key);
                    explored.remove(childKey);

                    int h = heuristic.heuristic(child, goalState);

                    frontier.add(new Node(
                        child, newCost, newCost + h
                    ));
                }
            }
        }

        runningTime = System.currentTimeMillis() - start;
        return false;
    }

    private List<int[]> getChildren(int[] state) {
        List<int[]> children = new ArrayList<>();
        int dim = (int) Math.sqrt(state.length);
        int empty = -1;

        for (int i = 0; i < state.length; i++) {
            if (state[i] == 0) {
                empty = i;
                break;
            }
        }

        if (empty == -1) {
            throw new IllegalArgumentException(
                "El tablero debe contener el 0."
            );
        }

        int row = empty / dim;
        int col = empty % dim;

        int[] moves = {-dim, dim, -1, 1};

        for (int move : moves) {
            int next = empty + move;

            if (next < 0 || next >= state.length) continue;
            if (move == -1 && col == 0) continue;
            if (move == 1 && col == dim - 1) continue;

            int[] child = state.clone();
            child[empty] = child[next];
            child[next] = 0;

            children.add(child);
        }

        return children;
    }

    private void rebuildPath(
            HashMap<String, String> parent, String goalKey) {

        List<int[]> reversed = new ArrayList<>();
        String current = goalKey;

        while (current != null) {
            reversed.add(parseState(current));
            current = parent.get(current);
        }

        Collections.reverse(reversed);

        for (int i = 0; i < reversed.size(); i++) {
            statesToGoal.put(i, reversed.get(i).clone());
        }
    }

    private int[] parseState(String key) {
        String cleaned = key.substring(1, key.length() - 1);
        String[] parts = cleaned.split(",\\s*");
        int[] state = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            state[i] = Integer.parseInt(parts[i]);
        }

        return state;
    }

    public HashMap<Integer, int[]> getStatesToGoal() {
        HashMap<Integer, int[]> copy = new HashMap<>();

        statesToGoal.forEach(
            (step, state) -> copy.put(step, state.clone())
        );

        return copy;
    }

    public int getSolutionDepth() {
        return Math.max(0, statesToGoal.size() - 1);
    }

    public int getNodesExpanded() {
        return nodesExpanded;
    }

    public long getRunningTime() {
        return runningTime;
    }
}