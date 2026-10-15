
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class TestAgent extends Agent {

    @Override
    protected void setup() {
        System.out.println("Agente de prueba iniciado.");

        int[] initialState = {4, 1, 0, 2, 6, 3, 7, 5, 8};
        int[] goalState = {1, 2, 3, 4, 5, 6, 7, 8, 0};

        // Enviar solicitud al agente LinearConflictAgent
        ACLMessage request = new ACLMessage(ACLMessage.REQUEST);
        request.addReceiver(
            new AID("solver", AID.ISLOCALNAME)
        );

        request.setContent(
            toText(initialState) + "|" + toText(goalState)
        );

        send(request);

        System.out.println("Solicitud enviada al agente solver.");

        // Esperar y mostrar la respuesta
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage reply = receive();

                if (reply == null) {
                    block();
                    return;
                }

                System.out.println("\n===== RESULTADO =====");
                System.out.println("Performativa: "
                    + ACLMessage.getPerformative(reply.getPerformative()));
                // Imprimimos el contenido completo que contiene las métricas y los estados paso a paso
                System.out.println(reply.getContent());

                if (reply.getPerformative() == ACLMessage.INFORM
                        || reply.getPerformative() == ACLMessage.FAILURE) {
                    doDelete();
                }
            }
        });
    }


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
