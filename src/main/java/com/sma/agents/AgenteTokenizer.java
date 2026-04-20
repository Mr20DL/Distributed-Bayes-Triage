package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

public class AgenteTokenizer extends Agent {
    protected void setup() {
        System.out.println("TOKENIZER: Agente listo.");
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // Lógica simple: limpiar texto y separar por espacios
                    String content = msg.getContent().toLowerCase().replaceAll("[^a-z ]", "");
                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(content);
                    send(reply);
                    System.out.println("TOKENIZER: Texto procesado: " + content);
                } else {
                    block();
                }
            }
        });
    }
}