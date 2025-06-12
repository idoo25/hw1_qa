package structure;

public class MockMessageController implements IMessageController {
    private boolean sendWarningCalled = false;
    private String lastWarningMessage = null;
    
    @Override
    public void sendWarning(String message) {
        this.sendWarningCalled = true;
        this.lastWarningMessage = message;
    }
    
    // Verification methods
    public boolean wasSendWarningCalled() {
        return sendWarningCalled;
    }
    
    public String getLastWarningMessage() {
        return lastWarningMessage;
    }
    
    // Reset method for test cleanup
    public void reset() {
        sendWarningCalled = false;
        lastWarningMessage = null;
    }
}