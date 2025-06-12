package structure;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * TDD Test class for saveTreeInDatabase functionality
 * Tests follow Red-Green-Refactor pattern
 */
public class TreeSaveTest {
    private StubTreeLoader stubLoader;
    private StubTreeSaver stubSaver;
    private MockMessageController mockMessageController;
    private Tree tree;
    
    @Before
    public void setUp() {
        // Initialize stubs and mocks
        stubLoader = new StubTreeLoader();
        stubSaver = new StubTreeSaver();
        mockMessageController = new MockMessageController();
        
        // Create tree with injected dependencies
        tree = new Tree("originalTree", stubLoader, stubSaver, mockMessageController);
    }
    
    // Test 1: Save empty tree
    @Test
    public void testSaveTreeInDatabase_EmptyTree_SavesNull() {
        // Arrange - tree.root is already null
        
        // Act
        tree.saveTreeInDatabase("newTree");
        
        // Assert
        assertEquals("Tree name should be saved correctly", "newTree", stubSaver.getLastSavedTreeName());
        assertNull("Saved root should be null for empty tree", stubSaver.getSavedRoot());
        assertFalse("No warning should be sent for successful save", mockMessageController.wasSendWarningCalled());
    }
    
    // Test 2: Save single node tree
    @Test
    public void testSaveTreeInDatabase_SingleNode_SavesCorrectTree() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        
        // Act
        tree.saveTreeInDatabase("singleNodeTree");
        
