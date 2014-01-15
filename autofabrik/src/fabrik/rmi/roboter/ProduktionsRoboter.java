package fabrik.rmi.roboter;

import java.lang.reflect.Constructor;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fabrik.rmi.Config;
import fabrik.rmi.ICallFactory;

import autoKonfiguration.Einzelteil;

/**
 * Die Einzelteile werden von spezialisierten Produktionsroboter erzeugt, d.h.
 * jeder Roboter erzeugt entweder Bodenplatten, Karosserien, Reifen, Achsen,
 * Sitze oder Lenkr\u00E4der. Produktionsroboter sind Akkordarbeiter. Das heißt,
 * sie produzieren die ihnen vorgegebene Anzahl an Teilen und haben dann ihre
 * Arbeit erledigt. Sollen noch mehr Teile produziert werden, muss man einen
 * neuen Roboter damit beauftragen. Auch Roboter machen ab und zu Fehler und
 * erstellen defekte Teile. Die Produktion jedes Teiles soll eine gewisse Zeit
 * dauern (ein Zufallswert von 1-3 Sekunden reicht).
 * 
 * @author Michael Borko
 * 
 */
public class ProduktionsRoboter extends Thread {

	private Class<? extends Einzelteil> type;

	private long id;
	private int anzahl;
	private double fehlerrate;

	private ICallFactory callFactory = null;

	@SuppressWarnings("unchecked")
	public ProduktionsRoboter(int anzahl, double fehlerrate, String type) {

		try {
			this.type = (Class<? extends Einzelteil>) Class.forName(type);
		} catch (ClassNotFoundException e) {
			System.err.println("Houston, we have a problem ...");
		}

		System.out.println();
		System.out.println("Produktionsroboter fuer " + "<" + type + ">"
				+ " meldet sich zum Dienst");
		System.out.println();

		this.anzahl = anzahl;
		this.fehlerrate = fehlerrate;

		try {
			// TODO MUST :: Connect mit Registry herstellen und callFactory setzen
			// <REMOVE>
			Registry registry = LocateRegistry.getRegistry(Config.registryPort);
			callFactory = (ICallFactory) registry
					.lookup(Config.unicastRemoteObjectName);
			// </REMOVE>
			id = callFactory.getID();
			System.err.println("Got id: " + id);
		} catch (RemoteException re) {
			if (re.getMessage().contains("Connection refused")) {
				System.err.println("Registry said goodbye ...");
				System.exit(1);
			}
		} catch (NotBoundException nbe) {
			System.err.println("Service is not bound anymore ...");
			System.exit(1);
		}

	}

	public void run() {
		int anzahlDefekte = (int) (anzahl * fehlerrate);
		for (int zaehler = 1; zaehler <= anzahl; zaehler++) {
			// Warte 1-3 Sekunden
			try {
				Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
			} catch (InterruptedException e1) {
			}

			// Bastel neues Teil
			boolean defekt = false;
			if (zaehler <= anzahlDefekte)
				defekt = true;

			long entryID = -1;
			try {
				entryID = callFactory.getID();
				System.err.println("Got component id: " + entryID);
			} catch (RemoteException re) {
				if (re.getMessage().contains("Connection refused")) {
					System.err.println("Registry said goodbye ...");
					System.exit(1);
				}
			}

			Einzelteil teil = null;

			// Erstellung eines neuen Objekts von einem generischen Datentypen
			try {
				@SuppressWarnings("rawtypes")
				Class[] argsC = new Class[] { long.class, long.class,
						boolean.class };
				Constructor<? extends Einzelteil> constructor = type
						.getConstructor(argsC);

				teil = constructor.newInstance(new Object[] { entryID, id,
						defekt });
			} catch (Exception e) {
				System.err.println("Houston, we have a problem ...");
			}

			System.out.println("Created new " + type.getName());

			try {
				callFactory.setEinzelteil(type.getName(), teil);
			} catch (RemoteException re) {
				if (re.getMessage().contains("Connection refused")) {
					System.err.println("Registry said goodbye ...");
					System.exit(1);
				}
			}

		}
	}

}
