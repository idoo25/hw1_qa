package structure;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.*;

public class TreeDatabaseIntegrationTest {
    private Tree tree;
    private Connection connection;
    
    @Before
    public void setUp() throws Exception {
        // Setup real database connection
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        connection = DriverManager.getConnection(
            "jdbc:mysql://localhost/world?serverTimezone=IST", "root", "Aa123456");
    }
    
    @After
    public void tearDown() throws Exception {
        // Close connection
        if (connection != null) {
            connection.close();
        }
    }
    
    @Test
    public void testSaveTreeInDatabase_CreatesNewTreeInDB() throws Exception {
        // Arrange - Create tree with real implementations matching homework structure
        tree = new Tree("test_new");
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        tree.root.right = new TreeNode("C", 5);
        tree.root.left.left = new TreeNode("D", 10);  // Added node D
        
        // Act - Save to real database
        tree.saveTreeInDatabase("test_new");
        
        // Assert - Verify data in database (State Verification)
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) FROM world.tree WHERE treeName = 'test_new'");
        rs.next();
        assertEquals("Should have 4 nodes", 4, rs.getInt(1));  // Changed to 4
        
        // Verify node A (root)
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_new' AND nodename = 'A'");
        assertTrue("Node A should exist", rs.next());
        assertEquals("A's weight", 10, rs.getInt("weight"));
        assertEquals("A's left child", "B", rs.getString("leftp"));
        assertEquals("A's right child", "C", rs.getString("rightp"));
        
        // Verify node B
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_new' AND nodename = 'B'");
        assertTrue("Node B should exist", rs.next());
        assertEquals("B's weight", 20, rs.getInt("weight"));
        assertEquals("B's left child", "D", rs.getString("leftp"));
        assertNull("B's right child should be null", rs.getString("rightp"));
        
        // Verify node C
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_new' AND nodename = 'C'");
        assertTrue("Node C should exist", rs.next());
        assertEquals("C's weight", 5, rs.getInt("weight"));
        assertNull("C's left child should be null", rs.getString("leftp"));
        assertNull("C's right child should be null", rs.getString("rightp"));
        
        // Verify node D
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_new' AND nodename = 'D'");
        assertTrue("Node D should exist", rs.next());
        assertEquals("D's weight", 10, rs.getInt("weight"));
        assertNull("D's left child should be null", rs.getString("leftp"));
        assertNull("D's right child should be null", rs.getString("rightp"));
        
        rs.close();
        stmt.close();
    }
    
    @Test
    public void testSaveTreeInDatabase_UpdatesExistingTree() throws Exception {
        // Arrange - First create a tree in DB
        tree = new Tree("test_upd");
        tree.root = new TreeNode("X", 5);
        tree.saveTreeInDatabase("test_upd");
        
        // Create new tree structure
        tree = new Tree("test_upd");
        tree.root = new TreeNode("Y", 15);
        tree.root.left = new TreeNode("Z", 25);
        
        // Act - Update the tree
        tree.saveTreeInDatabase("test_upd");
        
        // Assert - Verify old data is gone and new data exists
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) FROM world.tree WHERE treeName = 'test_upd'");
        rs.next();
        assertEquals("Should have 2 nodes after update", 2, rs.getInt(1));
        
        // Verify new root node
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_upd' AND nodename = 'Y'");
        assertTrue("Node Y should exist", rs.next());
        assertEquals("Y's weight", 15, rs.getInt("weight"));
        assertEquals("Y's left child", "Z", rs.getString("leftp"));
        
        // Verify old node X is gone
        rs = stmt.executeQuery(
            "SELECT * FROM world.tree WHERE treeName = 'test_upd' AND nodename = 'X'");
        assertFalse("Old node X should not exist", rs.next());
        
        rs.close();
        stmt.close();
    }
    
    @Test
    public void testTreeExistsInDatabase_WithRealDB() throws Exception {
        // Arrange - Create a tree first
        tree = new Tree("test_ex");
        tree.root = new TreeNode("A", 10);
        tree.saveTreeInDatabase("test_ex");
        
        // Act & Assert
        assertTrue("Should find existing tree", 
                  tree.treeExistsInDatabase("test_ex"));
        assertFalse("Should not find non-existing tree", 
                   tree.treeExistsInDatabase("not_exist"));
    }
    
    @Test
    public void testSaveEmptyTree() throws Exception {
        // Arrange
        tree = new Tree("test_emp");
        // root is null
        
        // Act
        tree.saveTreeInDatabase("test_emp");
        
        // Assert - Should handle empty tree gracefully
        Statement stmt = connection.createStatement();
        ResultSet rs = stmt.executeQuery(
            "SELECT COUNT(*) FROM world.tree WHERE treeName = 'test_emp'");
        rs.next();
        assertEquals("Empty tree should have 0 nodes", 0, rs.getInt(1));
        
        rs.close();
        stmt.close();
    }
}