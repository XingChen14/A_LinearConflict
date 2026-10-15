public class LinearConflict extends Heuristic {

    private final Manhattan manhattan;

    public LinearConflict(int boardDim) {
        super(boardDim);
        this.manhattan = new Manhattan(boardDim);
    }

    @Override
    public int heuristic(int[] state, int[] goalState) {
        int distance = manhattan.heuristic(state, goalState);
        int conflicts = 0;

        // Conflictos por filas
        for (int row = 0; row < boardDim; row++) {
            for (int i = 0; i < boardDim; i++) {
                int tile1 = state[row * boardDim + i];

                if (tile1 == 0) continue;

                int goal1 = findGoalIndex(tile1, goalState);

                if (goal1 / boardDim != row) continue;

                for (int j = i + 1; j < boardDim; j++) {
                    int tile2 = state[row * boardDim + j];

                    if (tile2 == 0) continue;

                    int goal2 = findGoalIndex(tile2, goalState);

                    if (goal2 / boardDim == row && goal1 > goal2) {
                        conflicts++;
                    }
                }
            }
        }

        // Conflictos por columnas
        for (int col = 0; col < boardDim; col++) {
            for (int i = 0; i < boardDim; i++) {
                int tile1 = state[i * boardDim + col];

                if (tile1 == 0) continue;

                int goal1 = findGoalIndex(tile1, goalState);

                if (goal1 % boardDim != col) continue;

                for (int j = i + 1; j < boardDim; j++) {
                    int tile2 = state[j * boardDim + col];

                    if (tile2 == 0) continue;

                    int goal2 = findGoalIndex(tile2, goalState);

                    if (goal2 % boardDim == col && goal1 > goal2) {
                        conflicts++;
                    }
                }
            }
        }

        return distance + 2 * conflicts;
    }

    private int findGoalIndex(int tile, int[] goalState) {
        for (int i = 0; i < goalState.length; i++) {
            if (goalState[i] == tile) {
                return i;
            }
        }

        throw new IllegalArgumentException(
            "Ficha no encontrada: " + tile
        );
    }
}