package structure;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses({ TreeTest.class, TreeSaveTest.class })
public class AllTests {
    // This class remains empty, it is used only as a holder for the above annotations
}