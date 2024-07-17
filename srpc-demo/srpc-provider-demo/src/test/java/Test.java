import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class Test {

    public static void main(String[] args) throws InvocationTargetException, IllegalAccessException {
        Class<TestClass> testClass = TestClass.class;
        Method[] methods = testClass.getMethods();
        for (Method method : methods) {
            if (method.getName().equals("sayHello")) {

                System.out.println(method.invoke(new TestClass(), "hello"));

            }

            System.out.println(method.getName());
        }

    }
}
