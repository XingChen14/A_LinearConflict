public class Manhattan extends Heuristic {

    public Manhattan(int boardDim) {
        super(boardDim);
    }

    @Override
    public int heuristic(int[] state, int[] goalState) {
        int distance = 0;

        for (int i = 0; i < state.length; i++) {
            int tile = state[i];

            if (tile == 0) {
                continue;
            }

            int goalIndex = -1;

            for (int j = 0; j < goalState.length; j++) {
                if (goalState[j] == tile) {
                    goalIndex = j;
                    break;
                }
            }

            if (goalIndex == -1) {
                throw new IllegalArgumentException(
                    "Ficha no encontrada en el objetivo: " + tile
                );
            }

            int row = i / boardDim;
            int col = i % boardDim;

            int goalRow = goalIndex / boardDim;
            int goalCol = goalIndex % boardDim;

            distance += Math.abs(row - goalRow)
                      + Math.abs(col - goalCol);
        }

        return distance;
    }
}