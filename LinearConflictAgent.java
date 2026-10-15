import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class LinearConflictAgent extends Agent {

    private AStar astar;

    @Override
    protected void setup() {
        System.out.println("Linear Conflict Agent iniciado.");

        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();

                if (msg == null) {
                    block();
                    return;
                }

                if (msg.getPerformative() != ACLMessage.REQUEST) {
                    return;
                }

                try {
                    String[] boards = msg.getContent().split("\\|");

                    int[] initialState = parseBoard(boards[0]);
                    int[] goalState = parseBoard(boards[1]);

                    Heuristic heuristic = new LinearConflict(3);
                    astar = new AStar(goalState, heuristic);

                    boolean solved = astar.search(initialState);

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(buildResponse(solved));

                    send(reply);

                } catch (Exception e) {
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.FAILURE);
                    reply.setContent("Error: " + e.getMessage());
                    send(reply);
                }
            }
        });
    }

    private int[] parseBoard(String value) {
        return Arrays.stream(value.trim().split(","))
            .map(String::trim)
            .mapToInt(Integer::parseInt)
            .toArray();
    }

    private String buildResponse(boolean solved) {
        StringBuilder result = new StringBuilder();

        result.append("agent=LinearConflict\n");
        result.append("solved=").append(solved).append("\n");
        result.append("expanded=").append(astar.getNodesExpanded()).append("\n");
        result.append("timeMs=").append(astar.getRunningTime()).append("\n");
        result.append("depth=").append(astar.getSolutionDepth()).append("\n");

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