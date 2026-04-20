package com.sma.agents;

import jade.core.Agent;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.lang.acl.ACLMessage;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AgenteInterfaz extends Agent {

    private AID tokenizerAID;

    @Override
    protected void setup() {
        System.out.println("[INTERFAZ] Iniciado. Esperando al Tokenizer...");

        // 1. BÚSQUEDA SILENCIOSA: Se detiene al encontrar al Tokenizer
        addBehaviour(new TickerBehaviour(this, 1000) {
            @Override
            protected void onTick() {
                buscarTokenizer();
                if (tokenizerAID != null) {
                    stop(); // Mata el comportamiento para que no imprima NADA más
                }
            }
        });

        // 2. RECIBIR RESPUESTAS
        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    // El \r limpia un poco la línea actual en algunas consolas
                    System.out.println("\n[INTERFAZ] Tokens recibidos: " + msg.getContent());
                    System.out.print("Ingrese bug: ");
                } else {
                    block();
                }
            }
        });

        // 3. HILO DE ENTRADA (BLOQUEADO HASTA QUE EXISTA EL TOKENIZER)
        new Thread(() -> {
            try {
                BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
                while (true) {
                    if (tokenizerAID != null) {
                        String texto = reader.readLine();
                        if (texto != null && !texto.trim().isEmpty()) {
                            enviar(texto);
                        }
                    } else {
                        Thread.sleep(500); // Espera sin imprimir nada
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void enviar(String texto) {
        ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
        msg.addReceiver(tokenizerAID);
        msg.setContent(texto);
        send(msg);
    }
}