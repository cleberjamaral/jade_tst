/**
 * ***************************************************************
 * JADE - Java Agent DEvelopment Framework is a framework to develop
 * multi-agent systems in compliance with the FIPA specifications.
 * Copyright (C) 2000 CSELT S.p.A.
 * 
 * GNU Lesser General Public License
 * 
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation,
 * version 2.1 of the License.
 * 
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307, USA.
 * **************************************************************
 */
package jadeagents;

import jade.core.Agent;
import jade.core.behaviours.ParallelBehaviour;
import jade.core.behaviours.TickerBehaviour;

import java.io.IOException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

import jade.core.AID;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.proto.ContractNetInitiator;
import jade.proto.ContractNetResponder;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPANames;
import jade.domain.FIPAAgentManagement.NotUnderstoodException;
import jade.domain.FIPAAgentManagement.RefuseException;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.FailureException;

/**
 * This example shows how to implement the responder role in a FIPA-contract-net
 * interaction protocol. In this case in particular we use a
 * <code>ContractNetResponder</code> to participate into a negotiation where an
 * initiator needs to assign a task to an agent among a set of candidates.
 * 
 * @author Giovanni Caire - TILAB
 */
public class ContractNetResponderAgent extends Agent {

	protected void setup() {
		System.out.println("Agent " + getLocalName() + " waiting for CFP...");
		MessageTemplate template = MessageTemplate.and(
				MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
				MessageTemplate.MatchPerformative(ACLMessage.CFP));

		addBehaviour(new ContractNetResponder(this, template) {
			protected ACLMessage prepareResponse(ACLMessage cfp) throws NotUnderstoodException, RefuseException {
				System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName()
						+ ". Action is " + cfp.getContent());
				int proposal = evaluateAction();
				if (proposal > 2) {
					// We provide a proposal
					System.out.println("Agent " + getLocalName() + ": Proposing " + proposal);
					ACLMessage propose = cfp.createReply();
					propose.setPerformative(ACLMessage.PROPOSE);
					propose.setContent(String.valueOf(proposal));
					return propose;
				} else {
					// We refuse to provide a proposal
					System.out.println("Agent " + getLocalName() + ": Refuse");
					throw new RefuseException("evaluation-failed");
				}
			}

			protected ACLMessage prepareResultNotification(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
					throws FailureException {
				System.out.println("Agent " + getLocalName() + ": Proposal accepted");
				if (performAction()) {
					System.out.println("Agent " + getLocalName() + ": Action successfully performed");
					ACLMessage inform = accept.createReply();
					inform.setPerformative(ACLMessage.INFORM);
					return inform;
				} else {
					System.out.println("Agent " + getLocalName() + ": Action execution failed");
					throw new FailureException("unexpected-error");
				}
			}

			protected void handleRejectProposal(ACLMessage reject) {
				System.out.println("Agent " + getLocalName() + ": Proposal rejected");
			}
		});
		DFAgentDescription dfd;

		// Register the vehicle-transporting service in the yellow pages
		dfd = new DFAgentDescription();
		dfd.setName(getAID());
		ServiceDescription sd = new ServiceDescription();
		sd.setName("Transport Of Items");
		sd.setType("Transport");
		dfd.addServices(sd);
		try {
			DFService.register(this, dfd);
		} catch (FIPAException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		ParallelBehaviour PB = new ParallelBehaviour();

		PB.addSubBehaviour(new Responder(this, null)); // ContractNetresponder behaviour for Vehicle agent
		PB.addSubBehaviour(new Initiator(this, 1000)); // ContractNetinitiator behaviour
		addBehaviour(PB);

	}

	public class Responder extends ContractNetResponder {

		public Responder(Agent a, MessageTemplate mt) {
			super(a, mt);
			MessageTemplate template = MessageTemplate.and(
					MessageTemplate.MatchProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET),
					MessageTemplate.MatchPerformative(ACLMessage.CFP));
			mt = template;
		}

		@Override
		protected ACLMessage handleCfp(ACLMessage cfp) throws NotUnderstoodException, RefuseException {

			System.out.println("Agent " + getLocalName() + ": CFP received from " + cfp.getSender().getName()
					+ ". Action is " + cfp.getContent());
			return cfp;

		}

		@Override
		protected ACLMessage handleAcceptProposal(ACLMessage cfp, ACLMessage propose, ACLMessage accept)
				throws FailureException {

			System.out.println("Agent " + getLocalName() + ": Proposal accepted");
			return accept;
		}

		protected void handleRejectProposal(ACLMessage cfp, ACLMessage propose, ACLMessage reject) {
			System.out.println("Agent " + cfp.getSender() + " rejected the proposal :(");
		}
	}

