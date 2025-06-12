package structure;

public class StubTreeSaver implements ITreeSaver {
    private TreeNode savedRoot;
    private String lastSavedTreeName;
    private Exception exceptionToThrow;
    
    // Methods to verify what was called
    public TreeNode getSavedRoot() {
        return savedRoot;
    }
    
    public String getLastSavedTreeName() {
        return lastSavedTreeName;
    }
    
    // Method to set up exception behavior
    public void setExceptionToThrow(Exception exception) {
        this.exceptionToThrow = exception;
    }
    
    @Override
    public void saveTree(String treeName, TreeNode root) throws Exception {
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        this.lastSavedTreeName = treeName;
        this.savedRoot = root;
    }
}