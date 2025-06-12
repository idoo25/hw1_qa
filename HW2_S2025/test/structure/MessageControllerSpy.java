package structure;

/**
 * Test Double (Spy) for MessageController.
 * Records the last message sent for verification in tests.
 */
public class MessageControllerSpy implements IMessageController {

    public String lastMessage = null;    // by default (given with the default constructor)
    
    @Override
    public void sendWarning(String message) {
        this.lastMessage = message;
    }
}