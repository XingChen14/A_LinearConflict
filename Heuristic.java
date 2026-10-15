//package heuristics;

public abstract class Heuristic {

    protected int boardDim;

    public Heuristic(int boardDim) {
        this.boardDim = boardDim;
    }

    public abstract int heuristic(int[] state, int[] goalState);
}