package org.corfudb.runtime.collections;

import org.corfudb.runtime.CorfuDBRuntime;
import org.corfudb.runtime.stream.IStream;
import org.corfudb.runtime.stream.SimpleStream;
import org.corfudb.runtime.view.*;
import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import static org.assertj.core.api.Assertions.*;
/**
 * Created by mwei on 5/1/15.
 */
public class CDBSimpleMapTest {

    SimpleStream s;
    IWriteOnceAddressSpace woas;
    IStreamingSequencer ss;
    CDBSimpleMap<Integer, Integer> testMap;
    UUID streamID;
    @Before
    public void generateStream() throws Exception
    {
        CorfuDBRuntime cdr = new CorfuDBRuntime("memory");
        ConfigurationMaster cm = new ConfigurationMaster(cdr);
        cm.resetAll();
        woas = new WriteOnceAddressSpace(cdr);
        ss = new StreamingSequencer(cdr);
        streamID = UUID.randomUUID();
        s = new SimpleStream(streamID, ss, woas);
        testMap = new CDBSimpleMap<Integer, Integer>(s);
    }

    @Test
    public void mapIsPuttableGettable()
    {
        testMap.put(0, 10);
        testMap.put(10, 20);
        assertThat(testMap.get(0))
                .isEqualTo(10);
        assertThat(testMap.get(10))
                .isEqualTo(20);
    }


    @Test
    public void multipleMapsContainSameData() throws Exception
    {
        testMap.put(0, 10);
        testMap.put(10, 100);
        IStream s2 = new SimpleStream(streamID, ss, woas);
        CDBSimpleMap<Integer,Integer> testMap2 = new CDBSimpleMap<Integer,Integer>(s2);
        assertThat(testMap2.get(0))
                .isEqualTo(10);
        assertThat(testMap2.get(10))
                .isEqualTo(100);
    }

    @Test
    public void ensureMutatorAccessorsWork() throws Exception
    {
        testMap.put(0, 10);
        assertThat(testMap.put(0, 100))
                .isEqualTo(10);
    }
}