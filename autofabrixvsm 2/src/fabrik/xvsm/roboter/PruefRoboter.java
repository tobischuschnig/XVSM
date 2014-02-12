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
 * Die Pr\u00FCfroboter \u00FCberpr\u00FCfen die fertigen Autos. Jeder
 * Pr\u00FCfroboter ist f\u00FCr eine bestimmte Art von \u00DCberpr\u00FCfung
 * programmiert worden. Ein Auto muss vor der Auslieferung folgende Tests
 * bestehen: Gewichtstest und Komponentenprobe. Beim Gewichtstest m\u00FCssen
 * Sie keine Logik implementieren. Es reicht, wenn der Test immer positiv ist.
 * Bei der Komponentenprobe wird nach defekten Teilen gesucht. Falls ein Auto
 * ein oder mehrere defekte Teile hat, wird das ganze Auto als defekt markiert.
 * Die Tests k\u00F6nnen in verschiedenen Reihenfolgen durchgef\u00FChrt werden,
 * abh\u00E4ngig davon, welcher Roboter nichts zu tun hat, allerdings nie
 * gleichzeitig an einem Auto. Welche Messung ein Roboter durchf\u00FChren kann,
 * wird beim Start angegeben.
 * 
 * @author Michael Borko
 * 
 */
public abstract class PruefRoboter extends Thread {

	protected long id;
	private static ICallFactory callFactory = null;

//	public void connect() throws RemoteException, NotBoundException {
//
//		Registry registry = LocateRegistry.getRegistry(Config.registryPort);
//		callFactory = (ICallFactory) registry
//				.lookup(Config.unicastRemoteObjectName);
//		id = callFactory.getID();
//		System.err.println("Got id: " + id);
//	}

	public PruefRoboter() {
		System.out.println();
		System.out.println("Pruefroboter meldet sich zum Dienst");
		System.out.println();
	}

	public void run() {
		// TODO NICE :: Callbackimplementierung soll Polling ersetzen
		while (true) {
			// Warte 1-3 Sekunden
			try {
				Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
			} catch (InterruptedException e1) {
			}

			try {
				Auto auto = callFactory.testen();
				if (!auto.isDefekt())
					auto.setDefekt(!isAutoOK(auto));

				System.out.println("Auto geprueft: " + auto.isDefekt());

				callFactory.getestet(auto);

			} catch (RemoteException re) {
				if (re.getMessage().contains("Connection refused")) {
					System.err.println("Registry said goodbye ...");
					System.exit(1);
				}
				// else System.err.println("Keine Autos !!!");
			}
		}
	}

	public abstract boolean isAutoOK(Auto auto);

	public static void main(String[] args) throws Exception {
		PruefRoboter prfRoboter1 = new PruefRoboterGewicht();
		callFactory = Fabrik.getInstance();
		prfRoboter1.start();

		PruefRoboter prfRoboter2 = new PruefRoboterTeile();
		callFactory = Fabrik.getInstance();
		prfRoboter2.start();
	}
}
