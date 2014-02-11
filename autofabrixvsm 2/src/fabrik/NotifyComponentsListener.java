/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author Tobias Schuschnig
 */
public class NotifyComponentsListener implements NotificationListener {

	
	//TODO Funktioniert nicht bearbeiten
    /*@Override
    public AspectResult preWrite(WriteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3, int executionCount) {
        Event e = new Event();
        e.detail = 1;
        gui.MainDisplay.listener.handleEvent(e);
        return AspectResult.OK;
    }*/

    @Override
    public void entryOperationFinished(Notification ntfctn, Operation oprtn, List<? extends Serializable> list) {
        Event e = new Event();
        e.detail = 1;
        gui.MainDisplay.listener.handleEvent(e);
    }
}
