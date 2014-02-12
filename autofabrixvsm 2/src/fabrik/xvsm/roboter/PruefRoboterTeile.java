package fabrik.xvsm.roboter;

import autoKonfiguration.Auto;

/**
 * @author Michael Borko
 * @see fabrik.rmi.roboter.PruefRoboter
 */
public class PruefRoboterTeile extends PruefRoboter {

    @Override
    public boolean isAutoOK(Auto auto) {
        auto.setPrueferDefekteID(id);

        if (auto.isDefekt()) {
            return false;
        }

        boolean defekt = false;

        if (auto.getAchseVorn().istDefekt()) {
            defekt = true;
        }
        if (auto.getAchseHinten().istDefekt()) {
            defekt = true;
        }
        if (auto.getReifenPaarVorn().istDefekt()) {
            defekt = true;
        }
        if (auto.getReifenPaarHinten().istDefekt()) {
            defekt = true;
        }
        if (auto.getBodenplatte().istDefekt()) {
            defekt = true;
        }
        if (auto.getSitz().istDefekt()) {
            defekt = true;
        }
        if (auto.getKarosserie().istDefekt()) {
            defekt = true;
        }
        if (auto.getLenkrad().istDefekt()) {
            defekt = true;
        }

        if (defekt) {
            auto.setDefekt(true);
            return false;
        }

        return true;
    }
}
