package top.niunaijun.aop_core

import javassist.ClassPool
import javassist.CtClass
import javassist.CtConstructor
import javassist.CtField
import javassist.CtMethod
import javassist.CtNewConstructor
import javassist.CtNewMethod
import javassist.bytecode.AccessFlag
import org.gradle.api.Project
import top.niunaijun.aop_api.annotations.Intercept
import top.niunaijun.aop_api.annotations.Intercepts
import top.niunaijun.aop_api.annotations.TimeLog
import top.niunaijun.aop_api.annotations.UIThread
import top.niunaijun.aop_api.annotations.AsyncThread

public class AOPInject {
    //初始化类池
    private final static ClassPool pool = ClassPool.getDefault();

    public static void addJar(String jar) {
        pool.appendClassPath(jar)
    }

    public static void inject(String path, Project project) {
        //将当前路径加入类池,不然找不到这个类
        pool.appendClassPath(path);
        //project.android.bootClasspath 加入android.jar，不然找不到android相关的所有类
        pool.appendClassPath(project.android.bootClasspath[0].toString());
        //引入android.os.Bundle包，因为onCreate方法参数有Bundle
        pool.importPackage("android.os.Bundle");
        pool.importPackage("top.niunaijun.aop_api.AOPCore")
        pool.importPackage("top.niunaijun.aop_api.AOPThreadCore")


        File dir = new File(path);
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
//                println("filePath = " + filePath)
                if (filePath.endsWith(".class")) {
                    String className = filePath.split("/classes/")[1]
                            .replace("/", ".").replace(".class", "")
//                    println("class = " + className)
                    CtClass ctClass = pool.getCtClass(className);
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()
                    boolean write = false
                    ctClass.getDeclaredMethods().each {
                        def orMethodName = it.name
                        if (it.hasAnnotation(TimeLog.class)) {
                            println("AOP TimeLog =====> " + className + ":" + it.name)
                            createProxyMethod(ctClass, it, TimeLog.class, it.name, createTimeLogBody(ctClass, it, orMethodName))
                            write = true
                        }
                        if (it.hasAnnotation(UIThread.class)) {
                            println("AOP UIThread =====> " + className + ":" + it.name)
                            createProxyMethod(ctClass, it, UIThread.class, it.name, createUIThreadBody(ctClass, it))
                            write = true
                        }
                        if (it.hasAnnotation(AsyncThread.class)) {
                            println("AOP AsyncThread =====> " + className + ":" + it.name)
                            createProxyMethod(ctClass, it, AsyncThread.class, it.name, createAsyncThreadBody(ctClass, it))
                            write = true
                        }

                        Intercept intercept = it.getAnnotation(Intercept.class)
                        if (intercept != null) {
                            String interceptStr = String.format(""" if(top.niunaijun.aop_api.AOPCore.intercept("%s")) return;""", intercept.name())
                            //在方法开头插入代码
                            it.insertBefore(interceptStr)
                            println("AOP Intercept:" + intercept.name() + " =====> " + className + ":" + orMethodName)
                            write = true
                        }

                        Intercepts intercepts = it.getAnnotation(Intercepts.class)
                        if (intercepts != null) {
                            String interceptStr = String.format(""" if(top.niunaijun.aop_api.AOPCore.intercept(%s)) return;""",
                                    ArrayToSrc(intercepts.names()))
                            //在方法开头插入代码
                            it.insertBefore(interceptStr);
                            println("AOP Intercepts:" + intercepts.names().join(",") + " =====> " + className + ":" + orMethodName)
                            write = true
                        }
                    }
                    if (write) {
                        ctClass.writeFile(path)
                    }
                    ctClass.detach()//释放
                }
            }
        }
    }

    private static void createProxyMethod(CtClass ctClass, CtMethod ctMethod, Class annotation, String orName, String body) {
        def newName = ctMethod.name + "\$\$" + annotation.simpleName
        ctMethod.setName(newName)
        def proxyMethod = CtNewMethod.make(ctMethod.modifiers, ctMethod.returnType, orName, ctMethod.parameterTypes, ctMethod.exceptionTypes, body, ctClass)
        ctClass.addMethod(proxyMethod)
    }

    private static String createTimeLogBody(CtClass ctClass, CtMethod ctMethod, String orMethodName) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        def newName = orMethodName + "\$\$" + TimeLog.class.simpleName

        //生产的方法返回值
        def methodResult = "${newName}(\$\$);"
        if (!"void".equals(returnType)) {
            //处理返回值
            methodResult = "${returnType} result = " + methodResult
        }
        return "{long costStartTime = System.currentTimeMillis();" +
                //调用原方法 xxx$$Impl() $$表示方法接收的所有参数
                methodResult +
                "android.util.Log.e(\"AOPCore\", \"${ctClass.name}.${ctMethod.name}() 耗时：\" + (System.currentTimeMillis() - costStartTime) + \"ms\");" +
                //处理一下返回值 void 类型不处理
                ("void".equals(returnType) ? "}" : "return result;}")
    }

    private static String createUIThreadBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        if (!"void".equals(returnType)) {
            throw new Exception("@UIThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + UIThread.class.simpleName

        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(\"${ctClass.name}\", \"${newName}\", null, \$args);"
                :
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(\"${ctClass.name}\", \"${newName}\", this, \$args);"
        return "{$methodResult}"
    }

    private static String createAsyncThreadBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        if (!"void".equals(returnType)) {
            throw new Exception("@AsyncThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + AsyncThread.class.simpleName

        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.runAsyncThread(\"${ctClass.name}\", \"${newName}\", null, \$args);"
                :
                "top.niunaijun.aop_api.AOPThreadCore.runAsyncThread(\"${ctClass.name}\", \"${newName}\", this, \$args);"
        return "{$methodResult}"
    }


    private static String ArrayToSrc(String[] strings) {
        List<String> newStr = new ArrayList<>()

        strings.each {
            newStr.add(String.format("\"%s\"", it))
        }

        return "new String[]{" + newStr.join(",") + "}"
    }
}
