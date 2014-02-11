package fabrik;

import org.eclipse.swt.widgets.Event;
import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.WriteEntriesRequest;

/**
 *
 * @author Tobias Schuschnig
 */
public class NotifyCarsAspect extends AbstractContainerAspect {

    @Override
    public AspectResult preWrite(WriteEntriesRequest request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3, int executionCount) {
        Event e = new Event();
        e.detail = 2;
        gui.MainDisplay.listener.handleEvent(e);
        e.detail = 3;
        gui.MainDisplay.listener.handleEvent(e);
        return AspectResult.OK;
    }
}
