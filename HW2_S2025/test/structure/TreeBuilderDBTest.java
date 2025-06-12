package structure;

import org.junit.Before;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;
import java.sql.*;

public class TreeBuilderDBTest {
    private TreeBuilderDB treeLoader;
    private MockMessageController mockMessageController;
    private Connection testConnection;
    private static final String TEST_DB_URL = "jdbc:mysql://localhost/world?serverTimezone=IST";
    private static final String TEST_DB_USER = "root";
    private static final String TEST_DB_PASSWORD = "Aa123456";
    
    @Before
    public void setUp() throws Exception {
        mockMessageController = new MockMessageController();
        treeLoader = new TreeBuilderDB(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD, mockMessageController);
        
        Class.forName("com.mysql.cj.jdbc.Driver").newInstance();
        testConnection = DriverManager.getConnection(TEST_DB_URL, TEST_DB_USER, TEST_DB_PASSWORD);
        
        setupTestData();
    }
    
    @After
    public void tearDown() throws Exception {
        cleanupTestData();
        if (testConnection != null && !testConnection.isClosed()) {
            testConnection.close();
        }
    }
    
    private void setupTestData() throws SQLException {
        String sql = "INSERT INTO world.tree (treeName, nodename, weight, leftp, rightp) VALUES " +
                    "('test_load', 'A', 10, 'B', 'C'), " +
                    "('test_load', 'B', 20, NULL, NULL), " +
                    "('test_load', 'C', 5, NULL, NULL)";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    private void cleanupTestData() throws SQLException {
        String sql = "DELETE FROM world.tree WHERE treeName LIKE 'test_%'";
        try (Statement stmt = testConnection.createStatement()) {
            stmt.executeUpdate(sql);
        }
    }
    
    @Test
    public void testLoadTree_WithExistingTree_LoadsCorrectStructure() throws Exception {
        // Act
        TreeNode root = treeLoader.loadTree("test_load");
        
        // Assert - STATE VERIFICATION
        assertNotNull("Root should not be null", root);
        assertEquals("A", root.nodeName);
        assertEquals(10, root.weight);
        
        assertNotNull("Left child should exist", root.left);
        assertEquals("B", root.left.nodeName);
        assertEquals(20, root.left.weight);
        
        assertNotNull("Right child should exist", root.right);
        assertEquals("C", root.right.nodeName);
        assertEquals(5, root.right.weight);
    }
    
    @Test
    public void testTreeExists_WhenTreeExists_ReturnsTrue() throws Exception {
        // Act
        boolean exists = treeLoader.treeExists("test_load");
        
        // Assert
        assertTrue("Should return true for existing tree", exists);
    }
}