package structure;

/**
 * Interface for test double of sendWarning method
 * The actual MessageController.sendWarning() is not implemented in the project
 * This interface allows us to create mocks/stubs for testing
 */
public interface IMessageController {
    /**
     * Sends a warning message
     * Note: The real implementation is empty (not implemented)
     * @param message the warning message to send
     */
    void sendWarning(String message);
}