package structure;

/**
 * Main class demonstrating tree operations
 * Shows loading, saving with new name, and overwriting existing trees
 */
public class TreeManager {

    public static void main(String[] args) {
        // Create the binary tree using default constructor
        Tree binaryTree = new Tree("tree1");

        // Build the tree from the database
        binaryTree.buildTreeFromDatabase();   
        System.out.println("distance from A to B is " + binaryTree.calculatePathLength("A", "B"));
        System.out.println();

        // Save the tree with a new name
        String backupTreeName = "tree1_backup_" + System.currentTimeMillis();
        System.out.println("Creating backup with name: " + backupTreeName);
        binaryTree.saveTreeInDatabase(backupTreeName);
        
        // Demonstrate overwriting existing tree
        System.out.println("\nOverwriting original tree 'tree1':");
        binaryTree.saveTreeInDatabase("tree1");
        
        // Create a new tree and save it
        System.out.println("\nCreating a new tree 'tree2':");
        Tree newTree = new Tree("tree2");
        newTree.root = new TreeNode("X", 50);
        newTree.root.left = new TreeNode("Y", 30);
        newTree.root.right = new TreeNode("Z", 70);
        newTree.saveTreeInDatabase("tree2");
        
        // Verify the new tree was saved
        System.out.println("\nLoading 'tree2' to verify:");
        Tree verifyTree = new Tree("tree2");
        verifyTree.buildTreeFromDatabase();
        verifyTree.inorderTraversal();
    }
}