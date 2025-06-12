package structure;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Real implementation of ITreeLoader for database operations
 * Loads tree structures from MySQL database
 * Receives IMessageController through constructor injection
 */
public class TreeBuilderDB implements ITreeLoader {
    private String connectionUrl;
    private String username;
    private String password;
    private IMessageController messageController;
    
    /**
     * Full constructor with all parameters including message controller
     */
    public TreeBuilderDB(String connectionUrl, String username, String password, 
                        IMessageController messageController) {
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
        this.messageController = messageController;
    }
    
    /**
     * Constructor with message controller (uses default connection parameters)
     */
    public TreeBuilderDB(IMessageController messageController) {
        this("jdbc:mysql://localhost/world?serverTimezone=IST", 
             "root", "Aa123456", messageController);
    }
    
    /**
     * Loads a tree from the database
     * @param treeName the name of the tree to load
     * @return the root TreeNode or null if empty/error
     */
    @Override
    public TreeNode loadTree(String treeName) throws Exception {
        Map<String, TreeNode> nodeMap = new HashMap<>();
        TreeNode root = null;
        
        // Load JDBC driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
            System.out.println("Driver definition succeed");
        } catch (Exception ex) {
            System.out.println("Driver definition failed");
            messageController.sendWarning("Database driver initialization failed");
            throw new Exception("Driver definition failed", ex);
        }
        
        // Connect and query database
        try (Connection connection = DriverManager.getConnection(connectionUrl, username, password)) {
            
            // Using PreparedStatement for better security (prevents SQL injection)
            String query = "SELECT * FROM world.tree WHERE treeName = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, treeName);
                
                try (ResultSet resultSet = stmt.executeQuery()) {
                    // First pass: Create all nodes
                    while (resultSet.next()) {
                        String nodeName = resultSet.getString("nodename");
                        int weight = resultSet.getInt("weight");
                        nodeMap.put(nodeName, new TreeNode(nodeName, weight));
                    }
                    
                    // Reset cursor for second pass
                    resultSet.beforeFirst();
                    
                    // Second pass: Link nodes based on relationships
                    while (resultSet.next()) {
                        String nodeName = resultSet.getString("nodename");
                        String leftName = resultSet.getString("leftp");
                        String rightName = resultSet.getString("rightp");
                        
                        TreeNode currentNode = nodeMap.get(nodeName);
                        
                        // Link left child
                        if (leftName != null && nodeMap.containsKey(leftName)) {
                            currentNode.left = nodeMap.get(leftName);
                        }
                        
                        // Link right child
                        if (rightName != null && nodeMap.containsKey(rightName)) {
                            currentNode.right = nodeMap.get(rightName);
                        }
                        
                        // Set root (first node encountered)
                        if (root == null) {
                            root = currentNode;
                        }
                    }
                }
            }
        } catch (SQLException e) {
            messageController.sendWarning("A problem with Database was found");
            e.printStackTrace();
            throw e;
        }
        
        return root;
    }
    
    /**
     * Checks if a tree exists in the database
     * @param treeName the name to check
     * @return true if exists, false otherwise
     */
    @Override
    public boolean treeExists(String treeName) throws Exception {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            messageController.sendWarning("Database driver initialization failed");
            throw new Exception("Driver definition failed", ex);
        }
        
        try (Connection connection = DriverManager.getConnection(connectionUrl, username, password)) {
            String query = "SELECT COUNT(*) FROM world.tree WHERE treeName = ?";
            try (PreparedStatement stmt = connection.prepareStatement(query)) {
                stmt.setString(1, treeName);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        return rs.getInt(1) > 0;
                    }
                }
            }
        } catch (SQLException e) {
            messageController.sendWarning("Failed to check tree existence");
            throw e;
        }
        
        return false;
    }
}