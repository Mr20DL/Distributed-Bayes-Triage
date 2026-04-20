package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

public class AgenteRepositorio extends Agent {
    // Modelo simulado: palabra;prioridad;log_probabilidad
    private String modeloProbabilidades = "error:alta:-1.2,login:alta:-1.5,lento:baja:-1.1,caida:alta:-0.5,servidor:alta:-0.8";

    protected void setup() {
        // Registro en el Directory Facilitator (Páginas Amarillas)
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("modelo-datos");
        sd.setName("JADE-bug-triaje");
        dfd.addServices(sd);
        
        try {
            DFService.register(this, dfd);
            System.out.println("REPOSITORIO: Registrado en el DF y listo para servir el modelo.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    if (msg.getPerformative() == ACLMessage.REQUEST) {
                        System.out.println("REPOSITORIO: Petición de modelo recibida de " + msg.getSender().getLocalName());
                        ACLMessage reply = msg.createReply();
                        reply.setPerformative(ACLMessage.INFORM);
                        reply.setContent(modeloProbabilidades);
                        send(reply);
                    } else if (msg.getPerformative() == ACLMessage.INFORM) {
                        System.out.println("REPOSITORIO: Feedback recibido. Simulando reentrenamiento...");
                    }
                } else {
                    block();
                }
            }
        });
    }

    protected void takeDown() {
        try { DFService.deregister(this); } catch (Exception e) {}
    }
}