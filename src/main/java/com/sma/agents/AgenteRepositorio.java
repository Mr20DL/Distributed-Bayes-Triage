package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/*
 * Agente que sirve el modelo probabilistico via DF.
 * Corre en la plataforma remota (Container-1).
 *
 * Protocolo:
 *   REQUEST "GET_MODEL"              -> INFORM con modelo serializado
 *   INFORM  "UPDATE:tokens:prioridad" -> CONFIRM con ack
 */
public class AgenteRepositorio extends Agent {

    /*
     * Modelo naive bayes serializado.
     * Formato: palabra:clase:log_prob
     * El clasificador parsea esto directamente con split.
     */
    private String modelo = buildModelo();

    private int nRequests;
    private int nUpdates;

    private static String buildModelo() {
        /*
         * Log-probabilidades condicionales P(palabra|clase).
         * Valores mas cercanos a 0 = mayor probabilidad.
         */
        String[] entries = {
            /* alta prioridad */
            "error:alta:-0.16",
            "crash:alta:-0.11",
            "fallo:alta:-0.22",
            "critico:alta:-0.05",
            "excepcion:alta:-0.29",
            "timeout:alta:-0.36",
            "memoria:alta:-0.22",
            "seguridad:alta:-0.13",
            "caida:alta:-0.11",
            "servidor:alta:-0.29",
            "conexion:alta:-0.36",
            "null:alta:-0.22",
            "login:alta:-0.43",
            "datos:alta:-0.51",
            "base:alta:-0.60",
            /* baja prioridad */
            "lento:baja:-0.36",
            "interfaz:baja:-0.22",
            "color:baja:-0.11",
            "fuente:baja:-0.16",
            "boton:baja:-0.29",
            "alineacion:baja:-0.16",
            "texto:baja:-0.51",
            "visual:baja:-0.22"
        };
        return String.join(",", entries);
    }

    @Override
    protected void setup() {
        if (!registrarEnDF()) {
            doDelete();
            return;
        }
        addBehaviour(new Dispatcher());
        System.out.println("REPOSITORIO: listo, " + modelo.split(",").length + " entradas cargadas");
    }

    /* Registra el servicio "modelo-datos" en el DF para que el clasificador nos encuentre */
    private boolean registrarEnDF() {
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());

        ServiceDescription sd = new ServiceDescription();
        sd.setType("modelo-datos");
        sd.setName("JADE-bug-triaje");
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("REPOSITORIO: registrado en DF [modelo-datos]");
            return true;
        } catch (FIPAException e) {
            System.err.println("REPOSITORIO: fallo registro DF - " + e.getMessage());
            return false;
        }
    }

    /*
     * Behaviour principal. Despacha segun performativa:
     *   REQUEST -> entregar modelo
     *   INFORM  -> procesar feedback
     */
    private class Dispatcher extends CyclicBehaviour {
        @Override
        public void action() {
            ACLMessage msg = receive();
            if (msg == null) {
                block();
                return;
            }

            switch (msg.getPerformative()) {
            case ACLMessage.REQUEST:
                handleGetModel(msg);
                break;
            case ACLMessage.INFORM:
                handleFeedback(msg);
                break;
            default:
                System.out.println("REPOSITORIO: performativa inesperada de "
                    + msg.getSender().getLocalName());
            }
        }
    }

    /* Responde al clasificador con el modelo serializado */
    private void handleGetModel(ACLMessage msg) {
        String from = msg.getSender().getLocalName();

        if (!"GET_MODEL".equals(msg.getContent())) {
            System.out.println("REPOSITORIO: request desconocido de " + from
                + ": " + msg.getContent());
            return;
        }

        ACLMessage reply = msg.createReply();
        reply.setPerformative(ACLMessage.INFORM);
        reply.setContent(modelo);
        send(reply);

        nRequests++;
        System.out.println("REPOSITORIO: modelo enviado a " + from + " (#" + nRequests + ")");
    }

    /*
     * Procesa feedback del usuario cuando la clasificacion fue incorrecta.
     * Formato esperado: "UPDATE:token1 token2:PRIORIDAD_CORRECTA"
     */
    private void handleFeedback(ACLMessage msg) {
        String content = msg.getContent();
        String from = msg.getSender().getLocalName();

        if (content == null || !content.startsWith("UPDATE:")) {
            System.out.println("REPOSITORIO: inform generico de " + from + ": " + content);
            return;
        }

        String payload = content.substring("UPDATE:".length());
        String[] parts = payload.split(":");

        if (parts.length >= 2) {
            nUpdates++;
            System.out.println("REPOSITORIO: feedback #" + nUpdates + " de " + from);
            System.out.println("  tokens:    " + parts[0]);
            System.out.println("  correccion: " + parts[1]);
        } else {
            System.out.println("REPOSITORIO: formato update invalido: " + content);
        }

        /* Confirmar al emisor */
        ACLMessage ack = msg.createReply();
        ack.setPerformative(ACLMessage.CONFIRM);
        ack.setContent("UPDATE_OK:" + nUpdates);
        send(ack);
    }

    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
        } catch (FIPAException e) {
            /* no hay mucho que hacer aqui */
        }
        System.out.println("REPOSITORIO: terminado (" + nRequests + " requests, "
            + nUpdates + " updates)");
    }
}