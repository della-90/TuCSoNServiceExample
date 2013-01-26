import it.unibo.ing2.jade.exceptions.NoTucsonAuthenticationException;
import it.unibo.ing2.jade.operations.In;
import it.unibo.ing2.jade.operations.Out;
import it.unibo.ing2.jade.operations.TucsonAction;
import it.unibo.ing2.jade.operations.TucsonOperationHandler;
import it.unibo.ing2.jade.service.TuCSoNHelper;
import it.unibo.ing2.jade.service.TuCSoNService;
import jade.core.Agent;
import jade.core.ServiceException;
import jade.core.behaviours.CyclicBehaviour;

import java.util.Random;

import alice.logictuple.LogicTuple;
import alice.logictuple.exceptions.InvalidLogicTupleException;
import alice.tucson.api.TucsonTupleCentreId;
import alice.tucson.api.exceptions.TucsonInvalidAgentIdException;
import alice.tucson.api.exceptions.TucsonInvalidTupleCentreIdException;


public class Philosopher extends Agent {
	
	protected TuCSoNHelper helper;
	protected int chop1, chop2;
	
	@Override
	protected void setup() {
		super.setup();
		Object[] args = getArguments();
		chop1 = Integer.parseInt(args[0].toString());
		chop2 = (chop1+1)%3;
		try {
			helper = (TuCSoNHelper) getHelper(TuCSoNService.NAME);
			helper.authenticate(this);
			addBehaviour(new PhilosopherB(this));
		} catch (ServiceException e) {
			System.err.println("TuCSonService unavailable");
		} catch (TucsonInvalidAgentIdException e) {
			//Should be never thrown
			e.printStackTrace();
		}
	}
	
	@Override
	protected void takeDown() {
		helper.deauthenticate(this);
		super.takeDown();
	}
	
	private void log(String msg){
		System.out.println("["+getLocalName()+"] "+msg);
	}

	
	private class PhilosopherB extends CyclicBehaviour {
		
		public PhilosopherB(Agent agent){
			super(agent);
		}

		@Override
		public void action() {
			think();
			getChops();
			eat();
			releaseChops();
		}
		
		private void think(){
			log("Thinking");
			Random random = new Random();
			try {
				Thread.sleep(random.nextInt(5000)+5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void getChops(){
			log("Get chops "+chop1+","+chop2);
			try {
				TucsonOperationHandler handler = helper.getOperationHandler(myAgent);
				TucsonTupleCentreId tcid = new TucsonTupleCentreId("table", "localhost", "20504");
				LogicTuple tuple = LogicTuple.parse("chops("+chop1+","+chop2+")");
				TucsonAction action = new In(tcid, tuple);
				
				handler.executeSynch(action, null);
			} catch (NoTucsonAuthenticationException e) {
				//Should be never thrown
				e.printStackTrace();
			} catch (TucsonInvalidTupleCentreIdException e) {
				e.printStackTrace();
			} catch (InvalidLogicTupleException e) {
				e.printStackTrace();
			} catch (Exception e) {
				
				e.printStackTrace();
			}
		}
		
		private void eat(){
			log("Eating");
			Random random = new Random();
			try {
				Thread.sleep(random.nextInt(5000)+5000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		private void releaseChops(){
			log("Releasing chops "+chop1+","+chop2);
			try {
				TucsonOperationHandler handler = helper.getOperationHandler(myAgent);
				TucsonTupleCentreId tcid = new TucsonTupleCentreId("table", "localhost", "20504");
				LogicTuple tuple = LogicTuple.parse("chops("+chop1+","+chop2+")");
				TucsonAction action = new Out(tcid, tuple);
				
				handler.executeSynch(action, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}
}
