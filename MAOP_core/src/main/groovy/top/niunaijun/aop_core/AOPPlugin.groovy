package top.niunaijun.aop_core

import org.gradle.api.Plugin
import org.gradle.api.Project

import com.android.build.gradle.AppExtension

public class AOPPlugin implements Plugin<Project> {

    void apply(Project project) {
        def android = project.extensions.getByType(AppExtension)
        //注册一个Transform
        def classTransform = new AOPTransform(project);
        android.registerTransform(classTransform);
    }
}