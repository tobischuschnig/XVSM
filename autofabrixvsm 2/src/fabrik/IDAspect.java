package fabrik;

import java.io.Serializable;
import java.util.List;
import org.mozartspaces.capi3.Capi3AspectPort;
import org.mozartspaces.capi3.SubTransaction;
import org.mozartspaces.capi3.Transaction;
import org.mozartspaces.core.Entry;
import org.mozartspaces.core.aspects.AbstractContainerAspect;
import org.mozartspaces.core.aspects.AspectResult;
import org.mozartspaces.core.requests.ReadEntriesRequest;

/**
 * Aspekte sind eine Ergaenzung eines Containers die gewisse Hintergrundaktivitaeten bewaeltigen. 
 * Diese werden entweder beim lesen oder schreiben in einen Container ausgeloest und koennen vor 
 * oder nach der Aktion ausgefuehrt werden.
 * In diesem Beispiel verwenden wir die Aspekte um die ID‘s global eindeutig zu gestalten 
 * und im Space zu schreiben. Nachdem eine ID verwendet wurde schaltet sich der Aspekt ein 
 * um die ID zu erhoehen.
 * @author Tobias Schuschnig
 */
public class IDAspect extends AbstractContainerAspect {

    @Override
    public AspectResult postRead(ReadEntriesRequest<?> request, Transaction tx, SubTransaction stx, Capi3AspectPort capi3, int executionCount, List<Serializable> entries) {
        try {
            ID idNow = (ID) entries.get(0);
            idNow.id += 1L;
            entries.set(0, new Entry(idNow));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return AspectResult.OK;
    }
}
