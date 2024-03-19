import io.github.springstudent.third.GenericReplaceBuilder;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author 周宁
 * @Date 2019-12-02 14:52
 */
public class ThirdTest {

    public static void main(String[] args) {
        Method[] methods = TimPlst.class.getDeclaredMethods();
        GenericReplaceBuilder.initGenericReplaceBuilder("");
        for(Method method : methods){
            Type[] types = method.getGenericParameterTypes();
            for(Type type : types){
                GenericReplaceBuilder.buildReplaceClass(type,null);
            }
        }

    }
}
