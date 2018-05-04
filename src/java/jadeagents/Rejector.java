package jadeagents;

import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class Rejector extends Agent {
	private static final long serialVersionUID = 1L;

	protected void setup() {

		// Register the fruit-selling service in the yellow pages
		DFAgentDescription dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setType("fruit-selling");
		sd.setName("JADE-fruit-trading");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException fe) {
			fe.printStackTrace();
		}

		// Add the behaviour serving queries from buyer agents
		addBehaviour(new CFPServer());

		// Add the behaviour serving purchase orders from buyer agents
		addBehaviour(new RejectProposalServer());

	}

	/*
	 * Wait for a call for Proposals
	 */
	private class CFPServer extends CyclicBehaviour {
		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.CFP);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// CFP Message received. Process it
				String title = msg.getContent();
				ACLMessage reply = msg.createReply();

				// The requested fruit is NOT available for sale.
				reply.setPerformative(ACLMessage.REFUSE);
				reply.setContent(title);

				myAgent.send(reply);

			} else {
				block();
			}
		}
	}

	/*
	 * Initiator is saying he rejected my proposal
	 */
	private class RejectProposalServer extends CyclicBehaviour {

		private static final long serialVersionUID = 1L;

		public void action() {
			MessageTemplate mt = MessageTemplate.MatchPerformative(ACLMessage.REJECT_PROPOSAL);
			ACLMessage msg = myAgent.receive(mt);
			if (msg != null) {
				// REJECT_PROPOSAL Message received. Do nothing
				return;
			}
		}
	}

}
