package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.*;

import java.util.*;

public class AgenteTokenizer extends Agent {

    private static final Set<String> STOPWORDS = Set.of(
            "de", "la", "el", "en", "y", "a", "los", "las",
            "un", "una", "por", "para", "con", "al"
    );

    @Override
    protected void setup() {
        System.out.println("[TOKENIZER] Iniciado");

        registrarEnDF();

        addBehaviour(new CyclicBehaviour() {
            @Override
            public void action() {

                ACLMessage msg = receive();

                if (msg != null) {
                    String texto = msg.getContent();

                    List<String> tokens = procesar(texto);

                    System.out.println("[TOKENIZER] Tokens: " + tokens);

                    ACLMessage reply = msg.createReply();
                    reply.setContent(String.join(",", tokens));
                    send(reply);

                } else {
                    block(); 
                }
            }
        });
    }


    private List<String> procesar(String texto) {
        texto = texto.toLowerCase();

        String[] partes = texto.split("\\s+");

        List<String> resultado = new ArrayList<>();

        for (String token : partes) {
            token = token.replaceAll("[^a-z0-9]", "");

            if (!token.isEmpty() && !STOPWORDS.contains(token)) {
                resultado.add(token);
            }
        }

        return resultado;
    }


}