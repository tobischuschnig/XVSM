package fabrik.xvsm;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.LinkedList;
import java.util.HashMap;

import org.eclipse.swt.widgets.Event;
import org.mozartspaces.capi3.AnyCoordinator;
import org.mozartspaces.capi3.LindaCoordinator;
import org.mozartspaces.capi3.LindaCoordinator.LindaSelector;
import org.mozartspaces.capi3.Selector;
import org.mozartspaces.capi3.javanative.coordination.DefaultLindaCoordinator;
import org.mozartspaces.core.Capi;
import org.mozartspaces.core.ContainerReference;
import org.mozartspaces.core.DefaultMzsCore;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.MzsConstants;
import org.mozartspaces.core.MzsCore;
import org.mozartspaces.core.MzsCoreException;
import org.mozartspaces.core.aspects.ContainerIPoint;
import org.mozartspaces.notifications.NotificationManager;
import org.mozartspaces.notifications.Operation;

import autoKonfiguration.Auto;
import autoKonfiguration.Einzelteil;
import einzelteile.*;
import fabrik.ID;
import fabrik.IDAspect;
import fabrik.IFactory;
import fabrik.NotifyCarsAspect;
import fabrik.NotifyComponentsListener;
import fabrik.xvsm.roboter.ProduktionsRoboter;

/**
 * Implementierung der RMI ToyCarFactory. Die Klasse wird als Singleton
 * instanziert und startet auch alle notwendigen RMI Services um den Robotern
 * eine gemeinsame Arbeitsumgebung zu erm\u00F6glichen.
 * 
 * @author Michael Borko
 * @see fabrik.xvsm.ICallFactory
 * @see fabrik.IFactory
 */
public class Fabrik implements IFactory, ICallFactory {

//	private HashMap<String, LinkedList<Einzelteil>> produktion = new HashMap<String, LinkedList<Einzelteil>>();
//	private HashMap<String, Auto> montage = new HashMap<String, Auto>();
//	private HashMap<String, Auto> geprueft = new HashMap<String, Auto>();
//	private HashMap<String, Auto> fertig = new HashMap<String, Auto>();
//	private HashMap<String, Auto> sammelstelle = new HashMap<String, Auto>();
	private static MzsCore core = null;
	private static Capi capi = null;
	private static ContainerReference einzelteilC = null;
	private static ContainerReference autoC = null;
	private static ContainerReference gelieferteAutosC = null;
	private static ContainerReference getesteteAutosC = null;
	private static ContainerReference kaputteAutosC = null;

	private Long counter = new Long(1);
	private static Fabrik instance = null;
	public static ICallFactory uremoteObject = null;

	private int componentNotify = 0;
	private int deliveryNotify = 0;
	private int faultyNotify = 0;

	/**
	 * Initialisierung der Fabrik-Instanz durch Aufruf der getInstance()
	 * Methode.
	 * 
	 * @param args
	 *            Keine Verwendung vorgesehen
	 */
	public static void main(String args[]) {
		getInstance();
		getInstance().shutdown();
	}

	/**
	 * Fabrik gehorcht dem Singleton-Pattern um zu vermeiden, dass mehrer
	 * Objekte dieser Klasse implementiert werden. Somit ist gesichert, dass
	 * nicht mehrere Registries innerhalb der Applikation erzeugt werden.
	 * 
	 * @return Singleton-Instanz der Klasse Fabrik
	 */
	public static Fabrik getInstance() {
		
		return new Fabrik();
	}

