package com.sma;

import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.ContainerController;

public class Main {

    public static void main(String[] args) {
        Runtime rt = Runtime.instance();

        Profile p = new ProfileImpl();
        p.setParameter(Profile.GUI, "true");

        ContainerController container = rt.createMainContainer(p);

        try {
            container.createNewAgent("tokenizer",
                    "com.sma.agents.AgenteTokenizer", null).start();

            container.createNewAgent("interfaz",
                    "com.sma.agents.AgenteInterfaz", null).start();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}