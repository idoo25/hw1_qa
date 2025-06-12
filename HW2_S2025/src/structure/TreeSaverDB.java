package structure;

import java.sql.*;

/**
 * Real implementation of ITreeSaver
 * Saves trees to database using delete and insert approach
 * Works directly with TreeNode without intermediate objects
 */
public class TreeSaverDB implements ITreeSaver {
    private String connectionUrl;
    private String username;
    private String password;
    
    /**
     * Full constructor with connection parameters
     */
    public TreeSaverDB(String connectionUrl, String username, String password) {
        this.connectionUrl = connectionUrl;
        this.username = username;
        this.password = password;
    }
    
    /**
     * Default constructor with standard connection parameters
     */
    public TreeSaverDB() {
        this("jdbc:mysql://localhost/world?serverTimezone=IST", "root", "Aa123456");
    }
    
    /**
     * Saves or updates a tree in the database
     * Uses transaction for atomic operation (delete + insert)
     * @param treeName the name to save the tree under
     * @param root the root node of the tree (can be null)
     */
    @Override
    public void saveTree(String treeName, TreeNode root) throws Exception {
        // Initialize driver
        try {
            Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            throw new Exception("Driver definition failed", ex);
        }
        
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(connectionUrl, username, password);
            
            // Use transaction for atomic operation
            connection.setAutoCommit(false);
            
            // Step 1: Delete existing tree data
            String deleteSQL = "DELETE FROM world.tree WHERE treeName = ?";
            try (PreparedStatement deleteStmt = connection.prepareStatement(deleteSQL)) {
                deleteStmt.setString(1, treeName);
                int deletedRows = deleteStmt.executeUpdate();
                System.out.println("Deleted " + deletedRows + " existing records for tree: " + treeName);
            }
            
            // Step 2: Insert new tree data if not empty
            if (root != null) {
                insertTreeNodes(connection, root, treeName);
            }
            
            // Commit transaction
            connection.commit();
            
        } catch (Exception e) {
            // Rollback on error
            if (connection != null) {
                try {
                    connection.rollback();
                } catch (SQLException rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }
            throw new Exception("Failed to save tree: " + e.getMessage(), e);
        } finally {
            // Restore auto-commit and close connection
            if (connection != null) {
                try {
                    connection.setAutoCommit(true);
                    connection.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }
    
    /**
     * Recursively inserts tree nodes into database
     * Similar to reference but with batch processing for efficiency
     */
    private void insertTreeNodes(Connection connection, TreeNode node, String treeName) throws SQLException {
        String insertSQL = "INSERT INTO world.tree (treeName, nodename, weight, leftp, rightp) VALUES (?, ?, ?, ?, ?)";
        
        try (PreparedStatement insertStmt = connection.prepareStatement(insertSQL)) {
            // Use batch processing for efficiency
            insertNodeBatch(insertStmt, node, treeName);
            
            // Execute all inserts at once
            int[] results = insertStmt.executeBatch();
            System.out.println("Inserted " + results.length + " nodes for tree: " + treeName);
        }
    }
    
    /**
     * Adds nodes to batch using pre-order traversal (root, left, right)
     * Different from reference's approach but achieves same result
     */
    private void insertNodeBatch(PreparedStatement stmt, TreeNode node, String treeName) throws SQLException {
        if (node == null) {
            return;
        }
        
        // Add current node to batch
        stmt.setString(1, treeName);
        stmt.setString(2, node.nodeName);
        stmt.setInt(3, node.weight);
        stmt.setString(4, node.left != null ? node.left.nodeName : null);
        stmt.setString(5, node.right != null ? node.right.nodeName : null);
        stmt.addBatch();
        
        // Recursively add left subtree
        insertNodeBatch(stmt, node.left, treeName);
        
        // Recursively add right subtree
        insertNodeBatch(stmt, node.right, treeName);
    }
}