package org.corfudb.runtime.view;

import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import io.netty.buffer.ByteBuf;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.corfudb.protocols.wireprotocol.ILogUnitEntry;
import org.corfudb.protocols.wireprotocol.IMetadata;
import org.corfudb.protocols.wireprotocol.LogUnitReadResponseMsg;
import org.corfudb.runtime.CorfuRuntime;
import org.corfudb.runtime.exceptions.OverwriteException;
import org.corfudb.util.Utils;


/** All replication views must inherit from this class.
 *
 * This class takes a layout as a constructor and provides an address space with
 * the correct replication view given a layout and mode.
 *
 * Created by mwei on 12/11/15.
 */
@Slf4j
public abstract class AbstractReplicationView {

    public static AbstractReplicationView getReplicationView(Layout l, Layout.ReplicationMode mode)
    {
        switch (mode)
        {
            case CHAIN_REPLICATION:
                return new ChainReplicationView(l);
            case QUORUM_REPLICATION:
                log.warn("Quorum replication is not yet supported!");
                break;
        }
        log.error("Unknown replication mode {} selected.", mode);
        throw new RuntimeException("Unsupported replication mode.");
    }

    @ToString
    @RequiredArgsConstructor
    public static class ReadResult {
        @Getter
        final long address;
        @Getter
        final ILogUnitEntry result;
    }

    @ToString
    @RequiredArgsConstructor
    public static class CachedLogUnitEntry implements ILogUnitEntry
    {
        @Getter
        final LogUnitReadResponseMsg.ReadResultType resultType;

        @Getter
        final EnumMap<LogUnitMetadataType, Object> metadataMap = new EnumMap<>(IMetadata.LogUnitMetadataType.class);

        @Getter
        final Object payload;

        public Object getPayload(CorfuRuntime rt)
        {
            return getPayload();
        }

        @Getter
        final int sizeEstimate;

        /**
         * Gets a ByteBuf representing the payload for this data.
         *
         * @return A ByteBuf representing the payload for this data.
         */
        @Override
        public ByteBuf getBuffer() {
            log.warn("Attempted to get a buffer of a cached entry!");
            throw new RuntimeException("Invalid attempt to get the ByteBuf of a cached entry!");
        }
    }

    @Getter
    public final Layout layout;

    public AbstractReplicationView(Layout layout)
    {
        this.layout = layout;
    }

    /** Write the given object to an address and streams, using the replication method given.
     *
     * @param address   An address to write to.
     * @param stream    The streams which will belong on this entry.
     * @param data      The data to write.
     */
    public void write(long address, Set<UUID> stream, Object data)
        throws OverwriteException
    {
        write(address, stream, data, Collections.emptyMap());
    }

    /** Write the given object to an address and streams, using the replication method given.
     *
     * @param address           An address to write to.
     * @param stream            The streams which will belong on this entry.
     * @param data              The data to write.
     * @param backpointerMap    The map of backpointers to write.
     *
     * @return The number of bytes that was remotely written.
     */
    public abstract int write(long address, Set<UUID> stream, Object data, Map<UUID, Long> backpointerMap)
        throws OverwriteException;

    /** Read the given object from an address, using the replication method given.
     *
     * @param address   The address to read from.
     * @return          The result of the read.
     */
    public abstract ReadResult read(long address);

    /** Read a set of addresses, using the replication method given.
     *
     * @param addresses   The addresses to read from.
     * @return            A map containing the results of the read.
     */
    public Map<Long, ReadResult> read(RangeSet<Long> addresses) {
        Map<Long,ReadResult> results = new ConcurrentHashMap<>();
        Set<Long> total = Collections.newSetFromMap(new ConcurrentHashMap<>());
        for (Range<Long> r : addresses.asRanges())
        {
            total.addAll(Utils.discretizeRange(r));
        }
        total.parallelStream()
                .forEach(i -> results.put(i, read(i)));
        return results;
    }

    /** Fill a hole at an address, using the replication method given.
     *
     * @param address   The address to hole fill at.
     */
    public abstract void fillHole(long address)
        throws OverwriteException;
}
