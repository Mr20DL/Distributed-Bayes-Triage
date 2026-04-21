package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;

/**
 * AgenteRepositorio — Nodo de Datos (Plataforma Remota)
 * 
 * Responsabilidades:
 * 1. Registrarse en el Directory Facilitator (DF) como servicio "modelo-datos"
 * 2. Responder REQUEST "GET_MODEL" con el modelo Naïve Bayes serializado
 * 3. Recibir INFORM "UPDATE:tokens:prioridad" para simular reentrenamiento
 * 4. Desregistrarse del DF al apagarse
 * 
 * Protocolo FIPA-ACL utilizado:
 * - REQUEST  → Solicitud del modelo por parte del Clasificador
 * - INFORM   → Entrega del modelo / Recepción de feedback
 * - CONFIRM  → Confirmación de actualización al Interfaz
 * 
 * @author Ever (nesodev) — Grupo 02, Software Inteligente, UNMSM FISI
 */
public class AgenteRepositorio extends Agent {

    // ═══════════════════════════════════════════════════════════
    // Modelo Naïve Bayes hardcodeado (20 palabras clave)
    // Formato: palabra:prioridad:log_probabilidad
    // El AgenteClasificador parsea exactamente este formato.
    // ═══════════════════════════════════════════════════════════
    private String modeloProbabilidades = String.join(",",
        // --- Palabras de ALTA prioridad (log-prob negativas cercanas a 0 = alta probabilidad) ---
        "error:alta:-0.16",        // P(error|alta) ≈ 0.85
        "crash:alta:-0.11",        // P(crash|alta) ≈ 0.90
        "fallo:alta:-0.22",        // P(fallo|alta) ≈ 0.80
        "critico:alta:-0.05",      // P(critico|alta) ≈ 0.95
        "excepcion:alta:-0.29",    // P(excepcion|alta) ≈ 0.75
        "timeout:alta:-0.36",      // P(timeout|alta) ≈ 0.70
        "memoria:alta:-0.22",      // P(memoria|alta) ≈ 0.80
        "seguridad:alta:-0.13",    // P(seguridad|alta) ≈ 0.88
        "caida:alta:-0.11",        // P(caida|alta) ≈ 0.90
        "servidor:alta:-0.29",     // P(servidor|alta) ≈ 0.75
        "conexion:alta:-0.36",     // P(conexion|alta) ≈ 0.70
        "null:alta:-0.22",         // P(null|alta) ≈ 0.80
        "login:alta:-0.43",        // P(login|alta) ≈ 0.65
        "datos:alta:-0.51",        // P(datos|alta) ≈ 0.60
        "base:alta:-0.60",         // P(base|alta) ≈ 0.55
        // --- Palabras de BAJA prioridad ---
        "lento:baja:-0.36",        // P(lento|baja) ≈ 0.70
        "interfaz:baja:-0.22",     // P(interfaz|baja) ≈ 0.80
        "color:baja:-0.11",        // P(color|baja) ≈ 0.90
        "fuente:baja:-0.16",       // P(fuente|baja) ≈ 0.85
        "boton:baja:-0.29",        // P(boton|baja) ≈ 0.75
        "alineacion:baja:-0.16",   // P(alineacion|baja) ≈ 0.85
        "texto:baja:-0.51",        // P(texto|baja) ≈ 0.60
        "visual:baja:-0.22"        // P(visual|baja) ≈ 0.80
    );

    private int solicitudesAtendidas = 0;
    private int updatesRecibidos = 0;

