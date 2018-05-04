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
        p.setParameter(Profile.GUI, "true");
        
        cc = Runtime.instance().createMainContainer(p);
    }
    
    void createAgents() throws Exception {

    	int nR = 3;
        for (int i=1; i<=nR; i++) {
            AgentController ar = 
            		cc.createNewAgent("R"+i, "jadeagents.ContractNetResponderAgent", new Object[] { i });
            ar.start();
        }

        AgentController ac = 
        		cc.createNewAgent("In", "jadeagents.ContractNetInitiatorAgent", new Object[] { "R1","R2","R3" });
        ac.start();
    	
    }
}
