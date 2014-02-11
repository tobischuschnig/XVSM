package fabrik.xvsm;

import java.rmi.Remote;
import java.rmi.RemoteException;

import org.mozartspaces.core.Capi;
import org.mozartspaces.core.TransactionReference;

import autoKonfiguration.Auto;
import autoKonfiguration.Einzelteil;

/**
 * Remote Interface der RMI-Fabrik.
 * 
 * @author Michael Borko
 */
public interface ICallFactory extends Remote {

	/**
	 * Die Fabrik verwaltet die eindeutigen IDs. Um als Roboter eine ID zu
	 * erhalten, wird diese Methode aufgerufen.
	 * 
	 * @return Systemweit-eindeutige ID
	 * @throws RemoteException
	 */
	public long getID() throws RemoteException;

	/**
	 * MontageRoboter ben\u00F6tigen die Anzahl der Einzelteile eines speziellen
	 * Typen. Hier erhalten sie diese Anzahl als int-Wert.
	 * 
	 * @param type
	 *            Name des Einzelteils
	 * @return Menge der produzierten Einteile, entsprechend dem
	 *         \u00FCbergebenen Typen
	 * @throws RemoteException
	 */
	public int getMenge(String type) throws RemoteException;

	/**
	 * ProduktionsRoboter \u00FCbergeben hier die produzierten Einzelteile an
	 * die Fabrik. MontageRoboter m\u00FCssen die nicht verwendeten Teile wieder
	 * zur\u00FClegen.
	 * 
	 * @param type
	 *            Typ des Einzelteils
	 * @param e
	 *            Objekt Einzeilteil
	 * @throws RemoteException
	 */
	public void setEinzelteil(String type, Einzelteil e) throws RemoteException;

	/**
	 * MontageRoboter verbrauchen die hergestellten Einzelteile um Autos
	 * herzustellen.
	 * 
	 * @param type
	 *            Name des Einzelteils
	 * @return Gew\u00FCnschtes Einzelteil
	 * @throws RemoteException
	 */
	public Einzelteil getEinzelteil(String type,TransactionReference tx) throws RemoteException;

	/**
	 * MontageRoboter \u00FCbergeben die fertiggestellten Autos an die Fabrik.
	 * 
	 * @param a
	 *            Fertiggestelltes Auto
	 * @throws RemoteException
	 */
	public void zusammengebaut(Auto a) throws RemoteException;

	/**
	 * TestRoboter erbittet von der Fabrik ein fertiggestelltes Auto zum Test.
	 * 
	 * @return Zu testendes Auto
	 * @throws RemoteException
	 */
	public Auto testen() throws RemoteException;

	/**
	 * TestRoboter \u00FCbergibt getestetes Auto wieder an die Fabrik.
	 * 
	 * @param a
	 *            Getestetes Auto
	 * @throws RemoteException
	 */
	public void getestet(Auto a) throws RemoteException;

	/**
	 * Wird von LogistikRoboter aufgerufen, um ein Auto zu erhalten. Der Roboter
	 * muss dann \u00FCberpr\u00FCfen, ob das Auto fehlerhaft ist oder nicht.
	 * 
	 * @return Auto
	 * @throws RemoteException
	 */
	public Auto transport() throws RemoteException;

	/**
	 * Nach der \u00DCberpr\u00FCfung des Autos, ruft der Logistikroboter diese
	 * Methode auf, um das fehlerfreie Auto in der Fabrik in den
	 * "Auslieferungs"-Speicher zu legen.
	 * 
	 * @param a
	 *            Fehlerfreies Auto, welches geliefert werden kann.
	 * @throws RemoteException
	 */
	public void liefern(Auto a) throws RemoteException;

	/**
	 * Nach der \u00DCberpr\u00FCfung des Autos, ruft der Logistikroboter diese
	 * Methode auf, um das fehlerhafte Auto in der Fabrik in der Sammelstelle
	 * abzugeben.
	 * 
	 * @param a
	 *            Fehlerhaftes Auto, welches in der Sammelstelle abgegeben wird.
	 * @throws RemoteException
	 */
	public void verwerfen(Auto a) throws RemoteException;

	public Capi getCapi();
}
