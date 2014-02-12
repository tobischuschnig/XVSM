package fabrik.xvsm.roboter;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import fabrik.xvsm.Config;

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

    public long id;
    private Capi capi;
//    private ICallFactory callFactory = null;

    public void connect() {
        try {
            MzsCore core = DefaultMzsCore.newInstanceWithoutSpace();
            this.capi = new Capi(core);
            ContainerReference idContainer = capi.lookupContainer("ID", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            id = ((ID) ((Entry) capi.read(idContainer).get(0)).getValue()).id;
            System.err.println("Got id: " + id);
        } catch (MzsCoreException | URISyntaxException ex) {
            Logger.getLogger(PruefRoboter.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public PruefRoboter() {
        System.out.println();
        System.out.println("Pruefroboter meldet sich zum Dienst");
        System.out.println();
    }

    public void run() {
        try {
            ContainerReference autos = capi.lookupContainer("Autos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);
            ContainerReference getesteteAutos = capi.lookupContainer("GetesteteAutos", new URI("xvsm://localhost:9876"), Long.MAX_VALUE, null);

            while (true) {
                // Warte 1-3 Sekunden
                try {
                    Thread.sleep((long) (Math.random() * 1000 * 2) + 1000);
                } catch (InterruptedException e1) {
                }

                try {
                    AnySelector as = AnyCoordinator.newSelector(1);
                    capi.test(autos, as, Long.MAX_VALUE, null);
                    Auto auto = (Auto) capi.take(autos, as, Long.MAX_VALUE, null).get(0);
                    if (!auto.isDefekt()) {
                        auto.setDefekt(!isAutoOK(auto));
                    }
                    System.out.println("Auto geprueft: " + !auto.isDefekt());
                    capi.write(new Entry(auto), getesteteAutos);
                } catch (MzsTimeoutException ex) {
                    System.out.println("Kein Auto zum Prüfen");
                }
            }
        } catch (Exception ex) {
            System.exit(1);
        }
    }

    public abstract boolean isAutoOK(Auto auto);

    public static void main(String[] args) throws Exception {
        PruefRoboter prfRoboter1 = new PruefRoboterGewicht();
        prfRoboter1.connect();
        prfRoboter1.start();

        PruefRoboter prfRoboter2 = new PruefRoboterTeile();
        prfRoboter2.connect();
        prfRoboter2.start();
    }
}
