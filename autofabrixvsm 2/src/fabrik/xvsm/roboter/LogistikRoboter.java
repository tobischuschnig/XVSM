package fabrik.xvsm.roboter;

import autoKonfiguration.Auto;
import fabrik.ID;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.AnyCoordinator.AnySelector;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.MzsTimeoutException;

/**
 * In der Logistik werden die fertigen Autos von Logistikroboter ausgeliefert (=
 * als fertig markiert), nachdem sie alle Tests bestanden haben. Defekte Autos
 * werden nicht transportiert, sondern enden an einer Sammelstelle.
 *
 * @author Michael Borko
 *
 */
public class LogistikRoboter extends Thread {

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

    public LogistikRoboter() {
        System.out.println();
        System.out.println("LogistikRoboter meldet sich zum Dienst");
        System.out.println();
    }

    @Override
    public void run() {
        try {
            ContainerReference tested = capi.lookupContainer("GetesteteAutos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            ContainerReference crashed = capi.lookupContainer("KaputteAutos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            ContainerReference delivered = capi.lookupContainer("GelieferteAutos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            ContainerReference cars = capi.lookupContainer("Autos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);


            while (true) {
                try {
                    // Warte 1-3 Sekunden
                    try {
                        Thread.currentThread().sleep((long) (Math.random() * 1000 * 2) + 1000);
                        // Thread.sleep(500);
                    } catch (InterruptedException e1) {
                    }
                    AnySelector ls = AnyCoordinator.newSelector(1);
                    capi.test(tested, ls, Long.MAX_VALUE, null);
                    Auto auto = (Auto) capi.take(tested, ls, Long.MAX_VALUE, null).get(0);
                    if (auto.isDefekt()) {
                        // Fertig geprueft
                        auto.setLieferantID(id);
                        capi.write(new Entry(auto), crashed);
                        System.out.println("Auto " + auto.getAutoID() + " entsorgt");
                    } else {
                        // Ist es ueberhaupt geprueft?
                        if ((auto.getPrueferDefekteID() != -1) && (auto.getPrueferGewichtID() != -1)) {
                            // Fertig geprueft
                            auto.setLieferantID(id);
                            capi.write(new Entry(auto), delivered);
                            System.out.println("Auto " + auto.getAutoID()
                                    + " ausgeliefert");
                        } else {
                            capi.write(cars, new Entry(auto));
                            System.out.println("Auto nicht vollständig geprueft");
                        }
                    }
                } catch (MzsTimeoutException ex) {
                    System.out.println("Kein Auto verfuegbar");
                }
            }
        } catch (Exception ex) {
            System.err.println("Container said goodbye ..." + ex.getMessage());
            System.exit(1);
        }
    }

    public static void main(String[] args) {
        try {
            LogistikRoboter lieferant = new LogistikRoboter();
            lieferant.connect();
            lieferant.start();
        } catch (Exception e) {
            System.err.println("Space not online!");
            System.exit(1);
        }
    }
}
