package fabrik.xvsm.roboter;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.TransactionException;
import org.mozartspaces.core.TransactionReference;
import org.mozartspaces.core.MzsConstants.RequestTimeout;

import autoKonfiguration.Auto;
import einzelteile.Achse;
import einzelteile.Bodenplatte;
import einzelteile.Karosserie;
import einzelteile.Lenkrad;
import einzelteile.ReifenPaar;
import einzelteile.Sitz;
import fabrik.ID;
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
	private Capi capi;

//    public void connect() {
//        try {
//            MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
//            this.capi = new Capi(core);
//            ContainerReference idContainer = capi.lookupContainer("ID", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
//            id = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
//            System.err.println("Got id: " + id);
//        } catch (URISyntaxException | MzsCoreException ex) {
//            Logger.getLogger(LogistikRoboter.class.getName()).log(Level.SEVERE, null, ex);
//        }
//    }

	public MontageRoboter() {
		callFactory = Fabrik.getInstance1();
		capi = callFactory.getCapi();
		System.out.println();
		System.out.println("Montageroboter meldet sich zum Dienst");
		System.out.println();
	}

	public void run() { 
		while (true) {
			//System.out.println("123");
//			// Warte 1-3 Sekunden
			try {
				Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
				// Thread.sleep(500);
			} catch (InterruptedException e1) {
			}
//rausgenommen wegen benchmarking
			int parts = 0;
			Auto auto = null;
			TransactionReference tx = null;
			try {
				tx = capi.createTransaction( 100000, new URI("xvsm://localhost:9876"));
			} catch (MzsCoreException ex) {
                Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex);
			} catch (URISyntaxException ex) {
                Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (TransactionException ex) {
               //Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex);
            }
			
			try {
				//System.out.println(callFactory.getMenge(Achse.class.getName()));
				if (callFactory.getMenge(Achse.class.getName()) > 1
						&& callFactory.getMenge(ReifenPaar.class.getName()) > 1
						&& callFactory.getMenge(Bodenplatte.class.getName()) > 0
						&& callFactory.getMenge(Sitz.class.getName()) > 0
						&& callFactory.getMenge(Karosserie.class.getName()) > 0
						&& callFactory.getMenge(Lenkrad.class.getName()) > 0) {
					
					long entryID = -1;
					entryID = callFactory.getID();
					//TODO aendern
					
					
					
					System.err.println("Got car id: " + entryID);

					auto = new Auto(entryID, id);

					auto.setAchseVorn((Achse) callFactory
							.getEinzelteil(Achse.class.getName(),tx));
					parts++;
					auto.setAchseHinten((Achse) callFactory
							.getEinzelteil(Achse.class.getName(),tx));
					parts++;
					auto.setReifenPaarVorn((ReifenPaar) callFactory
							.getEinzelteil(ReifenPaar.class.getName(),tx));
					parts++;
					auto.setReifenPaarHinten((ReifenPaar) callFactory
							.getEinzelteil(ReifenPaar.class.getName(),tx));
					parts++;
					auto.setBodenplatte((Bodenplatte) callFactory
							.getEinzelteil(Bodenplatte.class.getName(),tx));
					parts++;
					auto.setSitz((Sitz) callFactory
							.getEinzelteil(Sitz.class.getName(),tx));
					parts++;
					auto.setKarosserie((Karosserie) callFactory
							.getEinzelteil(Karosserie.class.getName(),tx));
					parts++;
					auto.setLenkrad((Lenkrad) callFactory
							.getEinzelteil(Lenkrad.class.getName(),tx));
					parts++;

					callFactory.zusammengebaut(auto);
					capi.commitTransaction(tx);
					System.out.println("Car successfully produced!");
				}

			} catch (MzsTimeoutException | CountNotMetException ex) {
                try {
                    capi.rollbackTransaction(tx);
                } catch (MzsCoreException ex1) {
                    Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex1);
                }
                System.out.println("Not enougth parts.");
            } catch (Exception ex) {
                try {
                    capi.rollbackTransaction(tx);
                } catch (MzsCoreException ex1) {
                    Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex1);
                }
                System.exit(1);
            } 
			
		}
	}

	public static void main(String[] args) {
		try {
			MontageRoboter monteur = new MontageRoboter();
			//monteur.connect();
			monteur.start();
		} catch (Exception e) {
			System.err.println("Space not online!");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
