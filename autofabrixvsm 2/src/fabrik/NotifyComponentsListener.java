package fabrik;

import java.io.Serializable;
import java.util.List;
import org.eclipse.swt.widgets.Event;
import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;
import org.mozartspaces.notifications.Notification;
import org.mozartspaces.notifications.NotificationListener;
import org.mozartspaces.notifications.Operation;

/**
 * Aspekt informiert die GUI wenn neue Teile vorhanden sind.
 * @author Tobias Schuschnig
 */
public class NotifyComponentsListener implements NotificationListener {



    @Override
    public void entryOperationFinished(Notification ntfctn, Operation oprtn, List<? extends Serializable> list) {
        Event e = new Event();
        e.detail = 1;
        gui.MainDisplay.listener.handleEvent(e);
    }
}
