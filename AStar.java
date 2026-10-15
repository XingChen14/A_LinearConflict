import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;

/**
 * Implementación del algoritmo de búsqueda A* (A-Star) para la resolución
 * de problemas de espacio de estados (como el N-Puzzle).
 */
public class AStar {

    private final Heuristic heuristic; // Heurística utilizada para estimar el costo restante
    private final int[] goalState;     // Estado objetivo que se desea alcanzar

    private HashMap<Integer, int[]> statesToGoal; // Almacena el camino final paso a paso (Paso -> Estado)
    private int nodesExpanded;                   // Contador de nodos expandidos durante la búsqueda
    private long runningTime;                    // Tiempo total de ejecución en milisegundos

    /**
     * Constructor del algoritmo A*.
     * 
     * @param goalState Estado meta u objetivo.
     * @param heuristic Instancia de la heurística a utilizar.
     */
    public AStar(int[] goalState, Heuristic heuristic) {
        this.goalState = goalState.clone();
        this.heuristic = heuristic;
        this.statesToGoal = new HashMap<>();
    }

    /**
     * Clase interna que representa un nodo dentro del grafo de búsqueda de A*.
     */
    private static class Node {
        int[] state; // Representación numérica del estado actual
        int g;       // Costo real desde el inicio hasta este nodo
        int f;       // Costo total estimado (f = g + h)
        String key;  // Representación en cadena del estado para uso en colecciones

        Node(int[] state, int g, int f) {
            this.state = state.clone();
            this.g = g;
            this.f = f;
            this.key = Arrays.toString(state);
        }
    }

    /**
     * Ejecuta la búsqueda A* desde un estado inicial dado hasta el estado objetivo.
     * 
     * @param initialState Estado inicial del problema.
     * @return true si se encontró una solución, false en caso contrario.
     */
    public boolean search(int[] initialState) {
        long start = System.currentTimeMillis();

        statesToGoal.clear();
        nodesExpanded = 0;

        String startKey = Arrays.toString(initialState);
        String goalKey = Arrays.toString(goalState);

        // Cola de prioridad (Frontera) ordenada por el costo f de menor a mayor,
        // y en caso de empate, por el costo g de menor a mayor.
        PriorityQueue<Node> frontier = new PriorityQueue<>(
            Comparator.comparingInt((Node n) -> n.f)
                      .thenComparingInt(n -> n.g)
        );

        HashMap<String, Integer> cost = new HashMap<>();     // Costo mínimo conocido (g) para llegar a cada estado
        HashMap<String, String> parent = new HashMap<>();    // Mapeo para reconstruir el camino (Hijo -> Padre)
        HashSet<String> explored = new HashSet<>();          // Conjunto de estados ya explorados/visitados

        // Inicialización del nodo de arranque
        cost.put(startKey, 0);
        parent.put(startKey, null);

        frontier.add(new Node(
            initialState, 0,
            heuristic.heuristic(initialState, goalKey != null ? goalState : goalState) // Estimación inicial h
        ));

        // Bucle principal de exploración
        while (!frontier.isEmpty()) {
            Node current = frontier.poll();

            // Si el costo acumulado actual es mayor al costo registrado, se descarta (nodo obsoleto)
            if (current.g != cost.getOrDefault(current.key, Integer.MAX_VALUE)) {
                continue;
            }

            // Si el estado ya fue explorado, se omite
            if (!explored.add(current.key)) {
                continue;
            }

            nodesExpanded++;

            // Prueba de meta: si llegamos al estado objetivo, reconstruimos el camino y terminamos
            if (current.key.equals(goalKey)) {
                rebuildPath(parent, goalKey);
                runningTime = System.currentTimeMillis() - start;
                return true;
            }

            // Generar y procesar los estados vecinos (hijos)
            for (int[] child : getChildren(current.state)) {
                String childKey = Arrays.toString(child);
                int newCost = current.g + 1; // Asumimos un costo uniforme de 1 por movimiento

                // Si encontramos un camino más corto hacia el hijo, lo actualizamos
                if (newCost < cost.getOrDefault(childKey, Integer.MAX_VALUE)) {
                    cost.put(childKey, newCost);
                    parent.put(childKey, current.key);
                    explored.remove(childKey); // Permitir reexploración si se halla un camino mejor

                    int h = heuristic.heuristic(child, goalState);

                    // Agregar el nuevo nodo a la frontera con su f = g + h
                    frontier.add(new Node(
                        child, newCost, newCost + h
                    ));
                }
            }
        }

        runningTime = System.currentTimeMillis() - start;
        return false; // No se encontró solución
    }

