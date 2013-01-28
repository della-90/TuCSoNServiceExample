package example.agents;

import it.unibo.ing2.jade.operations.In;
import it.unibo.ing2.jade.operations.TucsonAction;
import it.unibo.ing2.jade.operations.TucsonOperationHandler;
import it.unibo.ing2.jade.service.TuCSoNHelper;
import it.unibo.ing2.jade.service.TuCSoNService;
import jade.core.Agent;
import jade.core.ContainerID;
import jade.core.Location;
import jade.core.behaviours.OneShotBehaviour;

import java.net.InetSocketAddress;

import alice.logictuple.LogicTuple;
import alice.tucson.api.ITucsonOperation;
import alice.tucson.api.TucsonTupleCentreId;
import alice.tucson.api.exceptions.TucsonInvalidTupleCentreIdException;

public class Worker extends Agent {

	private transient TuCSoNHelper helper;
	private transient TucsonOperationHandler handler;
	private transient TucsonTupleCentreId workerTC;
	
	private String nextIp;
	private int nextPort;
	private Location mainContainer;

	@Override
	protected void setup() {
		super.setup();
		addBehaviour(new MigrateAgent());
		mainContainer = here();
	}

	private void doStuff() {
		try {
			workerTC = new TucsonTupleCentreId("worker", "localhost", "20504");
			helper = (TuCSoNHelper) getHelper(TuCSoNService.NAME);
			helper.authenticate(this);
			handler = helper.getOperationHandler(this);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	protected void takeDown() {
		helper.deauthenticate(this);
		super.takeDown();
	}

	@Override
	protected void beforeMove() {
		System.out.println("Rilascio ACC");
		helper.deauthenticate(this);
		super.beforeMove();
	}

	@Override
	protected void afterMove() {
		super.afterMove();
		
		if (here().equals(mainContainer)){
			//Se vado sul main container non devo copiare niente
			System.out.println("I'm back to main container");
			addBehaviour(new MigrateAgent());
		} else {
			addBehaviour(new MigrateTC(nextIp, nextPort));
		}
	}

	private class MigrateAgent extends OneShotBehaviour {

		@Override
		public void action() {
			doStuff();
			System.out.println("Waiting for migrate...");
			try {
				//Ottengo le informazioni sul container di destinazione
				LogicTuple tuple = LogicTuple.parse("cont(X, Y)");
				TucsonAction action = new In(workerTC, tuple);
				System.out.println("Reading cont(X,Y) in "+workerTC);
				ITucsonOperation result = handler.executeSynch(action, null);

				String contName = result.getLogicTupleResult().getArg(0)
						.toString();
				String nodeName = result.getLogicTupleResult().getArg(1).toString();
				contName = contName.replace("'", "");
				System.out.println("Ho letto contName = " + contName);
				ContainerID dest = new ContainerID();
				dest.setName(contName);
				
				//Ottengo i dati del TC di destinazione
				if (nodeName.trim().length() > 0){
					InetSocketAddress addr = helper.findTupleCentre(nodeName);
					nextIp = addr.getAddress().getHostAddress();
					nextPort = addr.getPort();
				}
				System.out.println("Mi sposto sulla destinazione "+dest);
				doMove(dest);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

	}
	
	private class MigrateTC extends OneShotBehaviour {
		
		private String ip;
		private int port;

		public MigrateTC(String nextIp, int nextPort) {
			ip = nextIp;
			port = nextPort;
		}

		@Override
		public void action() {
			doStuff();
			
			try {
				System.out.println("imposto il nodo principale a "+ip+":"+port);
				helper.setMainNode(ip, port);
				
				//Invio tutto al nodo TuCSoN centrale
				System.out.println("Migro il TC");
				helper.doClone("centrale", "X", new String[]{"default"});
				
				helper.deauthenticate(Worker.this);
				Worker.this.addBehaviour(new MigrateAgent());
				System.out.println("Rilascio ACC");
				Worker.this.removeBehaviour(this);
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		
	}
}
