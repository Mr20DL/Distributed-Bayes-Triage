package com.sma.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Scanner;

public class AgenteInterfaz extends Agent {
    protected void setup() {
        System.out.println("INTERFAZ: Iniciada. Escriba el reporte del bug:");
        
        // Hilo separado para no bloquear el agente con la consola
        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String bug = sc.nextLine();
                ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                msg.addReceiver(new AID("Tokenizer", AID.ISLOCALNAME));
                msg.setContent(bug);
                send(msg);
            }
        }).start();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getSender().getLocalName().equals("Tokenizer")) {
                        // Enviamos al clasificador
                        ACLMessage nMsg = new ACLMessage(ACLMessage.REQUEST);
                        nMsg.addReceiver(new AID("Clasificador", AID.ISLOCALNAME));
                        nMsg.setContent(msg.getContent());
                        send(nMsg);
                    } else if (msg.getSender().getLocalName().equals("Clasificador")) {
                        System.out.println(">>> RESULTADO DEL TRIAJE: " + msg.getContent());
                        System.out.println("\nIngrese otro bug:");
                    }
                } else { block(); }
            }
        });
    }
}