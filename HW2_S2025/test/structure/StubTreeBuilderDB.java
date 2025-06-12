package structure;

/**
 * Alternative stub that mimics TreeBuilderDB constructor
 * Useful when testing with constructor injection pattern
 */
public class StubTreeBuilderDB extends StubTreeLoader {
    private IMessageController messageController;
    
    /**
     * Constructor matching TreeBuilderDB signature
     * @param messageController the message controller (ignored in stub)
     */
    public StubTreeBuilderDB(IMessageController messageController) {
        super();
        this.messageController = messageController;
    }
    
    /**
     * Get the injected message controller (for verification if needed)
     */
    public IMessageController getMessageController() {
        return messageController;
    }
}