    /**
     * Genera todos los estados sucesores (hijos válidos) moviendo la celda vacía (0).
     * 
     * @param state Estado actual del tablero.
     * @return Lista de arreglos que representan los estados hijos válidos.
     */
    private List<int[]> getChildren(int[] state) {
        List<int[]> children = new ArrayList<>();
        int dim = (int) Math.sqrt(state.length); // Asume un tablero cuadrado (ej. 3x3 para 8-puzzle)
        int empty = -1;

        // Localizar la posición del espacio vacío (representado por 0)
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

        // Movimientos posibles en la cuadrícula: Arriba (-dim), Abajo (dim), Izquierda (-1), Derecha (1)
        int[] moves = {-dim, dim, -1, 1};

        for (int move : moves) {
            int next = empty + move;

            // Validar límites de la cuadrícula
            if (next < 0 || next >= state.length) continue;
            // Evitar saltos de línea inválidos a la izquierda o derecha
            if (move == -1 && col == 0) continue;
            if (move == 1 && col == dim - 1) continue;

            // Realizar el intercambio (swap) entre el espacio vacío y la ficha adyacente
            int[] child = state.clone();
            child[empty] = child[next];
            child[next] = 0;

            children.add(child);
        }

        return children;
    }

    /**
     * Reconstruye el camino desde el estado objetivo hasta el estado inicial 
     * utilizando el mapa de padres y lo almacena de manera secuencial.
     * 
     * @param parent Mapa que relaciona cada estado hijo con su padre.
     * @param goalKey Clave en cadena del estado objetivo.
     */
    private void rebuildPath(HashMap<String, String> parent, String goalKey) {
        List<int[]> reversed = new ArrayList<>();
        String current = goalKey;

        // Retroceder desde la meta hasta el inicio mediante los padres
        while (current != null) {
            reversed.add(parseState(current));
            current = parent.get(current);
        }

        // Invertir la lista para que vaya del inicio a la meta
        Collections.reverse(reversed);

        for (int i = 0; i < reversed.size(); i++) {
            statesToGoal.put(i, reversed.get(i).clone());
        }
    }

    /**
     * Convierte una representación en cadena de un arreglo de enteros de vuelta a un arreglo int[].
     * 
     * @param key Cadena que representa el estado (ej: "[1, 2, 3, ...]").
     * @return Arreglo de enteros parseado.
     */
    private int[] parseState(String key) {
        String cleaned = key.substring(1, key.length() - 1);
        String[] parts = cleaned.split(",\\s*");
        int[] state = new int[parts.length];

        for (int i = 0; i < parts.length; i++) {
            state[i] = Integer.parseInt(parts[i]);
        }

        return state;
    }

    /**
     * Obtiene una copia del mapa de estados que conforman la solución paso a paso.
     * 
     * @return Mapa con los pasos ordenados desde el estado inicial hasta la meta.
     */
    public HashMap<Integer, int[]> getStatesToGoal() {
        HashMap<Integer, int[]> copy = new HashMap<>();

        statesToGoal.forEach(
            (step, state) -> copy.put(step, state.clone())
        );

        return copy;
    }

    /**
     * Devuelve la profundidad de la solución encontrada (número de movimientos).
     */
    public int getSolutionDepth() {
        return Math.max(0, statesToGoal.size() - 1);
    }

    /**
     * Devuelve el número total de nodos expandidos durante la búsqueda.
     */
    public int getNodesExpanded() {
        return nodesExpanded;
    }

    /**
     * Devuelve el tiempo transcurrido en la ejecución de la búsqueda.
     */
    public long getRunningTime() {
        return runningTime;
    }
}