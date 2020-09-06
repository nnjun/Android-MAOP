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
import top.niunaijun.aop_api.annotations.DelayAsyncThread
import top.niunaijun.aop_api.annotations.DelayUIThread
import top.niunaijun.aop_api.annotations.Intercept
import top.niunaijun.aop_api.annotations.Intercepts
import top.niunaijun.aop_api.annotations.MAOPInit
import top.niunaijun.aop_api.annotations.PrefAll
import top.niunaijun.aop_api.annotations.PrefBoolean
import top.niunaijun.aop_api.annotations.Storage
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
        pool.importPackage("top.niunaijun.aop_api.AOPPrefCore")


        File dir = new File(path);
        if (dir.isDirectory()) {
            //遍历文件夹
            dir.eachFileRecurse { File file ->
                String filePath = file.absolutePath
//                println("filePath = " + filePath)
                if (filePath.endsWith(".class")) {
                    filePath = filePath.replace("\\", "/")
                    String className = filePath.split("/classes/")[1]
                            .replace("/", ".").replace(".class", "")
//                    println("class = " + className)
                    CtClass ctClass = pool.getCtClass(className);
                    //解冻
                    if (ctClass.isFrozen())
                        ctClass.defrost()
                    boolean write = false
                    try {
                        if (ctClass.hasAnnotation(Storage.class)) {
//                            PrefBoolean prefBoolean = it.getAnnotation(PrefBoolean.class)
//                            it.setBody("""{return top.niunaijun.aop_api.AOPPrefCore.prefBoolean("${prefBoolean.name()}", "${prefBoolean.field()}", ${prefBoolean.def()});}""")
                            createStorageBody(ctClass, ctClass.getAnnotation(Storage))
                            write = true
                        }
                        ctClass.getDeclaredMethods().each {
                            def orMethodName = it.name
                            if (it.hasAnnotation(MAOPInit.class)) {
                                println("AOP MAOPInit =====> " + className + ":" + it.name)
                                it.insertBefore("top.niunaijun.aop_api.AOPCore.setContext(\$0);")
                                write = true
                            }
                            if (it.hasAnnotation(TimeLog.class)) {
                                println("AOP TimeLog =====> " + className + ":" + it.name)
                                createProxyMethod(ctClass, it, TimeLog.class, it.name, createTimeLogBody(ctClass, it))
                                write = true
                            }
                            if (it.hasAnnotation(UIThread.class)) {
                                println("AOP UIThread =====> " + className + ":" + it.name)
                                createProxyMethod(ctClass, it, UIThread.class, it.name, createUIThreadBody(ctClass, it))
                                write = true
                            }
                            if (it.hasAnnotation(DelayUIThread.class)) {
                                println("AOP DelayUIThread =====> " + className + ":" + it.name)
                                createProxyMethod(ctClass, it, DelayUIThread.class, it.name, createDelayUIThreadBody(ctClass, it))
                                write = true
                            }
                            if (it.hasAnnotation(AsyncThread.class)) {
                                println("AOP AsyncThread =====> " + className + ":" + it.name)
                                createProxyMethod(ctClass, it, AsyncThread.class, it.name, createAsyncThreadBody(ctClass, it))
                                write = true
                            }
                            if (it.hasAnnotation(DelayAsyncThread.class)) {
                                println("AOP DelayAsyncThread =====> " + className + ":" + it.name)
                                createProxyMethod(ctClass, it, DelayAsyncThread.class, it.name, createDelayAsyncThreadBody(ctClass, it))
                                write = true
                            }

                            Intercept intercept = it.getAnnotation(Intercept.class)
                            if (intercept != null) {
                                String interceptStr = String.format(""" if(top.niunaijun.aop_api.AOPCore.intercept("%s")) return;""", intercept.name())
                                it.insertBefore(interceptStr)
                                println("AOP Intercept:" + intercept.name() + " =====> " + className + ":" + orMethodName)
                                write = true
                            }

                            Intercepts intercepts = it.getAnnotation(Intercepts.class)
                            if (intercepts != null) {
                                String interceptStr = String.format(""" if(top.niunaijun.aop_api.AOPCore.intercept(%s)) return;""",
                                        ArrayToSrc(intercepts.names()))
                                it.insertBefore(interceptStr);
                                println("AOP Intercepts:" + intercepts.names().join(",") + " =====> " + className + ":" + orMethodName)
                                write = true
                            }
                        }
                        if (write) {
                            ctClass.writeFile(path)
                        }
                    } catch(Exception e) {
                        e.printStackTrace()
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

    private static String createTimeLogBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        def newName = ctMethod.name + "\$\$" + TimeLog.class.simpleName

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
            ctClass.detach()
            throw new Exception("@UIThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + UIThread.class.simpleName
        def params = new String[ctMethod.parameterTypes.length]
        ctMethod.parameterTypes.eachWithIndex { CtClass entry, int i ->
            params[i] = entry.class.name
        }
        def paramStr = ArrayToSrc(params)

        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(${ctClass.name}.class, \"${newName}\", null, \$args);"
                :
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(${ctClass.name}.class, \"${newName}\", this, \$args);"
        return "{$methodResult}"
    }

    private static String createDelayUIThreadBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        if (!"void".equals(returnType)) {
            ctClass.detach()
            throw new Exception("@DelayUIThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + DelayUIThread.class.simpleName
        def params = new String[ctMethod.parameterTypes.length]
        ctMethod.parameterTypes.eachWithIndex { CtClass entry, int i ->
            params[i] = entry.class.name
        }
        def paramStr = ArrayToSrc(params)

        DelayUIThread delayUIThread = ctMethod.getAnnotation(DelayUIThread.class)
        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(${ctClass.name}.class, \"${newName}\", null, \$args, (long) ${delayUIThread.delayTime()});"
                :
                "top.niunaijun.aop_api.AOPThreadCore.runUIThread(${ctClass.name}.class, \"${newName}\", this, \$args, (long) ${delayUIThread.delayTime()});"
        return "{$methodResult}"
    }

    private static String createAsyncThreadBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        if (!"void".equals(returnType)) {
            ctClass.detach()
            throw new Exception("@AsyncThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + AsyncThread.class.simpleName

        def params = new String[ctMethod.parameterTypes.length]
        ctMethod.parameterTypes.eachWithIndex { CtClass entry, int i ->
            params[i] = entry.class.name
        }
        def paramStr = ArrayToSrc(params)
        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.runAsyncThread(${ctClass.name}.class, \"${newName}\", null, \$args);"
                :
                "top.niunaijun.aop_api.AOPThreadCore.runAsyncThread(${ctClass.name}.class, \"${newName}\", this, \$args);"
        return "{$methodResult}"
    }

    private static String createDelayAsyncThreadBody(CtClass ctClass, CtMethod ctMethod) {
        //方法返回类型
        def returnType = ctMethod.returnType.name
        if (!"void".equals(returnType)) {
            ctClass.detach()
            throw new Exception("@DelayAsyncThread是一个异步方法，方法返回值必须是void")
        }
        def newName = ctMethod.name + "\$\$" + DelayAsyncThread.class.simpleName

        def params = new String[ctMethod.parameterTypes.length]
        ctMethod.parameterTypes.eachWithIndex { CtClass entry, int i ->
            params[i] = entry.class.name
        }
        def paramStr = ArrayToSrc(params)

        DelayAsyncThread delayAsyncThread = ctMethod.getAnnotation(DelayAsyncThread.class)
        def methodResult = ctMethod.methodInfo.getAccessFlags() & AccessFlag.STATIC ?
                "top.niunaijun.aop_api.AOPThreadCore.delayAsyncThread(${ctClass.name}.class, \"${newName}\", null, \$args, (long) ${delayAsyncThread.delayTime()});"
                :
                "top.niunaijun.aop_api.AOPThreadCore.delayAsyncThread(${ctClass.name}.class, \"${newName}\", this, \$args, (long) ${delayAsyncThread.delayTime()});"
        return "{$methodResult}"
    }

    private static void createStorageBody(CtClass ctClass, Storage storage) {
        def getStorageMethod = CtNewMethod.make("""public ${ctClass.name} getStorage() {return top.niunaijun.aop_api.AOPPrefCore.getStorage("${storage.name()}", "${ctClass.name}");} """, ctClass)
        ctClass.addMethod(getStorageMethod)

        def setStorageMethod = CtNewMethod.make("""public void setStorage() {top.niunaijun.aop_api.AOPPrefCore.setStorage(\$0, "${storage.name()}", "${ctClass.name}");} """, ctClass)
        ctClass.addMethod(setStorageMethod)
    }

    private static String ArrayToSrc(String[] strings) {
        List<String> newStr = new ArrayList<>()

        strings.each {
            newStr.add(String.format("\"%s\"", it))
        }

        return "new String[]{" + newStr.join(",") + "}"
    }
}
