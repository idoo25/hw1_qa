package structure;

/**
 * Interface for isolating external dependencies when creating a tree from database
 * This interface allows the Tree class to be tested independently from the database
 */
public interface ITreeLoader {
    /**
     * Loads a tree from the database
     * @param treeName the name of the tree to load
     * @return the root TreeNode of the loaded tree, or null if tree is empty
     * @throws Exception if database operation fails
     */
    TreeNode loadTree(String treeName) throws Exception;
    
    /**
     * Checks if a tree exists in the database
     * @param treeName the name of the tree to check
     * @return true if tree exists, false otherwise
     * @throws Exception if database operation fails
     */
    boolean treeExists(String treeName) throws Exception;
}