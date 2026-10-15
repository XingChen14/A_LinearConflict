//javac -cp "jade/lib/jade.jar" *.java
//java -cp ".;jade/lib/jade.jar" jade.Boot -gui -agents solver:LinearConflictAgent;tester:TestAgent

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

/**
 * TestAgent es un agente de prueba encargado de enviar un estado inicial
 * y un estado objetivo (goal state) a un agente solucionador (solver)
 * mediante JADE, y de esperar la respuesta con el resultado.
 */
public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agente de prueba iniciado.");

        // Definición de los estados del puzzle (por ejemplo, un rompecabezas de 8 piezas)
        int[] initialState = {4, 1, 0, 2, 6, 3, 7, 5, 8}; // 0 representa el espacio vacío
        int[] goalState = {1, 2, 3, 4, 5, 6, 7, 8, 0};

        // Crear un mensaje ACL con performativa REQUEST para solicitar un cálculo/servicio
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        
        // Configurar el destinatario del mensaje (un agente con el nombre local "solver")
        request.addReceiver(
            new AID("solver", AID.ISLOCALNAME)
        );

        // Establecer el contenido del mensaje, combinando el estado inicial y el objetivo
        // separados por el carácter barra vertical ("|")
        request.setContent(
            toText(initialState) + "|" + toText(goalState)
        );

        // Enviar el mensaje a través de la infraestructura de JADE
        send(request);

        System.out.println("Solicitud enviada al agente solver.");

        // Agregar un comportamiento cíclico (CyclicBehaviour) para escuchar continuamente las respuestas
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                // Intentar recibir un mensaje dirigido a este agente
                ACLMessage reply = receive();

                // Si no hay mensajes en la cola, bloquear temporalmente el comportamiento hasta que llegue uno nuevo
                if (reply == null) {
                    block();
                    return;
                }

                // Imprimir la cabecera y la información básica de la respuesta recibida
                System.out.println("\n===== RESULTADO =====");
                System.out.println("Performativa: "
                    + ACLMessage.getPerformative(reply.getPerformative()));
                
                // Imprimir el contenido completo que contiene las métricas y los estados paso a paso
                System.out.println(reply.getContent());

                // Si el agente solucionador confirma el éxito (INFORM) o el fallo (FAILURE) de la tarea,
                // se procede a eliminar este agente de prueba ya que su ciclo ha terminado.
                if (reply.getPerformative() == ACLMessage.INFORM
                        || reply.getPerformative() == ACLMessage.FAILURE) {
                    doDelete();
                }
            }
        });
    }

    /**
     * Método auxiliar para convertir un arreglo de enteros en una cadena de texto
     * separada por comas (ej. "4,1,0,2,6,3,7,5,8").
     * 
     * @param state Arreglo de enteros que representa el estado del puzzle.
     * @return Una cadena con los elementos separados por comas.
     */
    private String toText(int[] state) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < state.length; i++) {
            if (i > 0) {
                result.append(",");
            }
            result.append(state[i]);
        }

        return result.toString();
    }
}