package example.agents;

import it.unibo.ing2.jade.operations.No;
import it.unibo.ing2.jade.operations.Out;
import it.unibo.ing2.jade.operations.TucsonAction;
import it.unibo.ing2.jade.operations.TucsonOperationHandler;
import it.unibo.ing2.jade.service.TuCSoNHelper;
import it.unibo.ing2.jade.service.TuCSoNService;
import jade.core.Agent;
import jade.core.behaviours.OneShotBehaviour;
import alice.logictuple.LogicTuple;
import alice.tucson.api.TucsonTupleCentreId;

public class Master extends Agent {
	
	private TuCSoNHelper helper;
	private TucsonOperationHandler handler;
	private TucsonTupleCentreId defaultTC;
	
	@Override
	protected void setup() {
		// TODO Auto-generated method stub
		super.setup();
		
		try {
			defaultTC = new TucsonTupleCentreId("worker","localhost","20504");
			helper = (TuCSoNHelper) getHelper(TuCSoNService.NAME);
			helper.authenticate(this);
			handler = helper.getOperationHandler(this);
			addBehaviour(new MasterBehaviour());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Override
	protected void takeDown() {
		helper.deauthenticate(this);
		super.takeDown();
	}
	
	private class MasterBehaviour extends OneShotBehaviour {

		@Override
		public void action() {
			try {
				//Quando non ci sono più tuple...
				LogicTuple tuple = LogicTuple.parse("X");
				TucsonAction action = new No(defaultTC, tuple);
				handler.executeSynch(action, null);
				
				//...inserisco tutte quelle relative ai container da visitare
				System.out.println("Doing out(cont(...))");
				tuple = LogicTuple.parse("cont('Container-1')");
				action = new Out(defaultTC, tuple);
				handler.executeSynch(action, null);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
	}

}
