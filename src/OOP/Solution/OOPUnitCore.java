package OOP.Solution;

import OOP.Provided.OOPAssertionFailure;
import OOP.Provided.OOPExceptionMismatchError;
import OOP.Provided.OOPExpectedException;
import OOP.Provided.OOPResult;
import OOP.Provided.OOPResult.*;


import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

public class OOPUnitCore {
    public static void assertEquals (Object expected, Object actual) throws OOPAssertionFailure{
        if (!expected.equals(actual))
            throw new OOPAssertionFailure(expected, actual);
    }

    public static void fail(){
        throw  new OOPAssertionFailure();
    }

    public static OOPTestSummary runClass(Class<?> testClass) throws IllegalArgumentException{
        return runClass(testClass, null);
    }
    public static OOPTestSummary runClass(Class<?> testClass, String tag) throws IllegalArgumentException{
        if(testClass==null || !testClass.isAnnotationPresent(OOPTestClass.class))
            throw new IllegalArgumentException();
        Object o=null;
        try {
            Constructor constructor = testClass.getConstructor();
            constructor.setAccessible(true);
            o = constructor.newInstance();
        }
        catch (Exception e){}
        List<Method> methods = new ArrayList<>();
        Class<?> c = testClass;
        while(c!=Object.class){
            methods.addAll(Arrays.stream(c.getDeclaredMethods()).filter(m->!methods.contains(m)).collect(Collectors.toList()));
            c=c.getSuperclass();
        }
        Collections.reverse(methods);
        for(Method m : methods){
            if(m.isAnnotationPresent(OOPSetup.class)) {
                m.setAccessible(true);
                try {
                    m.invoke(o);
                }
                catch (Exception e) {}
            }
        }
        List<Method> tests = methods.stream().filter(m -> m.isAnnotationPresent(OOPTest.class)).sorted((a, b) -> a.getAnnotation(OOPTest.class).order() - b.getAnnotation(OOPTest.class).order()).collect(Collectors.toList());
        if(tag!=null) {
            tests=tests.stream().filter(m->m.getAnnotation(OOPTest.class).tag().equals(tag)).collect(Collectors.toList());
        }
        List<Method> befores = methods.stream().filter(m-> m.isAnnotationPresent(OOPBefore.class)).collect(Collectors.toList());
        List<Method> afters = methods.stream().filter(m-> m.isAnnotationPresent(OOPAfter.class)).sorted((a,b)->a.getAnnotation(OOPTest.class).order()-b.getAnnotation(OOPTest.class).order()).collect(Collectors.toList());
        Collections.reverse(afters);
        Map<String, OOPResult> testmap = new HashMap<>();
        for(Method m : tests) {
            boolean before_fail=false;
            for(Method b : befores){
                if(Arrays.asList(b.getAnnotation(OOPBefore.class).value()).contains(m.getName())) {
                    Object o_copy=getCopy(testClass, o);
                    b.setAccessible(true);
                    try {
                        b.invoke(o);
                    } catch (Exception e) {
                        o = o_copy;
                        testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.ERROR, e.getClass().getName()));
                        before_fail=true;
                        break;
                    }
                }
            }
            if(before_fail){
                continue;
            }
            m.setAccessible(true);
            try {
                m.invoke(o);
                testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.SUCCESS, null));
            }
            catch (Exception e) {
                OOPExpectedException exp=null;
                try {
                    Field exp_field =Arrays.stream(testClass.getDeclaredFields()).filter(f -> f.isAnnotationPresent(OOPExceptionRule.class)).collect(Collectors.toList()).get(0);
                    exp_field.setAccessible(true);
                    exp = (OOPExpectedException)exp_field.get(o);
                }
                catch (IllegalAccessException ex){}
                if (exp != null) {
                    if(exp.assertExpected(e)){
                        System.out.println("Succ");
                        testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.SUCCESS, null));
                    }
                    else{
                        if(e.getCause().getClass()==OOPAssertionFailure.class){
                            System.out.println("Failure");
                            testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.FAILURE, e.getMessage()));
                        }
                        else {
                            if(exp.getExpectedException() != null) {
                                testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.EXPECTED_EXCEPTION_MISMATCH, new OOPExceptionMismatchError(e.getClass(), exp.getExpectedException()).getMessage()));
                            }
                            else {
                                testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.ERROR, e.getClass().getName()));
                            }
                        }
                    }
                }
            }
            for(Method a : afters){
                if(Arrays.asList(a.getAnnotation(OOPAfter.class).value()).contains(m.getName())) {
                    Object o_copy=getCopy(testClass, o);
                    a.setAccessible(true);
                    try {
                        a.invoke(o);
                    }
                    catch (Exception e) {
                        o = o_copy;
                        testmap.put(m.getName(), new OOPResultImpl(OOPTestResult.ERROR, e.getClass().getName()));
                        break;
                    }
                }
            }
        }
        return new OOPTestSummary(testmap);
    }

    public static Object getCopy(Class<?> testClass, Object o){
        Object o_copy = null;
        try {
            Constructor constructor = testClass.getConstructor();
            constructor.setAccessible(true);
            o_copy = constructor.newInstance();
        } catch (Exception e) {
        }
        for (Field f : testClass.getDeclaredFields()) {
            try {
                f.setAccessible(true);
                Object field_obj = f.get(o);
                if (field_obj instanceof Cloneable) {
                    Method clone_method = field_obj.getClass().getDeclaredMethod("clone");
                    clone_method.setAccessible(true);
                    Object field_obj_copy = clone_method.invoke(field_obj);
                    f.set(o_copy, field_obj_copy);
                } else {
                    try {
                        Constructor copy_constructor = field_obj.getClass().getDeclaredConstructor(field_obj.getClass());
                        copy_constructor.setAccessible(true);
                        Object field_obj_copy = copy_constructor.newInstance(field_obj);
                        f.set(o_copy, field_obj_copy);
                    } catch (Exception e) {
                        f.set(o_copy, field_obj);
                    }
                }
            } catch (Exception e) {}
        }
        return o_copy;
    }
}
