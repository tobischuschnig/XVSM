package fabrik.xvsm.roboter;

import autoKonfiguration.Auto;
import einzelteile.Achse;
import einzelteile.Bodenplatte;
import einzelteile.Karosserie;
import einzelteile.Lenkrad;
import einzelteile.ReifenPaar;
import einzelteile.Sitz;
import fabrik.ID;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozartspaces.capi3.CountNotMetException;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsConstants.RequestTimeout;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;
import org.mozartspaces.core.RequestContext;
import org.mozartspaces.core.TransactionReference;

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
    private Capi capi;

    public void connect() {
        try {
            MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
            this.capi = new Capi(core);
            ContainerReference idContainer = capi.lookupContainer("ID", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            id = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
            System.err.println("Got id: " + id);
        } catch (URISyntaxException | MzsCoreException ex) {
            Logger.getLogger(LogistikRoboter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

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
            ContainerReference einzelteile = null;
            ContainerReference autos = null;
            try {
                einzelteile = capi.lookupContainer("Einzelteile", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
                autos = capi.lookupContainer("Autos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            } catch (MzsCoreException ex) {
                System.err.println("Einzelteile ist nicht da");
            } catch (URISyntaxException ex) {
                Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex);
            }

            TransactionReference tx = null;
            try {
                tx = capi.createTransaction(1000, new URI("xvsm://localhost:9876"));
            } catch (MzsCoreException | URISyntaxException ex) {
                Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex);
            }
            ArrayList<LindaCoordinator.LindaSelector> selectoren = new ArrayList<>();
            selectoren.add(LindaCoordinator.newSelector(new Achse(0L, 0L, (boolean) false), 2));
            selectoren.add(LindaCoordinator.newSelector(new ReifenPaar(0L, 0L, (boolean) false), 2));
            selectoren.add(LindaCoordinator.newSelector(new Bodenplatte(0L, 0L, (boolean) false), 1));
            selectoren.add(LindaCoordinator.newSelector(new Sitz(0L, 0L, (boolean) false), 1));
            selectoren.add(LindaCoordinator.newSelector(new Karosserie(0L, 0L, (boolean) false), 1));
            selectoren.add(LindaCoordinator.newSelector(new Lenkrad(0L, 0L, (boolean) false), 1));
            try {
                long entryID = -1;
                ContainerReference idContainer = capi.lookupContainer("ID", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
                entryID = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
                System.err.println("Got car id: " + entryID);
                auto = new Auto(entryID, id);
                ArrayList<Achse> achsen = null;
                achsen = capi.take(einzelteile, selectoren.get(0), RequestTimeout.ZERO, tx);
                auto.setAchseVorn(achsen.get(0));
                parts++;
                auto.setAchseHinten(achsen.get(1));
                parts++;
                ArrayList<ReifenPaar> reifen = null;
                reifen = capi.take(einzelteile, selectoren.get(1), RequestTimeout.ZERO, tx);

                auto.setReifenPaarVorn(reifen.get(0));
                parts++;
                auto.setReifenPaarHinten(reifen.get(1));
                parts++;
                auto.setBodenplatte((Bodenplatte) capi.take(einzelteile, selectoren.get(2), RequestTimeout.ZERO, tx).get(0));
                parts++;
                auto.setSitz((Sitz) capi.take(einzelteile, selectoren.get(3), RequestTimeout.ZERO, tx).get(0));
                parts++;
                auto.setKarosserie((Karosserie) capi.take(einzelteile, selectoren.get(4), RequestTimeout.ZERO, tx).get(0));
                parts++;
                auto.setLenkrad((Lenkrad) capi.take(einzelteile, selectoren.get(5), RequestTimeout.ZERO, tx).get(0));
                parts++;

                capi.write(new Entry(auto), autos);
                capi.commitTransaction(tx);
                System.out.println("Car successfully produced!");
            } catch (MzsTimeoutException | CountNotMetException ex) {
                try {
                    capi.rollbackTransaction(tx);
                } catch (MzsCoreException ex1) {
                    Logger.getLogger(MontageRoboter.class.getName()).log(Level.SEVERE, null, ex1);
                }
                System.out.println("Nicht genug Einzelteile");
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
            monteur.connect();
            monteur.start();
        } catch (Exception e) {
            System.err.println("Registry not online!");
            System.exit(1);
        }
    }
}
