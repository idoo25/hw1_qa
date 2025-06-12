package structure;

/**
 * Binary tree data structure with database persistence
 * Uses dependency injection for external dependencies
 */
public class Tree {
    String treeName;
    TreeNode root;
    private IMessageController messageController;
    private ITreeLoader treeLoader;
    private ITreeSaver treeSaver;

    /**
     * Default constructor - uses real implementations
     * Creates TreeBuilderDB with message controller for proper dependency injection
     */
    public Tree(String name) {
        this.treeName = name;
        this.root = null;
        this.messageController = new MessageController();
        // Pass message controller to TreeBuilderDB
        this.treeLoader = new TreeBuilderDB(messageController);
        this.treeSaver = new TreeSaverDB();
    }
    
    /**
     * Constructor for dependency injection (used in testing)
     * Allows injection of stub/mock implementations
     */
    public Tree(String name, ITreeLoader loader, ITreeSaver saver, 
                IMessageController messageController) {
        this.treeName = name;
        this.root = null;
        this.treeLoader = loader;
        this.treeSaver = saver;
        this.messageController = messageController;
    }

    /**
     * Finds a node by name using recursive search
     * @param current starting node for search
     * @param nodeName name to search for
     * @return found node or null
     */
    public TreeNode findNode(TreeNode current, String nodeName) {
        if (current == null) {
            return null;
        }
        if (current.nodeName.equals(nodeName)) {
            return current;
        }
        TreeNode leftSearch = findNode(current.left, nodeName);
        if (leftSearch != null) {
            return leftSearch;
        }
        return findNode(current.right, nodeName);
    }

    /**
     * Finds the Lowest Common Ancestor (LCA) of two nodes
     * @param current current node in recursion
     * @param nodeX first node name
     * @param nodeY second node name
     * @return LCA node or null if not found
     */
    public TreeNode findLCA(TreeNode current, String nodeX, String nodeY) {
        if (current == null) {
            return null;
        }
        if (current.nodeName.equals(nodeX) || current.nodeName.equals(nodeY)) {
            return current;
        }

        TreeNode leftLCA = findLCA(current.left, nodeX, nodeY);
        TreeNode rightLCA = findLCA(current.right, nodeX, nodeY);

        if (leftLCA != null && rightLCA != null) {
            return current;
        }
        return (leftLCA != null) ? leftLCA : rightLCA;
    }

    /**
     * Calculates distance from a node to target
     * @param current starting node
     * @param targetNode target node name
     * @param distance current distance
     * @return distance or -1 if not found
     */
    public int calculateDistance(TreeNode current, String targetNode, int distance) {
        if (current == null) {
            return -1;
        }
        if (current.nodeName.equals(targetNode)) {
            return distance;
        }

        int leftDistance = calculateDistance(current.left, targetNode, distance + 1);
        if (leftDistance != -1) {
            return leftDistance;
        }

        return calculateDistance(current.right, targetNode, distance  + 1);
    }

    /**
     * Calculates the path length between two nodes (number of edges)
     * @param nodeX first node name
     * @param nodeY second node name
     * @return number of edges or -1 if error
     */
    public int calculatePathLength(String nodeX, String nodeY) {
        if (root == null) {
            System.out.println("The tree is empty.");
            return -1;
        }

        TreeNode lca = findLCA(root, nodeX, nodeY);
        if (lca == null) {
            System.out.println("One or both of the nodes were not found.");
            return -1;
        }

        int distanceToX = calculateDistance(lca, nodeX, 0);
        int distanceToY = calculateDistance(lca, nodeY, 0);

        if (distanceToX == -1 || distanceToY == -1) {
            System.out.println("One or both of the nodes were not found.");
            return -1;
        }

        return distanceToX + distanceToY;
    }

    /**
     * Builds the tree from database using injected loader
     * Delegates to ITreeLoader implementation
     */
    public void buildTreeFromDatabase() {
        try {
            this.root = treeLoader.loadTree(this.treeName);
            if (this.root == null) {
                System.out.println("No tree found with name: " + this.treeName);
            }
        } catch (Exception e) {
            messageController.sendWarning("Failed to build tree from database");
            e.printStackTrace();
        }
    }

    /**
     * Saves the tree to database with specified name
     * Can save as new tree or overwrite existing
     * @param treeName name to save the tree under
     */
    public void saveTreeInDatabase(String treeName) {
        try {
            // Check if tree exists for logging purposes
            boolean exists = treeLoader.treeExists(treeName);
            
            // Save the tree (will overwrite if exists)
            treeSaver.saveTree(treeName, this.root);
            
            // Log appropriate message
            if (exists) {
                System.out.println("Updated existing tree: " + treeName);
            } else {
                System.out.println("Created new tree: " + treeName);
            }
        } catch (Exception e) {
            messageController.sendWarning("Failed to save tree to database");
            e.printStackTrace();
        }
    }
    
    /**
     * Checks if a tree exists in the database
     * @param treeName name to check
     * @return true if exists, false otherwise
     */
    public boolean treeExistsInDatabase(String treeName) {
        try {
            return treeLoader.treeExists(treeName);
        } catch (Exception e) {
            messageController.sendWarning("Failed to check tree existence in database");
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Performs inorder traversal of the tree
     * @param node starting node
     */
    public void inorderTraversal(TreeNode node) {
        if (node != null) {
            inorderTraversal(node.left);
            System.out.println("Node: " + node.nodeName + ", Weight: " + node.weight);
            inorderTraversal(node.right);
        }
    }
    
    /**
     * Performs inorder traversal starting from root
     */
    public void inorderTraversal() {
        if (this.root == null) {
            System.out.println("The tree is empty.");
        } else {
            inorderTraversal(this.root);
        }
    }
    
    // Getter methods
    public TreeNode getRoot() {
        return this.root;
    }
    
    public String getTreeName() {
        return this.treeName;
    }
}