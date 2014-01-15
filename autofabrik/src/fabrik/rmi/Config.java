package fabrik.rmi;

/**
 * Konfigurationspool der RMI ToyCarFactory-Implementierung.
 * 
 * @author Michael Borko
 */
public class Config {
	/**
	 * Name des zu bindenden UnicastRemoteObjects der
	 * ToyCarFactory-Implementierung
	 */
	public static String unicastRemoteObjectName = "CallFactory";
	/**
	 * Default-Port der intern erstellten Registry. Wert ist auf 12345 gesetzt.
	 */
	public static final int registryPort = 12345;
}
