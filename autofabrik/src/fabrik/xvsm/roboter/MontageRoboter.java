package fabrik.xvsm.roboter;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import autoKonfiguration.Auto;
import einzelteile.Achse;
import einzelteile.Bodenplatte;
import einzelteile.Karosserie;
import einzelteile.Lenkrad;
import einzelteile.ReifenPaar;
import einzelteile.Sitz;
import fabrik.xvsm.Config;
import fabrik.xvsm.Fabrik;
import fabrik.xvsm.ICallFactory;

/**
 * Montageroboter bauen die Autos zusammen, sobald alle Teile verf\u00FCgbar
 * sind. Fertige Autos unterscheiden sich durch ihre eindeutige ID. Die
 * Montageroboter arbeiten unabh\u00E4ngig voneinander und d\u00FCrfen sich auf
 * keinen Fall gegenseitig behindern, d.h. ein Montageroboter darf z.B. nicht
 * den letzten Sitz f\u00FCr sich beanspruchen und ein anderer die letzte
 * Karosserie, wodurch beide kein fertiges Auto herstellen k\u00F6nnen, obwohl
 * eigentlich noch genug Teile f\u00FCr ein Auto vorhanden w\u00E4ren.
 * 
 * @author Michael Borko
 * 
 */
public class MontageRoboter extends Thread {

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

	public MontageRoboter() {
		System.out.println();
		System.out.println("Montageroboter meldet sich zum Dienst");
		System.out.println();
	}

	public void run() {
		while (true) {
			// Warte 1-3 Sekunden
			try {
				Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
				// Thread.sleep(500);
			} catch (InterruptedException e1) {
			}

			int parts = 0;
			Auto auto = null;
			try {
				if (callFactory.getMenge(Achse.class.getName()) > 1
						&& callFactory.getMenge(ReifenPaar.class.getName()) > 1
						&& callFactory.getMenge(Bodenplatte.class.getName()) > 0
						&& callFactory.getMenge(Sitz.class.getName()) > 0
						&& callFactory.getMenge(Karosserie.class.getName()) > 0
						&& callFactory.getMenge(Lenkrad.class.getName()) > 0) {
					long entryID = -1;
					entryID = callFactory.getID();
					System.err.println("Got car id: " + entryID);

					auto = new Auto(entryID, id);

					auto.setAchseVorn((Achse) callFactory
							.getEinzelteil(Achse.class.getName()));
					parts++;
					auto.setAchseHinten((Achse) callFactory
							.getEinzelteil(Achse.class.getName()));
					parts++;
					auto.setReifenPaarVorn((ReifenPaar) callFactory
							.getEinzelteil(ReifenPaar.class.getName()));
					parts++;
					auto.setReifenPaarHinten((ReifenPaar) callFactory
							.getEinzelteil(ReifenPaar.class.getName()));
					parts++;
					auto.setBodenplatte((Bodenplatte) callFactory
							.getEinzelteil(Bodenplatte.class.getName()));
					parts++;
					auto.setSitz((Sitz) callFactory.getEinzelteil(Sitz.class
							.getName()));
					parts++;
					auto.setKarosserie((Karosserie) callFactory
							.getEinzelteil(Karosserie.class.getName()));
					parts++;
					auto.setLenkrad((Lenkrad) callFactory
							.getEinzelteil(Lenkrad.class.getName()));
					parts++;

					callFactory.zusammengebaut(auto);
					System.out.println("Car successfully produced!");
				}

			} catch (RemoteException e) {
				// System.err.println("Something happend ... " +
				// e.getMessage());
				if (e.getMessage().contains("No Elements!")
						|| e.getMessage().contains("Car not accepted!")) {
					try {
						switch (parts) {

						case 8:
							callFactory.setEinzelteil(Lenkrad.class.getName(),
									auto.getLenkrad());
						case 7:
							callFactory.setEinzelteil(
									Karosserie.class.getName(),
									auto.getKarosserie());
						case 6:
							callFactory.setEinzelteil(Sitz.class.getName(),
									auto.getSitz());
						case 5:
							callFactory.setEinzelteil(
									Bodenplatte.class.getName(),
									auto.getBodenplatte());
						case 4:
							callFactory.setEinzelteil(
									ReifenPaar.class.getName(),
									auto.getReifenPaarHinten());
						case 3:
							callFactory.setEinzelteil(
									ReifenPaar.class.getName(),
									auto.getReifenPaarVorn());
						case 2:
							callFactory.setEinzelteil(Achse.class.getName(),
									auto.getAchseHinten());
						case 1:
							callFactory.setEinzelteil(Achse.class.getName(),
									auto.getAchseVorn());
						}
						// Auto wieder zerlegt
					} catch (RemoteException re) {
						System.err.println("Registry said goodbye ...");
						System.exit(1);
					}
				} else if (e.getMessage().contains("Connection refused")) {
					System.err.println("Registry said goodbye ...");
					System.exit(1);
				}
			}
		}
	}

	public static void main(String[] args) {
		try {
			MontageRoboter monteur = new MontageRoboter();
			callFactory = Fabrik.getInstance();
			monteur.start();
		} catch (Exception e) {
			System.err.println("Registry not online!");
			System.exit(1);
		}
	}
}
