package fabrik.rmi.roboter;

import autoKonfiguration.Auto;

/**
 * @author Michael Borko
 * @see fabrik.rmi.roboter.PruefRoboter
 */
public class PruefRoboterGewicht extends PruefRoboter {

	@Override
	public boolean isAutoOK(Auto auto) {
		auto.setPrueferGewichtID(id);

		return true;
	}

}
