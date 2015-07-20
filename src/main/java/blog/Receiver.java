package blog;

import org.springframework.stereotype.Component;
import quickfix.*;
import quickfix.Application;
import quickfix.fix41.Reject;
import quickfix.fix44.Logon;
import quickfix.fix41.NewOrderSingle;

import javax.annotation.PostConstruct;

@Component
public class Receiver extends MessageCracker implements Application {

    private static Message receivedMessage;

    @Override
    public void fromAdmin(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, RejectLogon {
        System.out.println("Successfully called fromAdmin for sessionId : "
                + arg0);
    }

    @Override
    public void fromApp(Message arg0, SessionID arg1) throws FieldNotFound,
            IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        System.out.println("Successfully called fromApp for sessionId : " + arg0);
        crack(arg0, arg1);
    }

    @Override
    public void onCreate(SessionID arg0) {
        System.out.println("Successfully called onCreate for sessionId : " + arg0);
    }

    @Override
    public void onLogon(SessionID arg0) {
        System.out.println("Successfully logged on for sessionId : " + arg0);
    }

    @Override
    public void onLogout(SessionID arg0) {
        System.out.println("Successfully logged out for sessionId : " + arg0);
    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        System.out.println("Inside toAdmin");
    }

    @Override
    public void toApp(Message arg0, SessionID arg1) throws DoNotSend {
        System.out.println("Message : " + arg0 + " for sessionid : " + arg1);
    }

    public void onMessage(NewOrderSingle message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("Inside onMessage for New Order Single");
        receivedMessage = message;
    }

    public void onMessage(Logon message, SessionID sessionID)
            throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
        System.out.println("Inside Logon Message");
        super.onMessage(message, sessionID);
    }


    @PostConstruct
    public void start() throws Exception {
        System.out.println("Starting receiver.........");
        SessionSettings settings = new SessionSettings("fix-gateway.properties");
        MessageStoreFactory storeFactory = new FileStoreFactory(settings);
//        LogFactory logFactory = new FileLogFactory(settings);
        LogFactory logFactory = new SLF4JLogFactory(settings);
        MessageFactory messageFactory = new DefaultMessageFactory();
        Acceptor acceptor = new SocketAcceptor(new Receiver(), storeFactory, settings, logFactory, messageFactory);
        acceptor.start();

        System.out.println("Receiver started.................................");
    }

    public Message getReceivedMessage() {
        System.out.println("getting received message............returning: " + receivedMessage);
        return receivedMessage;
    }

    public boolean hasReceivedMessage() {
        return receivedMessage != null;
    }
}
