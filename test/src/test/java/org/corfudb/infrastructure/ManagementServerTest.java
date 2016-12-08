package org.corfudb.infrastructure;

import org.corfudb.protocols.wireprotocol.CorfuMsgType;
import org.corfudb.protocols.wireprotocol.FailureDetectorMsg;
import org.corfudb.runtime.view.Layout;
import org.junit.After;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Checks the various services and messages handled by the
 * Management Server.
 * <p>
 * Created by zlokhandwala on 11/2/16.
 */
public class ManagementServerTest extends AbstractServerTest<ManagementServer> {

    public ManagementServerTest() {
        super(ManagementServer::new);
    }

    /**
     * Testing the status of the failure detector and shutdown functionality.
     */
    @Test
    public void checkFailureDetectorStatus() {
    }

    /**
     * Bootstrapping the management server multiple times.
     */
    @Test
    public void bootstrapManagementServer() {
        Layout layout = TestLayoutBuilder.single(SERVERS.PORT_0);
        sendMessage(CorfuMsgType.MANAGEMENT_BOOTSTRAP.payloadMsg(layout));
        assertThat(getLastMessage().getMsgType()).isEqualTo(CorfuMsgType.ACK_RESPONSE);
        sendMessage(CorfuMsgType.MANAGEMENT_BOOTSTRAP.payloadMsg(layout));
        assertThat(getLastMessage().getMsgType()).isEqualTo(CorfuMsgType.MANAGEMENT_ALREADY_BOOTSTRAP);
    }

    /**
     * Triggering the failure handler with and without bootstrapping the server.
     */
    @Test
    public void triggerFailureHandler() {
        Layout layout = TestLayoutBuilder.single(SERVERS.PORT_0);
        Map<String, Boolean> map = new HashMap<>();
        map.put("key", true);
        sendMessage(CorfuMsgType.MANAGEMENT_FAILURE_DETECTED.payloadMsg(new FailureDetectorMsg(map)));
        assertThat(getLastMessage().getMsgType()).isEqualTo(CorfuMsgType.MANAGEMENT_NOBOOTSTRAP);
        sendMessage(CorfuMsgType.MANAGEMENT_BOOTSTRAP.payloadMsg(layout));
        assertThat(getLastMessage().getMsgType()).isEqualTo(CorfuMsgType.ACK_RESPONSE);
        sendMessage(CorfuMsgType.MANAGEMENT_FAILURE_DETECTED.payloadMsg(new FailureDetectorMsg(map)));
        assertThat(getLastMessage().getMsgType()).isEqualTo(CorfuMsgType.ACK_RESPONSE);
    }
}