	public class Initiator extends TickerBehaviour // vehicle agent as initiator
	{
		int VResponders;
		DFAgentDescription template;
		ServiceDescription sd;
		AID[] responders;

		public Initiator(Agent a, long period) {
			super(a, period);
		}

		protected void onTick() {

			try {

				template = new DFAgentDescription();
				sd = new ServiceDescription();
				sd.setType("Transport");
				template.addServices(sd);

				DFAgentDescription[] result = DFService.search(myAgent, template);

				if (result.length > 0) {
					System.out.println("The Agent--->" + getName() + " found  this vehicle's Agents :");
					responders = new AID[(result.length) - 1];
					int j = 0;
					for (int i = 0; i < result.length; ++i) {
						if (!result[i].getName().getName().equals(myAgent.getName()))// eliminate the agent that launch
																						// the CFP
						{
							responders[j] = result[j].getName();
							System.out.println(responders[j].getName());
							j++;
						}
					}
				} else {
					System.out.println("Agent " + getLocalName() + " did not find any service");
				}
			} catch (FIPAException fe) {
				fe.printStackTrace();
			}

			System.out.println("number of responder---------------------------->" + responders.length);
			// 3. prepare the message to send

			for (int i = 0; i < responders.length; i++) {

				VResponders = responders.length;

				// Fill the CFP message
				ACLMessage msg = new ACLMessage(ACLMessage.CFP);
				for (AID a : responders) {
					msg.addReceiver(a);
					System.out.println("les receveurs sont-----------------" + a.getName());
				}
				msg.setProtocol(FIPANames.InteractionProtocol.FIPA_CONTRACT_NET);
				try {
					msg.setContentObject(Math.random() * 10);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

				// We want to receive a reply in 10 secs
				msg.setReplyByDate(new Date(System.currentTimeMillis() + ((1000))));

				addBehaviour(new ContractNetInitiator(myAgent, msg) {

					protected void handlePropose(ACLMessage propose, Vector v) {
						System.out.println(
								"Agent " + propose.getSender().getName() + " proposed " + propose.getContent());
					}

					protected void handleRefuse(ACLMessage refuse) {
						System.out.println("Agent " + refuse.getSender().getName() + " refused");
					}

					protected void handleFailure(ACLMessage failure) {
						if (failure.getSender().equals(myAgent.getAMS())) {
							// FAILURE notification from the JADE runtime: the receiver
							// does not exist
							System.out.println("Responder does not exist");
						} else {
							System.out.println("Agent " + failure.getSender().getName() + " failed");
						}
						// Immediate failure --> we will not receive a response from this agent
						VResponders--;
					}

					protected void handleAllResponses(Vector responses, Vector acceptances) {
						if (responses.size() < VResponders) {
							// Some responder didn't reply within the specified timeout
							System.out.println(
									"Timeout expired: missing " + (VResponders - responses.size()) + " responses");
						}
						// Evaluate proposals.
						int bestProposal = -1;
						AID bestProposer = null;
						ACLMessage accept = null;
						Enumeration e = responses.elements();
						while (e.hasMoreElements()) {
							ACLMessage msg = (ACLMessage) e.nextElement();
							if (msg.getPerformative() == ACLMessage.PROPOSE) {
								ACLMessage reply = msg.createReply();
								reply.setPerformative(ACLMessage.REJECT_PROPOSAL);
								acceptances.addElement(reply);
								int proposal = Integer.parseInt(msg.getContent());
								if (proposal > bestProposal) {
									bestProposal = proposal;
									bestProposer = msg.getSender();
									accept = reply;
								}
							}
						}
						// Accept the proposal of the best proposer
						if (accept != null) {
							System.out.println(
									"Accepting proposal " + bestProposal + " from responder " + bestProposer.getName());
							accept.setPerformative(ACLMessage.ACCEPT_PROPOSAL);
						}
					}

					protected void handleInform(ACLMessage inform) {
						System.out.println("Agent " + inform.getSender().getName()
								+ " successfully performed the requested action");
					}
				});

			}
		}

	}

	private int evaluateAction() {
		// Simulate an evaluation by generating a random number
		return (int) (Math.random() * 10);
	}

	private boolean performAction() {
		// Simulate action execution by generating a random number
		return (Math.random() > 0.2);
	}
}