	/**
	 * Privater Defaultkonstruktor, da Fabrik als Singleton implementiert wurde.
	 */
	private Fabrik() {
		for (String type : getClasses("einzelteile")) {
			System.out.println("Initiating production collection with " + type);
		}
		try {
			core = DefaultMzsCore.newInstance();
			NotificationManager nm = new NotificationManager(core);
			capi = new Capi(core);
			System.out.println("Space erzeugt");
			
			ContainerReference ids = capi.createContainer("ID", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new AnyCoordinator("AnyCoordinator"));
			System.out.println(core.getConfig().getSpaceUri());
			IDAspect ida = new IDAspect();
			capi.addContainerAspect(ida, ids, ContainerIPoint.POST_READ);
			ID startID = new ID();
			startID.id = 1;
			capi.write(ids, new Entry(startID));

			//Notifications erstellen
			NotifyComponentsListener na = new NotifyComponentsListener();
			NotifyCarsAspect nca = new NotifyCarsAspect();

			// Container erstlellen
			einzelteilC = capi.createContainer("Einzelteile", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new DefaultLindaCoordinator("LindaCoordinator", true), new AnyCoordinator("AnyCoordinator"));
			nm.createNotification(einzelteilC, new NotifyComponentsListener(), Operation.DELETE, Operation.TAKE, Operation.WRITE); //capi.addContainerAspect(na, einzelTeilContainer, ContainerIPoint.PRE_WRITE);
			
			autoC = capi.createContainer("Autos", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new DefaultLindaCoordinator("LindaCoordinator", true), new AnyCoordinator("AnyCoordinator"));
			
			gelieferteAutosC = capi.createContainer("GelieferteAutos", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new DefaultLindaCoordinator("LindaCoordinator", true), new AnyCoordinator("AnyCoordinator"));
			capi.addContainerAspect(nca, gelieferteAutosC, ContainerIPoint.PRE_WRITE);
			
			getesteteAutosC = capi.createContainer("GetesteteAutos", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new DefaultLindaCoordinator("LindaCoordinator", true), new AnyCoordinator("AnyCoordinator"));
			
			kaputteAutosC = capi.createContainer("KaputteAutos", core.getConfig().getSpaceUri(), MzsConstants.Container.UNBOUNDED, null, new DefaultLindaCoordinator("LindaCoordinator", true), new AnyCoordinator("AnyCoordinator"));
			capi.addContainerAspect(nca, kaputteAutosC, ContainerIPoint.PRE_WRITE);
			
		} catch (InterruptedException ex) {
			System.err.println("Couldn't start space");
		}
		catch (MzsCoreException ex) {
			System.err.println("Couldn't start space");
		}
		
	}

	/**
	 * @see fabrik.IFactory#createComponentNotifier(int)
	 */
	@Override
	public void createComponentNotifier(int detail) {
		this.componentNotify = detail;
	}

	/**
	 * @see fabrik.IFactory#createDeliveryNotifier(int)
	 */
	@Override
	public void createDeliveryNotifier(int detail) {
		this.deliveryNotify = detail;
	}

	/**
	 * @see fabrik.IFactory#createFaultyNotifier(int)
	 */
	@Override
	public void createFaultyNotifier(int detail) {
		this.faultyNotify = detail;
	}

	/**
	 * @see fabrik.IFactory#startProduction(int, int, String)
	 */
	@Override
	public void startProduction(int quantity, int err, String type) {
		// TODO MUST :: Einfaches Starten von Produktionsroboter
//        new ProduktionsRoboter(quantity, (double) err / 100, type).start();
		new ProduktionsRoboter(capi, quantity, (double) err / 100, type).start();

		// </REMOVE>
		// TODO NICE :: Optimierungsmoeglichkeit mittels Threadpool?
	}

