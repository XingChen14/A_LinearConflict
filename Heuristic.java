/**
 * Clase abstracta que representa una función heurística para el algoritmo A*.
 * Las subclases implementarán la lógica específica de estimación (por ejemplo, 
 * Distancia Manhattan, Distancia Euclidiana o Fichas Fuera de Lugar).
 */
public abstract class Heuristic {

    protected int boardDim; // Dimensión del tablero (ej. 3 para un tablero de 3x3)

    /**
     * Constructor de la clase Heuristic.
     * 
     * @param boardDim Dimensión del tablero (número de filas o columnas).
     */
    public Heuristic(int boardDim) {
        this.boardDim = boardDim;
    }

    /**
     * Método abstracto para calcular el valor heurístico estimado desde 
     * el estado actual hasta el estado objetivo.
     * 
     * @param state El estado actual del tablero representado como un arreglo.
     * @param goalState El estado objetivo o meta deseado.
     * @return El costo estimado restante para llegar a la meta.
     */
    public abstract int heuristic(int[] state, int[] goalState);
}