package com.sma.agents;

import java.util.HashMap;
import java.util.Map;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AgenteClasificador extends Agent {
    private Map<String, Double> altaProb = new HashMap<>();
    private Map<String, Double> bajaProb = new HashMap<>();

    protected void setup() {
        System.out.println("CLASIFICADOR: Agente iniciado. Esperando 10 segundos para sincronizar con el repositorio...");
        
        // Fase 1: Buscar el repositorio después de 10 segundos
        addBehaviour(new jade.core.behaviours.WakerBehaviour(this, 10000) {
            protected void onWake() {
                DFAgentDescription template = new DFAgentDescription();
                ServiceDescription sd = new ServiceDescription();
                sd.setType("modelo-datos");
                template.addServices(sd);
                try {
                    DFAgentDescription[] result = DFService.search(myAgent, template);
                    if (result.length > 0) {
                        ACLMessage req = new ACLMessage(ACLMessage.REQUEST);
                        req.addReceiver(result[0].getName());
                        req.setContent("GET_MODEL");
                        send(req);
                        System.out.println("CLASIFICADOR: Solicitando modelo al repositorio...");
                    } else {
                        System.out.println("CLASIFICADOR ERROR: No se encontró el repositorio en el DF. ¿Está encendido?");
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }
        });

        // Fase 2: Escuchar peticiones (esto se queda igual)
        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.INFORM && msg.getContent().contains(":")) {
                        cargarModelo(msg.getContent());
                    } else if (msg.getPerformative() == ACLMessage.REQUEST) {
                        String resultado = inferenciaBayesiana(msg.getContent());
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(resultado);
                        send(reply);
                    }
                } else { block(); }
            }
        });
    }

    private void cargarModelo(String data) {
        for (String entry : data.split(",")) {
            String[] parts = entry.split(":");
            if (parts[1].equals("alta")) altaProb.put(parts[0], Double.parseDouble(parts[2]));
            else bajaProb.put(parts[0], Double.parseDouble(parts[2]));
        }
        System.out.println("CLASIFICADOR: Conocimiento cargado. Listo para clasificar.");
    }

    private String inferenciaBayesiana(String texto) {
        double scoreAlta = -0.7; // Log Prior
        double scoreBaja = -0.7;
        for (String word : texto.split(" ")) {
            scoreAlta += altaProb.getOrDefault(word, -4.0); // Log Likelihood
            scoreBaja += bajaProb.getOrDefault(word, -4.0);
        }
        return (scoreAlta > scoreBaja) ? "ALTA PRIORIDAD" : "BAJA PRIORIDAD";
    }
}