	/**
	 * @see fabrik.IFactory#getQuantity(String)
	 */
	@Override
	public int getQuantity(String type) {

		try {
            LindaSelector ls = null;
            switch (type) {
                case "einzelteile.Achse":
                    ls = LindaCoordinator.newSelector(new Achse(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                case "einzelteile.Bodenplatte":
                    ls = LindaCoordinator.newSelector(new Bodenplatte(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                case "einzelteile.Karosserie":
                    ls = LindaCoordinator.newSelector(new Karosserie(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                case "einzelteile.Lenkrad":
                    ls = LindaCoordinator.newSelector(new Lenkrad(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                case "einzelteile.ReifenPaar":
                    ls = LindaCoordinator.newSelector(new ReifenPaar(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                case "einzelteile.Sitz":
                    ls = LindaCoordinator.newSelector(new Sitz(0L, 0L, (boolean) false), Selector.COUNT_ALL);
                    break;
                default:
                    System.out.println("Creepy" + type);
            }
            return capi.test(einzelteilC, ls, Long.MAX_VALUE, null);
        } catch (MzsCoreException ex) {
            System.err.println(ex.getClass() + ": " + ex.getMessage());
        }
        return 0;
	}

	/**
	 * @see fabrik.IFactory#getDeliveredCars()
	 */
	@Override
	public String[] getDeliveredCars() {
		try {
            AnyCoordinator.AnySelector ls = AnyCoordinator.newSelector(Selector.COUNT_ALL);
            ArrayList<Auto> al = capi.take(gelieferteAutosC, ls, Long.MAX_VALUE, null);
            if (al.size() >= 1) {
                ArrayList<String> returnValue = new ArrayList<>();
                for (Auto auto : al) {
                    returnValue.add(auto.toString());
                }
                return returnValue.toArray(new String[returnValue.size()]);
            }
        } catch (Exception ex) {
        }
        return new String[0];
	}

	/**
	 * @see fabrik.IFactory#getFaultyCars()
	 */
	@Override
	public String[] getFaultyCars() {
		 try {
	            AnyCoordinator.AnySelector ls = AnyCoordinator.newSelector(Selector.COUNT_ALL);
	            ArrayList<Auto> al = capi.take(kaputteAutosC, ls, Long.MAX_VALUE, null);
	            if (al.size() >= 1) {
	                ArrayList<String> returnValue = new ArrayList<>();
	                for (Auto auto : al) {
	                    returnValue.add(auto.toString());
	                }
	                return returnValue.toArray(new String[returnValue.size()]);
	            }
	        } catch (Exception ex) {
	        }
	        return new String[0];
	    }

	/**
	 * @see fabrik.IFactory#shutdown()
	 */
	@Override
	public void shutdown() {
		try {
			System.out.println("Server shutdown ...");

			core.shutdown(true);
		} catch (Exception e) {
			System.err.println("Can not close Server." + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#getMenge(String)
	 */
	@Override
	public int getMenge(String type) throws RemoteException {
		return getQuantity(type);
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#getID()
	 */
	@Override
	public long getID() throws RemoteException {
		long ret = -1;
		synchronized (counter) {
			ret = counter.longValue();
			counter = new Long(counter.longValue() + 1);
		}
		return ret;
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#zusammengebaut(Auto)
	 */
	@Override
	public void zusammengebaut(Auto t) throws RemoteException {
		// TODO NEED :: Zusammengebautes Auto aufnehmen
		// <REMOVE>
//		synchronized (montage) {
//			try {
//				montage.put("" + t.getAutoID(), t);
//			} catch (Exception e) {
//				throw new RemoteException("Car not accepted!");
//			}
//		}
		// </REMOVE>
		// TODO NICE :: PruefRoboter ueber neu-produziertes Auto informieren
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#testen()
	 */
	@Override
	public Auto testen() throws RemoteException {
		Auto ret = null;
		// TODO NEED :: Auto aus Montage entnehmen und uebergeben
		// <REMOVE>
//		synchronized (montage) {
//			if (montage.size() > 0) {
//				ret = montage.values().iterator().next();
//				montage.remove("" + ret.getAutoID());
//			} else
//				throw new RemoteException("No Elements!");
//		}
		// </REMOVE>
		return ret;
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#getestet(Auto)
	 */
	@Override
	public void getestet(Auto t) throws RemoteException {
		// TODO NEED :: Geprueftes Auto aufnehmen
		// <REMOVE>
//		synchronized (geprueft) {
//			try {
//				geprueft.put("" + t.getAutoID(), t);
//			} catch (Exception e) {
//				throw new RemoteException("Car not accepted!");
//			}
//		}
		// </REMOVE>
		// TODO NICE :: LogistikRoboter ueber neu-geprueftes Auto informieren
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#transport()
	 */
	@Override
	public Auto transport() throws RemoteException {
		Auto ret = null;
		// TODO NEED :: Auto aus geprueften Pool entnehmen und uebergeben
		// <REMOVE>
//		synchronized (geprueft) {
//			if (geprueft.size() > 0) {
//				ret = geprueft.values().iterator().next();
//				geprueft.remove("" + ret.getAutoID());
//			} else
//				throw new RemoteException("No Elements!");
//		}
		// </REMOVE>
		return ret;
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#liefern(Auto)
	 */
	@Override
	public void liefern(Auto t) throws RemoteException {
		// TODO NEED :: Einfuegen des neuen Autos
		// <REMOVE>
//		synchronized (fertig) {
//			fertig.put("" + t.getAutoID(), t);
//		}
		// </REMOVE>

		Event e = new Event();
		e.detail = deliveryNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#verwerfen(Auto)
	 */
	@Override
	public void verwerfen(Auto t) throws RemoteException {
		// TODO NEED :: Einfuegen des defekten Autos
		// <REMOVE>
//		synchronized (sammelstelle) {
//			sammelstelle.put("" + t.getAutoID(), t);
//		}
		// </REMOVE>

		Event e = new Event();
		e.detail = faultyNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#setEinzelteil(String, Einzelteil)
	 */
	@Override
	public void setEinzelteil(String type, Einzelteil component)
			throws RemoteException {
		// TODO NEED :: Einfuegen des neuen Einzelteils
		// <REMOVE>
//		synchronized (produktion) {
//			produktion.get(type).add(component);
//		}
		// </REMOVE>

		Event e = new Event();
		e.detail = componentNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.xvsm.ICallFactory#getEinzelteil(String)
	 */
	@Override
	public Einzelteil getEinzelteil(String type) throws RemoteException {
		Einzelteil ret = null;
		// TODO NEED :: Abrufen eines Einzelteils
		// <REMOVE>
//		synchronized (produktion) {
//			if (produktion.get(type).size() > 0)
//				ret = produktion.get(type).removeLast();
//			else
//				throw new RemoteException("No Elements!");
//		}
		// </REMOVE>

		Event e = new Event();
		e.detail = componentNotify;
		gui.MainDisplay.listener.handleEvent(e);
		return ret;
	}

	/**
	 * Scans all classes accessible from the context class loader which belong
	 * to the given package and subpackages. <a
	 * href="http://snippets.dzone.com/posts/show/4831">snippets.dzone</a>
	 * 
	 * @param packageName
	 *            The base package
	 * @return The classes
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	@SuppressWarnings("rawtypes")
	private static String[] getClasses(String packageName) {
//		try {
//			ClassLoader classLoader = Thread.currentThread()
//					.getContextClassLoader();
//			assert classLoader != null;
//			String path = packageName.replace('.', '/');
//			Enumeration<URL> resources = classLoader.getResources(path);
//			List<File> dirs = new ArrayList<File>();
//			while (resources.hasMoreElements()) {
//				URL resource = resources.nextElement();
//				dirs.add(new File(resource.getFile()));
//			}
//			ArrayList<Class> classes = new ArrayList<Class>();
//			for (File directory : dirs) {
//				classes.addAll(findClasses(directory, packageName));
//			}
//			ArrayList<String> ret = new ArrayList<String>();
//			for (Class className : classes) {
//				ret.add(className.getName());
//			}
//			return ret.toArray(new String[ret.size()]);
//		} catch (Exception ex) {
//			return null;
//		}
		String[] wert = new String[6];
		wert[0] = "einzelteile.Achse";
		wert[1] = "einzelteile.Bodenplatte";
		wert[2] = "einzelteile.Karosserie";
		wert[3] = "einzelteile.Lenkrad";
		wert[4] = "einzelteile.ReifenPaar";
		wert[5] = "einzelteile.Sitz";
		return wert;

	}

	/**
	 * Recursive method used to find all classes in a given directory and
	 * subdirs. <a
	 * href="http://snippets.dzone.com/posts/show/4831">snippets.dzone</a>
	 * 
	 * @param directory
	 *            The base directory
	 * @param packageName
	 *            The package name for classes found inside the base directory
	 * @return the classes
	 * @throws ClassNotFoundException
	 */
	@SuppressWarnings("rawtypes")
	private static List<Class> findClasses(File directory, String packageName)
			throws ClassNotFoundException {
		List<Class> classes = new ArrayList<Class>();
		if (!directory.exists()) {
			return classes;
		}
		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file,
						packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(Class.forName(packageName
						+ '.'
						+ file.getName().substring(0,
								file.getName().length() - 6)));
			}
		}
		return classes;
	}
}