    // ═══════════════════════════════════════════════════════════
    // SETUP: Registro en Directory Facilitator + Comportamiento
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void setup() {
        System.out.println("══════════════════════════════════════════════════");
        System.out.println("  REPOSITORIO: Iniciando Agente Repositorio...");
        System.out.println("══════════════════════════════════════════════════");

        // ─── Paso 1: Registro en el DF (Páginas Amarillas) ───
        DFAgentDescription dfd = new DFAgentDescription();
        dfd.setName(getAID());
        ServiceDescription sd = new ServiceDescription();
        sd.setType("modelo-datos");          // Tipo que busca el Clasificador
        sd.setName("JADE-bug-triaje");       // Nombre descriptivo del servicio
        dfd.addServices(sd);

        try {
            DFService.register(this, dfd);
            System.out.println("  REPOSITORIO: ✅ Registrado en DF como servicio 'modelo-datos'");
            System.out.println("  REPOSITORIO: 📊 Modelo cargado con " 
                + modeloProbabilidades.split(",").length + " entradas de probabilidad");
        } catch (FIPAException e) {
            System.err.println("  REPOSITORIO: ❌ Error al registrar en DF: " + e.getMessage());
            e.printStackTrace();
            doDelete();
            return;
        }

        // ─── Paso 2: Comportamiento cíclico para atender solicitudes ───
        addBehaviour(new CyclicBehaviour(this) {
            @Override
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    switch (msg.getPerformative()) {
                        case ACLMessage.REQUEST:
                            manejarRequest(msg);
                            break;
                        case ACLMessage.INFORM:
                            manejarUpdate(msg);
                            break;
                        default:
                            System.out.println("  REPOSITORIO: ⚠️ Performativa no esperada: "
                                + ACLMessage.getPerformative(msg.getPerformative())
                                + " de " + msg.getSender().getLocalName());
                    }
                } else {
                    block(); // Duerme hasta que llegue un mensaje → eficiente
                }
            }
        });

        System.out.println("  REPOSITORIO: 🟢 Agente activo. Esperando solicitudes de modelo...");
        System.out.println("══════════════════════════════════════════════════\n");
    }

    // ═══════════════════════════════════════════════════════════
    // HANDLER: REQUEST — Entrega del modelo al Clasificador
    // ═══════════════════════════════════════════════════════════
    private void manejarRequest(ACLMessage msg) {
        String contenido = msg.getContent();
        String remitente = msg.getSender().getLocalName();

        System.out.println("  REPOSITORIO: 📩 REQUEST recibido de [" + remitente + "]: " + contenido);

        if ("GET_MODEL".equals(contenido)) {
            solicitudesAtendidas++;

            // Construir respuesta INFORM con el modelo serializado
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.INFORM);
            reply.setContent(modeloProbabilidades);
            send(reply);

            System.out.println("  REPOSITORIO: ✅ Modelo enviado a [" + remitente + "]"
                + " (solicitud #" + solicitudesAtendidas + ")");
        } else {
            System.out.println("  REPOSITORIO: ⚠️ REQUEST no reconocido: " + contenido);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // HANDLER: INFORM — Feedback del usuario (simulación de reentrenamiento)
    // ═══════════════════════════════════════════════════════════
    private void manejarUpdate(ACLMessage msg) {
        String contenido = msg.getContent();
        String remitente = msg.getSender().getLocalName();

        System.out.println("  REPOSITORIO: 📥 FEEDBACK recibido de [" + remitente + "]");

        if (contenido != null && contenido.startsWith("UPDATE:")) {
            updatesRecibidos++;

            // Formato esperado: "UPDATE:token1 token2 token3:PRIORIDAD_CORRECTA"
            String payload = contenido.substring(7); // Quitar "UPDATE:"
            String[] partes = payload.split(":");

            if (partes.length >= 2) {
                String tokensAfectados = partes[0];
                String prioridadCorrecta = partes[1];

                System.out.println("  REPOSITORIO: 🔄 [SIMULACIÓN] Reentrenamiento del modelo:");
                System.out.println("    → Tokens:   " + tokensAfectados);
                System.out.println("    → Corregir a: " + prioridadCorrecta);
                System.out.println("    → Estado:   Actualizado en memoria (update #" + updatesRecibidos + ")");

                // En un sistema real, aquí se re-calcularían las probabilidades
                // usando la fórmula de actualización bayesiana:
                // P(clase|palabra)_nuevo = P(clase|palabra)_viejo * factor_corrección
            } else {
                System.out.println("  REPOSITORIO: ⚠️ Formato de UPDATE inválido: " + contenido);
            }

            // Confirmar al emisor que se procesó el feedback
            ACLMessage reply = msg.createReply();
            reply.setPerformative(ACLMessage.CONFIRM);
            reply.setContent("UPDATE_OK:" + updatesRecibidos);
            send(reply);

            System.out.println("  REPOSITORIO: ✅ Confirmación enviada a [" + remitente + "]");

        } else {
            // INFORM genérico (no UPDATE) — lo logueamos
            System.out.println("  REPOSITORIO: ℹ️ INFORM recibido: " + contenido);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // TAKEDOWN: Desregistro limpio del DF
    // ═══════════════════════════════════════════════════════════
    @Override
    protected void takeDown() {
        try {
            DFService.deregister(this);
            System.out.println("  REPOSITORIO: ✅ Desregistrado del DF");
        } catch (FIPAException e) {
            System.err.println("  REPOSITORIO: Error al desregistrarse del DF: " + e.getMessage());
        }
        System.out.println("══════════════════════════════════════════════════");
        System.out.println("  REPOSITORIO: Agente terminado.");
        System.out.println("    📊 Solicitudes de modelo atendidas: " + solicitudesAtendidas);
        System.out.println("    🔄 Updates de feedback procesados:  " + updatesRecibidos);
        System.out.println("══════════════════════════════════════════════════");
    }
}