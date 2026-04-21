package com.sma.agents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class AgenteTokenizer extends Agent {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
        "el", "la", "los", "las", "un", "una", "unos", "unas",
        "de", "del", "en", "con", "por", "para", "al", "a",
        "es", "son", "fue", "ser", "y", "o", "no", "se",
        "que", "su", "sus", "lo", "le", "les", "me", "mi",
        "si", "como", "pero", "mas", "este", "esta", "muy"
    ));

    protected void setup() {
        System.out.println("TOKENIZER: Agente listo. Stopwords cargadas: " + STOPWORDS.size());

        addBehaviour(new CyclicBehaviour() {
            public void action() {
                ACLMessage msg = receive();
                if (msg != null) {
                    String raw = msg.getContent().toLowerCase().replaceAll("[^a-z ]", "");
                    StringBuilder filtered = new StringBuilder();
                    for (String word : raw.split("\\s+")) {
                        if (!word.isEmpty() && !STOPWORDS.contains(word))
                            filtered.append(word).append(" ");
                    }
                    String tokens = filtered.toString().trim();

                    ACLMessage reply = msg.createReply();
                    reply.setPerformative(ACLMessage.INFORM);
                    reply.setContent(tokens);
                    send(reply);
                    System.out.println("TOKENIZER: [" + raw.trim() + "] -> [" + tokens + "]");
                } else {
                    block();
                }
            }
        });
    }
}