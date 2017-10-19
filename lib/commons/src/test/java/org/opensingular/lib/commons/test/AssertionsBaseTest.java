package org.opensingular.lib.commons.test;

import org.junit.Assert;
import org.junit.Test;

import java.util.Optional;

public class AssertionsBaseTest {
    @Test
    public void testConstructor(){
        Assert.assertNotNull(new AssertionsTest("raiz"));
        Assert.assertNotNull(new AssertionsTest(Optional.of("raizOptional")));
    }

    @Test
    public void testGetClass(){
        AssertionsTest assertion = new AssertionsTest("raiz");

        Assert.assertNotNull(assertion.getTarget());
        Assert.assertTrue(assertion.getTarget() instanceof String);

        Assert.assertNotNull(assertion.getTarget(String.class));
        Assert.assertNotNull(assertion.getTarget());
    }

    @Test(expected = AssertionError.class)
    public void testGetTargetException(){
        AssertionsTest assertion = new AssertionsTest("raiz");
        assertion.getTarget(Integer.class);
    }

    @Test(expected = AssertionError.class)
    public void testGetTargetOrException(){
        AssertionsTest assertion = new AssertionsTest((String) null);
        assertion.getTarget();
    }

    @Test
    public void testIsNull(){
        AssertionsTest assertion = new AssertionsTest((String) null);
        assertion.isNull();
    }

    @Test(expected = AssertionError.class)
    public void testIsNullException(){
        AssertionsTest assertion = new AssertionsTest("raiz");
        assertion.isNull();
    }

    @Test
    public void testIs(){
        AssertionsTest assertion = new AssertionsTest("raiz");
        assertion.is(String.class);
    }

    @Test(expected = AssertionError.class)
    public void testIsException(){
        AssertionsTest assertion = new AssertionsTest("raiz");
        assertion.is(Integer.class);
    }

    @Test
    public void testIsSameAs(){
        String root = new String("valor");
        AssertionsTest assertion = new AssertionsTest(root);
        assertion.isSameAs(root);
    }

    @Test(expected = AssertionError.class)
    public void testIsSameAsException(){
        String root = new String("valor");
        String root2 = new String("valor");
        AssertionsTest assertion = new AssertionsTest(root);
        assertion.isSameAs(root2);
    }

    @Test
    public void testIsNotSameAs(){
        String root = new String("valor");
        String root2 = new String("valor");
        AssertionsTest assertion = new AssertionsTest(root);
        assertion.isNotSameAs(root2);
    }

    @Test(expected = AssertionError.class)
    public void testIsNotSameAsException(){
        String root = new String("valor");
        AssertionsTest assertion = new AssertionsTest(root);
        assertion.isNotSameAs(root);
    }


    private class AssertionsTest extends AssertionsBase<String, AssertionsTest>{

        public AssertionsTest(String target) {
            super(target);
        }

        public AssertionsTest(Optional<String> target) {
            super(target);
        }

        @Override
        protected String errorMsg(String msg) {
            return getTargetOpt().isPresent() ? "No elemento " + getTarget() + ": " + msg : msg;
        }
    }
}
