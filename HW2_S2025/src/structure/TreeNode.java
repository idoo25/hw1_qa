package structure;

/**
 * Node structure for binary tree
 * Package-private access for better encapsulation
 */
class TreeNode {
    String nodeName;
    int weight;
    TreeNode left;
    TreeNode right;
    
    TreeNode(String value, int weight) {
        this.nodeName = value;
        this.weight = weight;
        this.left = null;
        this.right = null;
    }
}