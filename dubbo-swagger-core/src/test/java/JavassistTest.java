import io.github.springstudent.tool.OsUtil;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;
import javassist.bytecode.annotation.Annotation;
import javassist.bytecode.annotation.IntegerMemberValue;

import java.io.File;
import java.io.FileOutputStream;

/**
 * @author 周宁
 * @Date 2019-12-02 13:29
 */
public class JavassistTest {

    public static void main(String[] args) throws Exception{
        ClassPool pool = ClassPool.getDefault();
        pool.importPackage("java.util.List");
        pool.importPackage("java.lang.Integer");
        // create the class
        CtClass cc = pool.makeClass("foo");
        ClassFile ccFile = cc.getClassFile();
        ConstPool constpool = ccFile.getConstPool();

        // create the annotation
        AnnotationsAttribute attr = new AnnotationsAttribute(constpool, AnnotationsAttribute.visibleTag);
        Annotation annot = new Annotation("MyAnnotation", constpool);
        annot.addMemberValue("value", new IntegerMemberValue(ccFile.getConstPool(), 0));
        attr.addAnnotation(annot);

        // create the method
        CtMethod mthd = CtNewMethod.make("public void getInteger(String[] args) { return null; }", cc);
        cc.addMethod(mthd);
        mthd.getMethodInfo().addAttribute(attr);

        cc.writeFile("./");

        byte[] byteArr = cc.toBytecode();
        FileOutputStream fos = new FileOutputStream(new File(OsUtil.pathJoin("E:\\Intellij\\workspace-hello\\dubbo-swagger\\src\\main\\java\\io\\github\\springstudent\\", "TTTT.class")));
        fos.write(byteArr);
        fos.close();

    }
}
