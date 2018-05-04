package jadeapp;

import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.core.Runtime;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

public class StartJade {

    ContainerController cc;
    
    public static void main(String[] args) throws Exception {
        StartJade s = new StartJade();
        s.startContainer();
        s.createAgents();         
    }

    void startContainer() {
        ProfileImpl p = new ProfileImpl();
        p.setParameter(Profile.MAIN_HOST, "localhost");
        p.setParameter(Profile.GUI, "false");
        
        cc = Runtime.instance().createMainContainer(p);
    }
    
    void createAgents() throws Exception {
    	int nI = 50, nP = 50, nR = 25;
    	//Participants must be created firstly since they must be waiting for a cfp
        for (int i=1; i<=nP; i++) {
            AgentController ac = cc.createNewAgent("p"+i, "jadeagents.Participant", new Object[] { i });
            ac.start();
        }
    	//Rejectors
        for (int i=1; i<=nR; i++) {
            AgentController ac = cc.createNewAgent("r"+i, "jadeagents.Rejector", new Object[] { i });
            ac.start();
        }
        //Initiators are the last ones to be created
        for (int i=1; i<=nI; i++) {
            Object args[] = new Object[] {nI, nP, nR};
            AgentController ac = cc.createNewAgent("i"+i, "jadeagents.Initiator", args);
            ac.start();
        }
    }
}