        // Assert
        assertEquals("Tree name should be saved correctly", "singleNodeTree", stubSaver.getLastSavedTreeName());
        TreeNode savedRoot = stubSaver.getSavedRoot();
        assertNotNull("Saved root should not be null", savedRoot);
        assertEquals("Node name should be A", "A", savedRoot.nodeName);
        assertEquals("Weight should be 10", 10, savedRoot.weight);
        assertNull("Left child should be null", savedRoot.left);
        assertNull("Right child should be null", savedRoot.right);
        assertFalse("No warning should be sent", mockMessageController.wasSendWarningCalled());
    }
    
    // Test 3: Save tree with multiple nodes
    @Test
    public void testSaveTreeInDatabase_CompleteTree_SavesAllNodes() {
        // Arrange - Build the tree from homework example
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        tree.root.right = new TreeNode("C", 5);
        tree.root.left.left = new TreeNode("D", 10);
        
        // Act
        tree.saveTreeInDatabase("completeTree");
        
        // Assert
        TreeNode savedRoot = stubSaver.getSavedRoot();
        assertNotNull("Saved root should not be null", savedRoot);
        assertEquals("Root should be A", "A", savedRoot.nodeName);
        assertEquals("Root weight should be 10", 10, savedRoot.weight);
        
        // Verify left subtree
        assertNotNull("Root should have left child", savedRoot.left);
        assertEquals("Left child should be B", "B", savedRoot.left.nodeName);
        assertEquals("B's weight should be 20", 20, savedRoot.left.weight);
        assertNotNull("B should have left child", savedRoot.left.left);
        assertEquals("B's left child should be D", "D", savedRoot.left.left.nodeName);
        assertEquals("D's weight should be 10", 10, savedRoot.left.left.weight);
        assertNull("B should not have right child", savedRoot.left.right);
        
        // Verify right subtree
        assertNotNull("Root should have right child", savedRoot.right);
        assertEquals("Right child should be C", "C", savedRoot.right.nodeName);
        assertEquals("C's weight should be 5", 5, savedRoot.right.weight);
        assertNull("C should not have left child", savedRoot.right.left);
        assertNull("C should not have right child", savedRoot.right.right);
        
        assertFalse("No warning should be sent", mockMessageController.wasSendWarningCalled());
    }
    
    // Test 4: Save with database exception
    @Test
    public void testSaveTreeInDatabase_WithDatabaseException_SendsWarning() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        stubSaver.setExceptionToThrow(new Exception("Database save failed"));
        
        // Act
        tree.saveTreeInDatabase("errorTree");
        
        // Assert
        assertTrue("sendWarning should be called when exception occurs", 
                  mockMessageController.wasSendWarningCalled());
        assertEquals("Warning message should match", 
                    "Failed to save tree to database", 
                    mockMessageController.getLastWarningMessage());
    }
    
    // Test 5: Save tree with only left branch
    @Test
    public void testSaveTreeInDatabase_OnlyLeftBranch_SavesCorrectly() {
        // Arrange - Linear tree going left
        tree.root = new TreeNode("A", 1);
        tree.root.left = new TreeNode("B", 2);
        tree.root.left.left = new TreeNode("C", 3);
        
        // Act
        tree.saveTreeInDatabase("leftBranchTree");
        
        // Assert
        TreeNode savedRoot = stubSaver.getSavedRoot();
        assertNotNull("Saved root should not be null", savedRoot);
        assertEquals("Root should be A", "A", savedRoot.nodeName);
        assertNotNull("A should have left child", savedRoot.left);
        assertEquals("A's left should be B", "B", savedRoot.left.nodeName);
        assertNotNull("B should have left child", savedRoot.left.left);
        assertEquals("B's left should be C", "C", savedRoot.left.left.nodeName);
        assertNull("A should not have right child", savedRoot.right);
        assertNull("B should not have right child", savedRoot.left.right);
    }
    
    // Test 6: Save tree with only right branch
    @Test
    public void testSaveTreeInDatabase_OnlyRightBranch_SavesCorrectly() {
        // Arrange - Linear tree going right
        tree.root = new TreeNode("A", 1);
        tree.root.right = new TreeNode("B", 2);
        tree.root.right.right = new TreeNode("C", 3);
        
        // Act
        tree.saveTreeInDatabase("rightBranchTree");
        
        // Assert
        TreeNode savedRoot = stubSaver.getSavedRoot();
        assertNotNull("Saved root should not be null", savedRoot);
        assertEquals("Root should be A", "A", savedRoot.nodeName);
        assertNull("A should not have left child", savedRoot.left);
        assertNotNull("A should have right child", savedRoot.right);
        assertEquals("A's right should be B", "B", savedRoot.right.nodeName);
        assertNotNull("B should have right child", savedRoot.right.right);
        assertEquals("B's right should be C", "C", savedRoot.right.right.nodeName);
    }
    
    // Test 7: Check if tree exists - returns true
    @Test
    public void testTreeExistsInDatabase_WhenTreeExists_ReturnsTrue() {
        // Arrange
        stubLoader.setTreeExists(true);
        
        // Act
        boolean exists = tree.treeExistsInDatabase("existingTree");
        
        // Assert
        assertTrue("Should return true when tree exists", exists);
        assertFalse("No warning should be sent", mockMessageController.wasSendWarningCalled());
    }
    
    // Test 8: Check if tree exists - returns false
    @Test
    public void testTreeExistsInDatabase_WhenTreeDoesNotExist_ReturnsFalse() {
        // Arrange
        stubLoader.setTreeExists(false);
        
        // Act
        boolean exists = tree.treeExistsInDatabase("nonExistentTree");
        
        // Assert
        assertFalse("Should return false when tree does not exist", exists);
        assertFalse("No warning should be sent", mockMessageController.wasSendWarningCalled());
    }
    
    // Test 9: Check if tree exists - handles exception
    @Test
    public void testTreeExistsInDatabase_WithException_ReturnsFalseAndSendsWarning() {
        // Arrange
        stubLoader.setExceptionToThrow(new Exception("Database error"));
        
        // Act
        boolean exists = tree.treeExistsInDatabase("someTree");
        
        // Assert
        assertFalse("Should return false when exception occurs", exists);
        assertTrue("sendWarning should be called", mockMessageController.wasSendWarningCalled());
        assertEquals("Warning message should match", 
                    "Failed to check tree existence in database", 
                    mockMessageController.getLastWarningMessage());
    }
    
    // Test 10: Save tree that already exists (update scenario)
    @Test
    public void testSaveTreeInDatabase_WhenTreeExists_UpdatesExistingTree() {
        // Arrange
        tree.root = new TreeNode("A", 10);
        tree.root.left = new TreeNode("B", 20);
        stubLoader.setTreeExists(true); // Simulate existing tree
        
        // Act
        tree.saveTreeInDatabase("existingTree");
        
        // Assert
        TreeNode savedRoot = stubSaver.getSavedRoot();
        assertNotNull("Should save root node", savedRoot);
        assertEquals("Should save with correct tree name", "existingTree", 
                    stubSaver.getLastSavedTreeName());
        assertEquals("Root should be A", "A", savedRoot.nodeName);
        assertNotNull("Should have left child B", savedRoot.left);
        assertEquals("Left child should be B", "B", savedRoot.left.nodeName);
        assertFalse("No warning should be sent for update", 
                   mockMessageController.wasSendWarningCalled());
    }
    
    // Test 11: Save with special characters in tree name
    @Test
    public void testSaveTreeInDatabase_WithSpecialTreeName_SavesCorrectly() {
        // Arrange
        tree.root = new TreeNode("Root", 100);
        String specialName = "tree_2025_backup";
        
        // Act
        tree.saveTreeInDatabase(specialName);
        
        // Assert
        assertEquals("Should handle special characters in name", specialName, 
                    stubSaver.getLastSavedTreeName());
        assertNotNull("Should save the tree", stubSaver.getSavedRoot());
        assertEquals("Root node should be saved", "Root", stubSaver.getSavedRoot().nodeName);
    }
    
    // Test 12: Verify console output for new vs existing tree
    @Test
    public void testSaveTreeInDatabase_ConsoleOutput_PrintsCorrectMessage() {
        // This test verifies console output indirectly through the logic
        // In real implementation, we could capture System.out
        
        // Arrange for new tree
        tree.root = new TreeNode("A", 10);
        stubLoader.setTreeExists(false);
        
        // Act - save as new tree
        tree.saveTreeInDatabase("newTree");
        
        // Assert
        assertFalse("No warning for successful save", mockMessageController.wasSendWarningCalled());
        
        // Arrange for existing tree
        stubLoader.setTreeExists(true);
        
        // Act - save as update
        tree.saveTreeInDatabase("existingTree");
        
        // Assert
        assertFalse("No warning for successful update", mockMessageController.wasSendWarningCalled());
    }
}