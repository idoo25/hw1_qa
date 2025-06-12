package structure;

/**
 * Interface for saving a binary tree in DB
 * This interface isolates the Tree class from database save operations
 */
public interface ITreeSaver {
    /**
     * Saves a tree to the database
     * If the tree already exists, it will be replaced (delete and insert)
     * @param treeName the name to save the tree under
     * @param root the root node of the tree to save (can be null for empty tree)
     * @throws Exception if database operation fails
     */
    void saveTree(String treeName, TreeNode root) throws Exception;
}