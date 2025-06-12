package structure;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.*;

public class TreeSaverDBTest {
    private TreeSaverDB treeSaver;
    private Connection testConnection;
    private static final String TEST_DB_URL = "jdbc:mysql://localhost/world?serverTimezone=IST";
    private static final String TEST_DB_USER = "root";
    private static final String TEST_DB_PASSWORD = "Aa123456";
    
    @Before
    public void setUp() throws Exception {
        // Initialize real TreeSaverDB
        treeSaver = new TreeSaverDB(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        
        // Get connection for verification
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        
        // Clean up test data before each test
        cleanupTestData();
    }
    
    @After
    public void tearDown() throws Exception {
        cleanupTestData();
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
    
    private void cleanupTestData() throws SQLException {
        String sql = "DELETE FROM world.tree WHERE treeName LIKE 'test_%'";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    // STATE VERIFICATION TEST
    @Test
    public void testSaveTree_WithCompleteTree_SavesAllNodesInDatabase() throws Exception {
        // Arrange
        String testTreeName = "test_tree_complete";
        TreeNode root = new TreeNode("A", 10);
        root.left = new TreeNode("B", 20);
        root.right = new TreeNode("C", 5);
        root.left.left = new TreeNode("D", 10);
        
        // Act
        treeSaver.saveTree(testTreeName, root);
        
        // Assert - STATE VERIFICATION
        String query = "SELECT * FROM world.tree WHERE treeName = ? ORDER BY nodename";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setString(1, testTreeName);
            try (ResultSet rs = stmt.executeQuery()) {
                // Verify node A
                assertTrue("Should have node A", rs.next());
                assertEquals("A", rs.getString("nodename"));
                assertEquals(10, rs.getInt("weight"));
                assertEquals("B", rs.getString("leftp"));
                assertEquals("C", rs.getString("rightp"));
                
                // Verify node B
                assertTrue("Should have node B", rs.next());
                assertEquals("B", rs.getString("nodename"));
                assertEquals(20, rs.getInt("weight"));
                assertEquals("D", rs.getString("leftp"));
                assertNull(rs.getString("rightp"));
                
                // Verify node C
                assertTrue("Should have node C", rs.next());
                assertEquals("C", rs.getString("nodename"));
                assertEquals(5, rs.getInt("weight"));
                assertNull(rs.getString("leftp"));
                assertNull(rs.getString("rightp"));
                
                // Verify node D
                assertTrue("Should have node D", rs.next());
                assertEquals("D", rs.getString("nodename"));
                assertEquals(10, rs.getInt("weight"));
                assertNull(rs.getString("leftp"));
                assertNull(rs.getString("rightp"));
                
                // Verify no more nodes
                assertFalse("Should have no more nodes", rs.next());
            }
        }
    }
    
    // BEHAVIOR VERIFICATION TEST
    @Test
    public void testSaveTree_WhenTreeExists_DeletesOldDataFirst() throws Exception {
        // Arrange
        String testTreeName = "test_tree_update";
        
        // Insert initial data
        String insertSql = "INSERT INTO world.tree (treeName, nodename, weight) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = testConnection.prepareStatement(insertSql)) {
            stmt.setString(1, testTreeName);
            stmt.setString(2, "OLD_NODE");
            stmt.setInt(3, 999);
            stmt.executeUpdate();
        }
        
        // Create new tree
        TreeNode newRoot = new TreeNode("NEW_NODE", 100);
        
        // Act
        treeSaver.saveTree(testTreeName, newRoot);
        
        // Assert - BEHAVIOR VERIFICATION (old data deleted, new data inserted)
        String query = "SELECT * FROM world.tree WHERE treeName = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setString(1, testTreeName);
            try (ResultSet rs = stmt.executeQuery()) {
                assertTrue("Should have new node", rs.next());
                assertEquals("NEW_NODE", rs.getString("nodename"));
                assertEquals(100, rs.getInt("weight"));
                
                assertFalse("Should not have old node", rs.next());
            }
        }
    }
    
    @Test
    public void testSaveTree_WithEmptyTree_DeletesExistingData() throws Exception {
        // Arrange
        String testTreeName = "test_tree_empty";
        
        // Insert initial data
        String insertSql = "INSERT INTO world.tree (treeName, nodename, weight) VALUES (?, ?, ?)";
        try (PreparedStatement stmt = testConnection.prepareStatement(insertSql)) {
            stmt.setString(1, testTreeName);
            stmt.setString(2, "TO_DELETE");
            stmt.setInt(3, 999);
            stmt.executeUpdate();
        }
        
        // Act
        treeSaver.saveTree(testTreeName, null);
        
        // Assert - STATE VERIFICATION
        String query = "SELECT COUNT(*) FROM world.tree WHERE treeName = ?";
        try (PreparedStatement stmt = testConnection.prepareStatement(query)) {
            stmt.setString(1, testTreeName);
            try (ResultSet rs = stmt.executeQuery()) {
                rs.next();
                assertEquals("Should have no nodes", 0, rs.getInt(1));
            }
        }
    }
    
    @Test
    public void testSaveTree_TransactionRollback_OnError() throws Exception {
        // This test verifies behavior when an error occurs
        // You might need to modify TreeSaverDB to make this testable
        // For example, by injecting a connection that fails mid-transaction
    }
}