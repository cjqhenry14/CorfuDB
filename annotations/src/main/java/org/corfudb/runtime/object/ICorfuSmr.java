package org.corfudb.runtime.object;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/** The interface for an object interfaced with SMR.
 *
 * <p>Created by mwei on 11/10/16.
 * @param <T> The type of the underlying object.
 */
public interface ICorfuSmr<T> {

    /** The suffix for all precompiled SMR wrapper classes. */
    String CORFUSMR_SUFFIX = "$CORFUSMR";

    /** Get the proxy for this wrapper, to manage the state of the object.
     * @return The proxy for this wrapper. */
    ICorfuSmrProxy<T> getCorfuSmrProxy();

    /** Set the proxy for this wrapper, to manage the state of the object.
     * @param proxy The proxy to set for this wrapper. */
    void setCorfuSmrProxy(ICorfuSmrProxy<T> proxy);

    /** Get a map from strings (function names) to SMR upcalls.
     * @return The SMR upcall map. */
    Map<String, ICorfuSmrUpcallTarget<T>> getCorfuSmrUpcallMap();

    /** Get a map from strings (function names) to undo methods.
     * @return The undo map. */
    Map<String, IUndoFunction<T>> getCorfuUndoMap();

    /** Get a map from strings (function names) to undoRecord methods.
     * @return The undo record map. */
    Map<String, IUndoRecordFunction<T>> getCorfuUndoRecordMap();

    /** Get a set of strings (function names) which result in a reset
     * of the object.
     * @return  The set of strings that cause a reset on the object.
     */
    Set<String> getCorfuResetSet();

    /** Return the stream ID that this object belongs to.
     * @return The stream ID this object belongs to. */
    default UUID getCorfuStreamId() {
        return getCorfuSmrProxy().getStreamId();
    }
}