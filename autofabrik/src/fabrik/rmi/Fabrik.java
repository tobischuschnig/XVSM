package fabrik.rmi;

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

import autoKonfiguration.Auto;
import autoKonfiguration.Einzelteil;

import fabrik.IFactory;
import fabrik.rmi.roboter.ProduktionsRoboter;

/**
 * Implementierung der RMI ToyCarFactory. Die Klasse wird als Singleton
 * instanziert und startet auch alle notwendigen RMI Services um den Robotern
 * eine gemeinsame Arbeitsumgebung zu erm\u00F6glichen.
 * 
 * @author Michael Borko
 * @see fabrik.rmi.ICallFactory
 * @see fabrik.IFactory
 */
public class Fabrik implements IFactory, ICallFactory {

	private HashMap<String, LinkedList<Einzelteil>> produktion = new HashMap<String, LinkedList<Einzelteil>>();
	private HashMap<String, Auto> montage = new HashMap<String, Auto>();
	private HashMap<String, Auto> geprueft = new HashMap<String, Auto>();
	private HashMap<String, Auto> fertig = new HashMap<String, Auto>();
	private HashMap<String, Auto> sammelstelle = new HashMap<String, Auto>();

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
		if (instance == null) {
			if (System.getSecurityManager() == null) {
				System.setSecurityManager(new SecurityManager());
			}
			try {
				instance = new Fabrik();

				// TODO MUST :: Initialisierung des UnicastRemoteObjects und
				// Erstellung der Registry
				// <REMOVE>
				uremoteObject = instance;
				ICallFactory stub = (ICallFactory) UnicastRemoteObject
						.exportObject(uremoteObject, 0);
				Registry registry = LocateRegistry
						.createRegistry(Config.registryPort);
				registry.rebind(Config.unicastRemoteObjectName, stub);
				// </REMOVE>

				System.out.println("CallFactory bound");
			} catch (RemoteException re) {
				System.err.println("CallFactory already bound?"
						+ " Check your RMI-Registry settings!");
				System.exit(1);
			} catch (Exception e) {
				System.err.println("CallFactory exception:");
				e.printStackTrace();
				System.exit(1);
			}
		}
		return instance;
	}

	/**
	 * Privater Defaultkonstruktor, da Fabrik als Singleton implementiert wurde.
	 */
	private Fabrik() {
		for (String type : getClasses("einzelteile")) {
			System.out.println("Initiating production collection with " + type);
			produktion.put(type, new LinkedList<Einzelteil>());
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
		// <REMOVE>
		new ProduktionsRoboter(quantity, (double) err / 100, type).start();
		// </REMOVE>
		// TODO NICE :: Optimierungsmoeglichkeit mittels Threadpool?
	}

	/**
	 * @see fabrik.IFactory#getQuantity(String)
	 */
	@Override
	public int getQuantity(String type) {

		// TODO NEED :: Abrufen der Anzahl eines Einzelteils
		// <REMOVE>
		synchronized (produktion) {
			try {
				return produktion.get(type).size();
			} catch (Exception e) {
				return 0;
			}
		}
		// </REMOVE>
	}

	/**
	 * @see fabrik.IFactory#getDeliveredCars()
	 */
	@Override
	public String[] getDeliveredCars() {
		ArrayList<String> ret = new ArrayList<String>();

		// TODO NEED :: Abrufen der fertigen Autos
		// <REMOVE>
		synchronized (fertig) {
			for (Auto t : fertig.values()) {
				ret.add(t.toString());
				fertig.remove("" + t.getAutoID());
			}
		}
		// </REMOVE>

		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * @see fabrik.IFactory#getFaultyCars()
	 */
	@Override
	public String[] getFaultyCars() {
		ArrayList<String> ret = new ArrayList<String>();

		// TODO NEED :: Abrufen der defekten Autos
		// <REMOVE>
		synchronized (sammelstelle) {
			for (Auto t : sammelstelle.values()) {
				ret.add(t.toString());
				sammelstelle.remove("" + t.getAutoID());
			}
		}
		// </REMOVE>

		return ret.toArray(new String[ret.size()]);
	}

	/**
	 * @see fabrik.IFactory#shutdown()
	 */
	@Override
	public void shutdown() {
		try {
			System.out.println("Server shutdown ...");
			// TODO MUST :: Sauberer Shutdown
			// <REMOVE>
			UnicastRemoteObject.unexportObject(uremoteObject, true);
			LocateRegistry.getRegistry(Config.registryPort).unbind(
					Config.unicastRemoteObjectName);
			// </REMOVE>
			// TODO NICE :: Shutdown all active threads
		} catch (Exception e) {
			System.err.println("Can not close Server." + e.getMessage());
			System.exit(1);
		}
	}

	/**
	 * @see fabrik.rmi.ICallFactory#getMenge(String)
	 */
	@Override
	public int getMenge(String type) throws RemoteException {
		return getQuantity(type);
	}

	/**
	 * @see fabrik.rmi.ICallFactory#getID()
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
	 * @see fabrik.rmi.ICallFactory#zusammengebaut(Auto)
	 */
	@Override
	public void zusammengebaut(Auto t) throws RemoteException {
		// TODO NEED :: Zusammengebautes Auto aufnehmen
		// <REMOVE>
		synchronized (montage) {
			try {
				montage.put("" + t.getAutoID(), t);
			} catch (Exception e) {
				throw new RemoteException("Car not accepted!");
			}
		}
		// </REMOVE>
		// TODO NICE :: PruefRoboter ueber neu-produziertes Auto informieren
	}

	/**
	 * @see fabrik.rmi.ICallFactory#testen()
	 */
	@Override
	public Auto testen() throws RemoteException {
		Auto ret = null;
		// TODO NEED :: Auto aus Montage entnehmen und uebergeben
		// <REMOVE>
		synchronized (montage) {
			if (montage.size() > 0) {
				ret = montage.values().iterator().next();
				montage.remove("" + ret.getAutoID());
			} else
				throw new RemoteException("No Elements!");
		}
		// </REMOVE>
		return ret;
	}

	/**
	 * @see fabrik.rmi.ICallFactory#getestet(Auto)
	 */
	@Override
	public void getestet(Auto t) throws RemoteException {
		// TODO NEED :: Geprueftes Auto aufnehmen
		// <REMOVE>
		synchronized (geprueft) {
			try {
				geprueft.put("" + t.getAutoID(), t);
			} catch (Exception e) {
				throw new RemoteException("Car not accepted!");
			}
		}
		// </REMOVE>
		// TODO NICE :: LogistikRoboter ueber neu-geprueftes Auto informieren
	}

	/**
	 * @see fabrik.rmi.ICallFactory#transport()
	 */
	@Override
	public Auto transport() throws RemoteException {
		Auto ret = null;
		// TODO NEED :: Auto aus geprueften Pool entnehmen und uebergeben
		// <REMOVE>
		synchronized (geprueft) {
			if (geprueft.size() > 0) {
				ret = geprueft.values().iterator().next();
				geprueft.remove("" + ret.getAutoID());
			} else
				throw new RemoteException("No Elements!");
		}
		// </REMOVE>
		return ret;
	}

	/**
	 * @see fabrik.rmi.ICallFactory#liefern(Auto)
	 */
	@Override
	public void liefern(Auto t) throws RemoteException {
		// TODO NEED :: Einfuegen des neuen Autos
		// <REMOVE>
		synchronized (fertig) {
			fertig.put("" + t.getAutoID(), t);
		}
		// </REMOVE>

		Event e = new Event();
		e.detail = deliveryNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.rmi.ICallFactory#verwerfen(Auto)
	 */
	@Override
	public void verwerfen(Auto t) throws RemoteException {
		// TODO NEED :: Einfuegen des defekten Autos
		// <REMOVE>
		synchronized (sammelstelle) {
			sammelstelle.put("" + t.getAutoID(), t);
		}
		// </REMOVE>

		Event e = new Event();
		e.detail = faultyNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.rmi.ICallFactory#setEinzelteil(String, Einzelteil)
	 */
	@Override
	public void setEinzelteil(String type, Einzelteil component)
			throws RemoteException {
		// TODO NEED :: Einfuegen des neuen Einzelteils
		// <REMOVE>
		synchronized (produktion) {
			produktion.get(type).add(component);
		}
		// </REMOVE>

		Event e = new Event();
		e.detail = componentNotify;
		gui.MainDisplay.listener.handleEvent(e);
	}

	/**
	 * @see fabrik.rmi.ICallFactory#getEinzelteil(String)
	 */
	@Override
	public Einzelteil getEinzelteil(String type) throws RemoteException {
		Einzelteil ret = null;
		// TODO NEED :: Abrufen eines Einzelteils
		// <REMOVE>
		synchronized (produktion) {
			if (produktion.get(type).size() > 0)
				ret = produktion.get(type).removeLast();
			else
				throw new RemoteException("No Elements!");
		}
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
		try {
			ClassLoader classLoader = Thread.currentThread()
					.getContextClassLoader();
			assert classLoader != null;
			String path = packageName.replace('.', '/');
			Enumeration<URL> resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<File>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			ArrayList<Class> classes = new ArrayList<Class>();
			for (File directory : dirs) {
				classes.addAll(findClasses(directory, packageName));
			}
			ArrayList<String> ret = new ArrayList<String>();
			for (Class className : classes) {
				ret.add(className.getName());
			}
			return ret.toArray(new String[ret.size()]);
		} catch (Exception ex) {
			return null;
		}

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
