package OOP.Tests;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExpectedException;
import OOP.Solution.*;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;


public class ExampleTest {

    @Test
    public void testForExample() {
        OOPTestSummary result = OOPUnitCore.runClass(ExampleClass.class);
        assertNotNull(result);
        result.testmap.forEach((key, value) -> System.out.println(key + ":" + value.getResultType()));
        assertEquals(2, result.getNumSuccesses());
        assertEquals(1, result.getNumFailures());
        assertEquals(0, result.getNumErrors());
        assertEquals(0, result.getNumExceptionMismatches());
    }

    static
    @OOPTestClass(OOPTestClass.OOPTestClassType.ORDERED)
    public class ExampleClass {

        @OOPExceptionRule
        private OOPExpectedException expected = OOPExpectedExceptionImpl.none();

        private int field = 0;

        @OOPSetup
        public void beforeFirstTest() {
            this.field = 123;
        }


        // Should be successful
        @OOPTest(order = 1)
        public void test1() throws OOPAssertionFailure {
            //this must run before the other test. must not throw an exception to succeed
            OOPUnitCore.assertEquals(123, this.field);
        }

        // Should fail
        @OOPTest(order = 2)
        public void test2() throws OOPAssertionFailure {
            OOPUnitCore.assertEquals(12, this.field);
        }

        // Should be successful
        @OOPTest(order = 3)
        public void test3() throws Exception {
            OOPUnitCore.assertEquals(123, this.field);
        }

    }

}
