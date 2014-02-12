package fabrik.xvsm.roboter;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fabrik.xvsm.Config;
import fabrik.xvsm.Fabrik;
import fabrik.xvsm.ICallFactory;
import autoKonfiguration.Auto;

/**
 * In der Logistik werden die fertigen Spielzeugautos von Logistikroboter
 * ausgeliefert (= als fertig markiert), nachdem sie alle Tests bestanden haben.
 * Defekte Autos werden nicht transportiert, sondern enden an einer
 * Sammelstelle.
 * 
 * @author Michael Borko
 * 
 */
public class LogistikRoboter extends Thread {

	private long id;
	private static ICallFactory callFactory = null;

//	public void connect() throws RemoteException, NotBoundException {
//
//		Registry registry = LocateRegistry.getRegistry(Config.registryPort);
//		callFactory = (ICallFactory) registry
//				.lookup(Config.unicastRemoteObjectName);
//		id = callFactory.getID();
//		System.err.println("Got id: " + id);
//	}

	public LogistikRoboter() {
		System.out.println();
		System.out.println("LogistikRoboter meldet sich zum Dienst");
		System.out.println();
	}

	public void run() {
		// TODO NICE :: Callbackimplementierung soll Polling ersetzen
		while (true) {
			// Warte 1-3 Sekunden
			try {
				Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
				// Thread.sleep(500);
			} catch (InterruptedException e1) {
			}

			try {
				Auto auto = callFactory.transport();

				// TODO MUST :: Implementierung der Logistik-Funktionalität
				// <REMOVE>
				if (auto.isDefekt()) {
					// Ab in die Sammelstelle!
					auto.setLieferantID(id);
					callFactory.verwerfen(auto);
					System.out
							.println("Auto " + auto.getAutoID() + " entsorgt");
				} else {
					// Ist es ueberhaupt geprueft?
					if ((auto.getPrueferDefekteID() != -1)
							&& (auto.getPrueferGewichtID() != -1)) {
						// Fertig geprueft
						auto.setLieferantID(id);
						callFactory.liefern(auto);
						System.out.println("Auto " + auto.getAutoID()
								+ " ausgeliefert");
					} else {
						callFactory.zusammengebaut(auto);
					}
				}
				// </REMOVE>

			} catch (RemoteException re) {
				// System.err.println("Something happend ... " +
				// re.getMessage());
				if (re.getMessage().contains("Connection refused")) {
					System.err.println("Registry said goodbye ...");
					System.exit(1);
				}
				// else System.err.println("Keine Autos !!!");
			}
		}
	}

	public static void main(String[] args) {
		try {
			LogistikRoboter lieferant = new LogistikRoboter();
			callFactory = Fabrik.getInstance();
			lieferant.start();
		} catch (Exception e) {
			System.err.println("Registry not online!");
			System.exit(1);
		}
	}
}
