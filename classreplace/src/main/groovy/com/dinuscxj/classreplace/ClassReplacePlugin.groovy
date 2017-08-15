package com.dinuscxj.classreplace

import com.android.build.gradle.AppExtension
import org.gradle.api.Plugin
import org.gradle.api.Project

public class ClassReplacePlugin implements Plugin<Project> {
    private static final String CLASS_REPLACE_NAME = "classreplace"

    @Override
    void apply(Project project) {
        project.extensions.create(CLASS_REPLACE_NAME, ClassReplaceExtension)

        def android = project.extensions.getByType(AppExtension)
        def transform = new ClassReplaceTransform(project)
        android.registerTransform(transform)
    }
}