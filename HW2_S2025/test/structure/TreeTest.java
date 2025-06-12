package structure;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class TreeTest {
    private StubTreeLoader stubLoader;
    private StubTreeSaver stubSaver;
    private MockMessageController mockMessageController;
    private Tree tree;
    
    @Before
    public void setUp() {
        stubLoader = new StubTreeLoader();
        stubSaver = new StubTreeSaver();
        mockMessageController = new MockMessageController();
        tree = new Tree("testTree", stubLoader, stubSaver, mockMessageController);
    }
    
    // Tests for buildTreeFromDatabase()
    
    @Test
    public void testBuildTreeFromDatabase_WithValidData_BuildsCorrectTree() {
        // Arrange - Build expected tree structure
        TreeNode expectedRoot = new TreeNode("A", 10);
        expectedRoot.left = new TreeNode("B", 20);
        expectedRoot.right = new TreeNode("C", 5);
        expectedRoot.left.left = new TreeNode("D", 10);
        
        stubLoader.setRootToReturn(expectedRoot);
        
        // Act
        tree.buildTreeFromDatabase();
        
        // Assert
        assertNotNull("Root should not be null", tree.root);
        assertEquals("Root node name should be A", "A", tree.root.nodeName);
        assertEquals("Root weight should be 10", 10, tree.root.weight);
        assertNotNull("Left child should exist", tree.root.left);
        assertEquals("Left child should be B", "B", tree.root.left.nodeName);
        assertNotNull("Right child should exist", tree.root.right);
        assertEquals("Right child should be C", "C", tree.root.right.nodeName);
        assertNotNull("B's left child should exist", tree.root.left.left);
        assertEquals("B's left child should be D", "D", tree.root.left.left.nodeName);
        assertNull("B's right child should be null", tree.root.left.right);
        assertFalse("Warning should not be sent", mockMessageController.wasSendWarningCalled());
    }
    
    @Test
    public void testBuildTreeFromDatabase_WithEmptyData_CreatesEmptyTree() {
        // Arrange
        stubLoader.setRootToReturn(null);
        
        // Act
        tree.buildTreeFromDatabase();
        
        // Assert
        assertNull("Root should be null for empty data", tree.root);
        assertFalse("Warning should not be sent", mockMessageController.wasSendWarningCalled());
    }
    
    @Test
    public void testBuildTreeFromDatabase_WithSingleNode_CreatesSingleNodeTree() {
        // Arrange
        TreeNode singleNode = new TreeNode("A", 10);
        stubLoader.setRootToReturn(singleNode);
        
        // Act
        tree.buildTreeFromDatabase();
        
        // Assert
        assertNotNull("Root should not be null", tree.root);
        assertEquals("Root node name should be A", "A", tree.root.nodeName);
        assertNull("Left child should be null", tree.root.left);
        assertNull("Right child should be null", tree.root.right);
        assertFalse("Warning should not be sent", mockMessageController.wasSendWarningCalled());
    }
    
    @Test
    public void testBuildTreeFromDatabase_WithDatabaseException_SendsWarning() {
        // Arrange
        stubLoader.setExceptionToThrow(new Exception("Database connection failed"));
        
        // Act
        tree.buildTreeFromDatabase();
        
        // Assert
        assertTrue("sendWarning should be called", mockMessageController.wasSendWarningCalled());
        assertEquals("Warning message should match", 
                    "A problem with Database was found", 
                    mockMessageController.getLastWarningMessage());
        assertNull("Root should remain null after exception", tree.root);
    }
    
    // Tests for calculatePathLength()
    
    @Test
    public void testCalculatePathLength_BetweenAdjacentNodes_ReturnsOne() {
        // Arrange - Build tree: A -> B
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        
        // Act
        int pathLength = tree.calculatePathLength("A", "B");
        
        // Assert
        assertEquals("Path length between A and B should be 1", 1, pathLength);
    }
    
    @Test
    public void testCalculatePathLength_BetweenSiblings_ReturnsTwo() {
        // Arrange - Build tree: A with children B and C
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        tree.root.right = new TreeNode("C", 5);
        
        // Act
        int pathLength = tree.calculatePathLength("B", "C");
        
        // Assert
        assertEquals("Path length between siblings B and C should be 2", 2, pathLength);
    }
    
    @Test
    public void testCalculatePathLength_SameNode_ReturnsZero() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        
        // Act
        int pathLength = tree.calculatePathLength("A", "A");
        
        // Assert
        assertEquals("Path length from node to itself should be 0", 0, pathLength);
    }
    
    @Test
    public void testCalculatePathLength_ComplexTree_ReturnsCorrectDistance() {
        // Arrange - Build the tree from the homework example
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        tree.root.right = new TreeNode("C", 5);
        tree.root.left.left = new TreeNode("D", 10);
        
        // Act
        int pathLengthAD = tree.calculatePathLength("A", "D");
        int pathLengthDC = tree.calculatePathLength("D", "C");
        
        // Assert
        assertEquals("Path length from A to D should be 2", 2, pathLengthAD);
        assertEquals("Path length from D to C should be 3", 3, pathLengthDC);
    }
    
    @Test
    public void testCalculatePathLength_WithEmptyTree_ReturnsMinusOne() {
        // Arrange - tree.root is already null
        
        // Act
        int pathLength = tree.calculatePathLength("A", "B");
        
        // Assert
        assertEquals("Path length in empty tree should be -1", -1, pathLength);
    }
    
    @Test
    public void testCalculatePathLength_NodeNotFound_ReturnsMinusOne() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        
        // Act
        int pathLength = tree.calculatePathLength("A", "Z");
        
        // Assert
        assertEquals("Path length with non-existent node should be -1", -1, pathLength);
    }
    
    @Test
    public void testCalculatePathLength_BothNodesNotFound_ReturnsMinusOne() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        
        // Act
        int pathLength = tree.calculatePathLength("X", "Y");
        
        // Assert
        assertEquals("Path length with both nodes non-existent should be -1", -1, pathLength);
    }
}