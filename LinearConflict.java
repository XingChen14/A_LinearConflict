/**
 * Implementación de la heurística de Conflicto Lineal (Linear Conflict) para el N-Puzzle.
 * Esta heurística mejora la distancia Manhattan añadiendo un coste extra de 2 por cada par 
 * de fichas que se encuentran en su fila o columna final pero están invertidas (en conflicto),
 * obligando a una de ellas a apartarse para que la otra pueda pasar.
 */
public class LinearConflict extends Heuristic {

    private final Manhattan manhattan; // Instancia de la heurística Manhattan como base

    /**
     * Constructor de LinearConflict.
     * 
     * @param boardDim Dimensión del tablero (ej. 3 para 8-puzzle).
     */
    public LinearConflict(int boardDim) {
        super(boardDim);
        this.manhattan = new Manhattan(boardDim);
    }

    /**
     * Calcula la heurística total sumando la distancia Manhattan base y 
     * dos movimientos extra por cada conflicto lineal detectado.
     * 
     * @param state Estado actual del tablero.
     * @param goalState Estado objetivo deseado.
     * @return El valor heurístico total estimado.
     */
    @Override
    public int heuristic(int[] state, int[] goalState) {
        int distance = manhattan.heuristic(state, goalState);
        int conflicts = 0;

        // 1. Detección de conflictos por filas
        for (int row = 0; row < boardDim; row++) {
            for (int i = 0; i < boardDim; i++) {
                int tile1 = state[row * boardDim + i];

                // Ignorar el espacio vacío (0)
                if (tile1 == 0) continue;

                int goal1 = findGoalIndex(tile1, goalState);

                // Si la ficha 1 no pertenece a esta fila, no genera conflicto en esta línea
                if (goal1 / boardDim != row) continue;

                // Comparar con las demás fichas en la misma fila
                for (int j = i + 1; j < boardDim; j++) {
                    int tile2 = state[row * boardDim + j];

                    if (tile2 == 0) continue;

                    int goal2 = findGoalIndex(tile2, goalState);

                    // Si la ficha 2 también pertenece a esta fila y están en orden inverso al objetivo, hay un conflicto
                    if (goal2 / boardDim == row && goal1 > goal2) {
                        conflicts++;
                    }
                }
            }
        }

        // 2. Detección de conflictos por columnas
        for (int col = 0; col < boardDim; col++) {
            for (int i = 0; i < boardDim; i++) {
                int tile1 = state[i * boardDim + col];

                // Ignorar el espacio vacío (0)
                if (tile1 == 0) continue;

                int goal1 = findGoalIndex(tile1, goalState);

                // Si la ficha 1 no pertenece a esta columna, se omite
                if (goal1 % boardDim != col) continue;

                // Comparar con las demás fichas en la misma columna
                for (int j = i + 1; j < boardDim; j++) {
                    int tile2 = state[j * boardDim + col];

                    if (tile2 == 0) continue;

                    int goal2 = findGoalIndex(tile2, goalState);

                    // Si la ficha 2 también pertenece a esta columna y están en orden inverso al objetivo, hay un conflicto
                    if (goal2 % boardDim == col && goal1 > goal2) {
                        conflicts++;
                    }
                }
            }
        }

        // Cada conflicto requiere al menos un rodeo de 2 movimientos (uno que se aparta y otro que pasa)
        return distance + 2 * conflicts;
    }

    /**
     * Busca el índice en el que se encuentra una ficha específica dentro del estado objetivo.
     * 
     * @param tile El número de la ficha a buscar.
     * @param goalState El arreglo del estado objetivo.
     * @return El índice de la ficha en el estado objetivo.
     */
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