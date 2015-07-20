package blog;

import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import quickfix.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.google.common.base.Charsets.UTF_8;
import static com.google.common.io.Resources.getResource;
import static com.jayway.awaitility.Awaitility.await;
import static org.assertj.core.api.Assertions.assertThat;
import static quickfix.Session.lookupSession;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MainApplication.class)
public class ReceiverTest {

    @Autowired
    Receiver receiver;

    Message message;

    SocketInitiator initiator;
    private Session session;

    @Before
    public void setup() throws Exception {
        startInitiator();
        await().until(() -> initiator.isLoggedOn());
        session = lookupSession(initiator.getSessions().get(0));
        System.out.println("initiator sender session: " + session);
    }

    private void startInitiator() throws ConfigError {
        SessionSettings initiatorSettings = new SessionSettings("fix-gateway-initiator.properties");
        MemoryStoreFactory memoryStoreFactory = new MemoryStoreFactory();
        FileLogFactory fileLogFactory = new FileLogFactory(initiatorSettings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        initiator = new SocketInitiator(new ApplicationAdapter(), memoryStoreFactory, initiatorSettings, fileLogFactory, messageFactory);
        initiator.start();
    }

    @Test
    public void receivesMessage() throws Exception {
        message = MessageUtils.parse(session, read("test.txt"));

        session.send(message);
        System.out.println("Successfully sent message:......" + message);
        await().until(() -> receiver.hasReceivedMessage());

        Message receivedMessage = receiver.getReceivedMessage();
        assertThat(receivedMessage.toString()).isEqualTo(message.toString());
    }

    private String read(String fileName) throws IOException {
        return Resources.toString(getResource(fileName), UTF_8);
    }

}