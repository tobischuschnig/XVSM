package fabrik.xvsm.roboter;

import autoKonfiguration.Einzelteil;
import fabrik.ID;
import fabrik.xvsm.ICallFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.concurrent.RejectedExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCoreException;

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
	private Capi capi;
    private ContainerReference idContainer;

	private ICallFactory callFactory = null;

	@SuppressWarnings("unchecked")
	public ProduktionsRoboter(Capi capi, int anzahl, double fehlerrate, String type) {
		try {
            this.type = (Class<? extends Einzelteil>) Class.forName(type);
        } catch (ClassNotFoundException e) {
            System.err.println("Houston, we have a problem ...");
        }
        this.capi = capi;
        System.out.println();
        System.out.println("Produktionsroboter fuer " + "<" + type + ">"
                + " meldet sich zum Dienst");
        System.out.println();

        this.anzahl = anzahl;
        this.fehlerrate = fehlerrate;
        this.connect();

	}
	
	public void connect() {
        try {
            idContainer = capi.lookupContainer("ID", new URI("xvsm://localhost:9876"),RequestTimeout.DEFAULT, null);
//            idContainer = capi.lookupContainer("ID", null, Long.MAX_VALUE, null);
            id = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
            System.err.println("Got id: " + id);
//        } catch (URISyntaxException | MzsCoreException ex) {
        } catch (MzsCoreException ex)   {
            Logger.getLogger(LogistikRoboter.class.getName()).log(Level.SEVERE, null, ex);
        } catch (URISyntaxException ex) {
            Logger.getLogger(LogistikRoboter.class.getName()).log(Level.SEVERE, null, ex);

		}
    }

	public void run() {
		int anzahlDefekte = (int) (anzahl * fehlerrate);
        ContainerReference container;
		try {
			container = capi.lookupContainer("Einzelteile", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);

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
					entryID = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
				} catch (MzsCoreException e1) {
					System.err.println("Container Error!" + e1.getMessage());
					System.exit(1);
					e1.printStackTrace();
				}
				System.err.println("Got component id: " + entryID+"\n");


				Einzelteil teil = null;

				// Erstellung eines neuen Objekts von einem generischen Datentypen
				try {
                    @SuppressWarnings("rawtypes")
                    Class[] argsC = new Class[]{long.class, long.class,
                        boolean.class};
                    Constructor<? extends Einzelteil> constructor = type
                            .getConstructor(argsC);

                    teil = constructor.newInstance(new Object[]{entryID, id,
                                defekt});
                } catch (NoSuchMethodException | SecurityException | InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                    System.err.println("Houston, we have a problem ...");
                }

				try {
					capi.write(container, new Entry(teil));
					System.out.println("Created new " + type.getName());
				} catch (Exception e) {
					System.err.println("Problem writing in the Space!");
					e.printStackTrace();
					System.exit(1);
				}

			}
		} catch (MzsCoreException | URISyntaxException e2) {
			System.err.println("Problem starting the space!");
			e2.printStackTrace();
			System.exit(1);
		}
	}

}
