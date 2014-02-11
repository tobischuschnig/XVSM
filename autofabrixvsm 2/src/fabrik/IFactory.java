package fabrik;

/**
 * Interface der ToyCarFactory, die von allen alternativen Technologien
 * implementiert werden muss. Die GUI basiert auf diesen Methoden.
 * 
 * @author Michael Borko
 */
public interface IFactory {

	/**
	 * Hier wird eine M\u00F6glichkeit geschaffen, um die GUI zu informieren,
	 * dass sich bei den Einzelteilen eine \u00C4nderung ergeben hat, und somit
	 * die Tabelle refresht werden muss.
	 * 
	 * @param detail
	 *            Wert zur Identifizierung einer \u00C4nderung der
	 *            Einzelteil-Tabelle
	 */
	public void createComponentNotifier(int detail);

	/**
	 * Hier wird eine M\u00F6glichkeit geschaffen, um die GUI zu informieren,
	 * dass sich bei den fertiggestellten Autos eine \u00C4nderung ergeben hat,
	 * und somit die Tabelle refresht werden muss.
	 * 
	 * @param detail
	 *            Wert zur Identifizierung einer \u00C4nderung der
	 *            Delivery-Tabelle
	 */
	public void createDeliveryNotifier(int detail);

	/**
	 * Hier wird eine M\u00F6glichkeit geschaffen, um die GUI zu informieren,
	 * dass sich bei den fehlerhaften Autos (Sammelstelle) eine \u00C4nderung
	 * ergeben hat, und somit die Tabelle refresht werden muss.
	 * 
	 * @param detail
	 *            Wert zur Identifizierung einer \u00C4nderung der
	 *            Faulty-Tabelle
	 */
	public void createFaultyNotifier(int detail);

	/**
	 * Produktionsbefehl an die Fabrik.
	 * 
	 * @param quantity
	 *            Anzahl der Einzelteile
	 * @param err
	 *            Fehlerrate
	 * @param type
	 *            Art des Einzelteils
	 */
	public void startProduction(int quantity, int err, String type);

	/**
	 * Anzahl eines gewissen Einzelteils im Produktionsspeicher der Fabrik.
	 * 
	 * @param type
	 *            Art des Einzelteils
	 * @return
	 */
	public int getQuantity(String type);

	/**
	 * GUI erfragt die Fabrik nach den fertiggestellten und zur Auslieferung
	 * bereiten Autos. Die Abfrage kommt einer Lieferung gleich und entfernt
	 * somit die Autos aus der Fabrik.
	 * 
	 * @return Aufbereiteter String der zu liefernden Autos
	 */
	public String[] getDeliveredCars();

	/**
	 * GUI erfragt die Fabrik nach den defekten und in der Sammelstelle
	 * aufbewahrten Autos. Die Abfrage kommt einer Lieferung gleich und entfernt
	 * somit die Autos aus der Fabrik.
	 * 
	 * @return Aufbereiteter String der defekten Autos
	 */
	public String[] getFaultyCars();

	/**
	 * Befehl an die Fabrik um die gesamte Produktion sauber abzuschlie\u00DFen.
	 */
	public void shutdown();
}
