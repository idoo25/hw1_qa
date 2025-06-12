package structure;

/**
 * Stub implementation of ITreeLoader for testing
 * Provides controlled tree loading behavior without database
 */
public class StubTreeLoader implements ITreeLoader {
    private TreeNode rootToReturn;
    private Exception exceptionToThrow;
    private boolean treeExistsReturn = false;
    private int loadTreeCallCount = 0;
    private int treeExistsCallCount = 0;
    
    public StubTreeLoader() {
        this.rootToReturn = null;
    }
    
    // Methods to set up the stub behavior
    public void setRootToReturn(TreeNode root) {
        this.rootToReturn = root;
    }
    
    public void setExceptionToThrow(Exception exception) {
        this.exceptionToThrow = exception;
    }
    
    public void setTreeExists(boolean exists) {
        this.treeExistsReturn = exists;
    }
    
    // Verification methods
    public int getLoadTreeCallCount() {
        return loadTreeCallCount;
    }
    
    public int getTreeExistsCallCount() {
        return treeExistsCallCount;
    }
    
    public void reset() {
        loadTreeCallCount = 0;
        treeExistsCallCount = 0;
        rootToReturn = null;
        exceptionToThrow = null;
        treeExistsReturn = false;
    }
    
    @Override
    public TreeNode loadTree(String treeName) throws Exception {
        loadTreeCallCount++;
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return rootToReturn;
    }
    
    @Override
    public boolean treeExists(String treeName) throws Exception {
        treeExistsCallCount++;
        if (exceptionToThrow != null) {
            throw exceptionToThrow;
        }
        return treeExistsReturn;
    }
}