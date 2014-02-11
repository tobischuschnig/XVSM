package fabrik.xvsm.roboter;

import autoKonfiguration.Auto;

/**
 * @author Michael Borko
 * @see fabrik.xvsm.roboter.PruefRoboter
 */
public class PruefRoboterGewicht extends PruefRoboter {

	@Override
	public boolean isAutoOK(Auto auto) {
		auto.setPrueferGewichtID(id);

		return true;
	}

}
