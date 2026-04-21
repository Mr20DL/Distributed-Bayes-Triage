package com.sma.agents;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Scanner;

public class AgenteInterfaz extends Agent {

    /* Estado compartido entre el hilo de stdin y el behaviour JADE */
    private volatile String lastTokens;
    private volatile String lastResult;
    private volatile boolean waitingFeedback;

    protected void setup() {
        System.out.println("INTERFAZ: Iniciada. Escriba el reporte del bug:");

        new Thread(() -> {
            Scanner sc = new Scanner(System.in);
            while (true) {
                String line = sc.nextLine().trim();
                if (line.isEmpty()) continue;

                if (waitingFeedback) {
                    handleUserFeedback(line);
                } else {
                    ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
                    msg.addReceiver(new AID("Tokenizer", AID.ISLOCALNAME));
                    msg.setContent(line);
                    send(msg);
                }
            }
        }).start();

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg == null) { block(); return; }

                String sender = msg.getSender().getLocalName();

                if (sender.equals("Tokenizer")) {
                    lastTokens = msg.getContent();
                    ACLMessage fwd = new ACLMessage(ACLMessage.REQUEST);
                    fwd.addReceiver(new AID("Clasificador", AID.ISLOCALNAME));
                    fwd.setContent(lastTokens);
                    send(fwd);
                } else if (sender.equals("Clasificador")) {
                    lastResult = msg.getContent();
                    System.out.println(">>> RESULTADO DEL TRIAJE: " + lastResult);
                    System.out.println("    Es correcta la clasificacion? (S/N):");
                    waitingFeedback = true;
                } else if (msg.getPerformative() == ACLMessage.CONFIRM) {
                    System.out.println("INTERFAZ: Repositorio confirmo la correccion (" + msg.getContent() + ")");
                    System.out.println("\nIngrese otro bug:");
                }
            }
        });
    }

    private void handleUserFeedback(String input) {
        waitingFeedback = false;
        String upper = input.toUpperCase();

        if (upper.equals("S") || upper.equals("SI")) {
            System.out.println("INTERFAZ: Clasificacion aceptada.");
            System.out.println("\nIngrese otro bug:");
            return;
        }

        if (upper.equals("N") || upper.equals("NO")) {
            String correccion = lastResult.contains("ALTA") ? "BAJA" : "ALTA";
            String payload = "UPDATE:" + lastTokens + ":" + correccion;

            ACLMessage fb = new ACLMessage(ACLMessage.INFORM);
            fb.addReceiver(new AID("Repositorio", AID.ISLOCALNAME));
            fb.setContent(payload);
            send(fb);
            System.out.println("INTERFAZ: Enviando correccion al Repositorio -> " + correccion);
            return;
        }

        System.out.println("    Responda S o N:");
        waitingFeedback = true;
    }
}