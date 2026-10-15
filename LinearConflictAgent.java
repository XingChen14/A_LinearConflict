import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * Agente JADE (LinearConflictAgent) que recibe solicitudes para resolver 
 * un puzzle (como el 8-puzzle) mediante el algoritmo A* utilizando la 
 * heurística de Conflicto Lineal (Linear Conflict).
 */
public class LinearConflictAgent extends Agent {

    private AStar astar; // Instancia del algoritmo de búsqueda A*

    /**
     * Método de inicialización del agente JADE.
     * Configura un comportamiento cíclico para escuchar mensajes ACL entrantes.
     */
    @Override
    protected void setup() {
        System.out.println("Linear Conflict Agent iniciado.");

        // Comportamiento cíclico que se ejecuta de manera continua para procesar peticiones
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                // Si no hay mensajes en la bandeja de entrada, bloquea el comportamiento hasta nuevo aviso
                if (msg == null) {
                    block();
                    return;
                }

                // Filtrar únicamente los mensajes con performativa REQUEST
                if (msg.getPerformative() != ACLMessage.REQUEST) {
                    return;
                }

                try {
                    // El contenido del mensaje debe tener el formato: "estadoInicial | estadoObjetivo"
                    // Ejemplo: "1,2,3,4,5,6,7,8,0|1,2,3,4,5,6,7,0,8"
                    String[] boards = msg.getContent().split("\\|");

                    int[] initialState = parseBoard(boards[0]);
                    int[] goalState = parseBoard(boards[1]);

                    // Instanciar la heurística LinearConflict para un tablero de dimensión 3x3
                    Heuristic heuristic = new LinearConflict(3);
                    astar = new AStar(goalState, heuristic);

                    // Ejecutar la búsqueda A*
                    boolean solved = astar.search(initialState);

                    // Crear y enviar la respuesta de vuelta al emisor
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(buildResponse(solved));

                    send(reply);

                } catch (Exception e) {
                    // En caso de error (formato inválido, etc.), responder con FAILURE
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Error: " + e.getMessage());
                    send(reply);
                }
            }
        });
    }

    /**
     * Convierte una cadena de texto separada por comas en un arreglo de enteros.
     * 
     * @param value Cadena de números (ej: "1, 2, 3, 0, 4...").
     * @return Arreglo primitivo int[].
     */
    private int[] parseBoard(String value) {
        return Arrays.stream(value.trim().split(","))
            .map(String::trim)
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    /**
     * Construye el mensaje de respuesta detallado con las métricas del algoritmo 
     * y el camino paso a paso si el problema fue resuelto.
     * 
     * @param solved Indica si el puzzle fue resuelto con éxito.
     * @return Una cadena formateada con los resultados.
     */
    private String buildResponse(boolean solved) {
        StringBuilder result = new StringBuilder();

        result.append("agent=LinearConflict\n");
        result.append("solved=").append(solved).append("\n");
        result.append("expanded=").append(astar.getNodesExpanded()).append("\n");
        result.append("timeMs=").append(astar.getRunningTime()).append("\n");
        result.append("depth=").append(astar.getSolutionDepth()).append("\n");

        // Si se encontró solución, adjuntar cada paso del camino
        if (solved) {
            HashMap<Integer, int[]> path = astar.getStatesToGoal();

            if (path != null) {
                for (Map.Entry<Integer, int[]> entry : path.entrySet()
                        .stream()
                        .sorted(Map.Entry.comparingByKey())
                        .collect(Collectors.toList())) {

                    result.append("Paso ")
                          .append(entry.getKey())
                          .append(": ")
                          .append(Arrays.toString(entry.getValue()))
                          .append("\n");
                }
            }
        }

        return result.toString();
    }
}