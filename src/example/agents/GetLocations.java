package example.agents;

import jade.content.lang.Codec.CodecException;
import jade.content.lang.sl.SLCodec;
import jade.content.onto.OntologyException;
import jade.content.onto.basic.Action;
import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import jade.domain.FIPANames;
import jade.domain.JADEAgentManagement.QueryPlatformLocationsAction;
import jade.domain.mobility.MobilityOntology;
import jade.lang.acl.ACLMessage;

public class GetLocations extends OneShotBehaviour {
	
	public GetLocations(Agent agent){
		super(agent);
	}

	@Override
	public void action() {
		myAgent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL0);
		myAgent.getContentManager().registerLanguage(new SLCodec(), FIPANames.ContentLanguage.FIPA_SL);
		myAgent.getContentManager().registerOntology(MobilityOntology.getInstance());
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.addReceiver(myAgent.getAMS());
		
		AID remotePlat = new AID("ams@Plat2", AID.ISGUID);
		remotePlat.addAddresses("http://192.168.1.4:7778/acc");
//		msg.addReceiver(remotePlat);
		msg.setOntology(MobilityOntology.NAME);
		msg.setProtocol(FIPANames.InteractionProtocol.FIPA_REQUEST);
		msg.setLanguage(FIPANames.ContentLanguage.FIPA_SL0);
		Action action = new Action(myAgent.getAID(), new QueryPlatformLocationsAction());
		try {
			myAgent.getContentManager().fillContent(msg, action);
		} catch (CodecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (OntologyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		myAgent.send(msg);
	}

}
