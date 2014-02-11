package fabrik;

import java.io.Serializable;
import org.mozartspaces.capi3.Queryable;

/**
 *
 * @author Tobias Schuschnig
 */
@Queryable(autoindex = true)
public class ID implements Serializable {

    public long id;
}
