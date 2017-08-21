package com.dinuscxj.classreplace

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.gradle.api.Project

import java.util.jar.JarEntry
import java.util.jar.JarFile
import java.util.jar.JarOutputStream

public class ClassReplaceTransform extends Transform {
    private static final String PRINT_FORMAT = "class replace: %s \n"

    Project mProject;

    public ClassReplaceTransform(Project project) {
        this.mProject = project
    }

    @Override
    public String getName() {
        return "ClassReplaceTransform"
    }

    @Override
    public Set<QualifiedContent.ContentType> getInputTypes() {
        return TransformManager.CONTENT_CLASS
    }

    @Override
    public Set<? super QualifiedContent.Scope> getScopes() {
        return TransformManager.SCOPE_FULL_PROJECT
    }

    @Override
    public boolean isIncremental() {
        return false
    }

    @Override
    public void transform(TransformInvocation transformInvocation) throws TransformException, InterruptedException, IOException {
        super.transform(transformInvocation);
        TransformOutputProvider outputProvider = transformInvocation.outputProvider;
        Collection<TransformInput> inputs = transformInvocation.inputs;

        def classReplaceItems = [];

        printf PRINT_FORMAT, "start transforming"
        ClassReplaceExtension classReplace = mProject.extensions.findByType(ClassReplaceExtension);
        printf PRINT_FORMAT, "create class replace item"
        classReplace.configFiles.each { File file ->
            file.eachLine { String line ->
                line = line.trim()
                if (!line.isEmpty()) {
                    def splitItems = line.split(ClassReplaceItem.SPLITTER)
                    classReplaceItems.add(new ClassReplaceItem(splitItems[0], splitItems[1]))
                    printf PRINT_FORMAT, splitItems[0] + " -> " + splitItems[1]
                }
            }
        }
        printf PRINT_FORMAT, "create class replace item success"

        inputs.each { TransformInput input ->
            input.directoryInputs.each { DirectoryInput directoryInput ->
                def dest = outputProvider.getContentLocation(directoryInput.name,
                        directoryInput.contentTypes, directoryInput.scopes,
                        Format.DIRECTORY)

                FileUtils.copyDirectory(directoryInput.file, dest)
                printf PRINT_FORMAT, "copy directory " + directoryInput.file.path + " success";
            }

            input.jarInputs.each { JarInput jarInput ->
                def jarInputFile = jarInput.file;

                def dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes,
                        jarInput.scopes, Format.JAR)

                classReplaceItems.each { ClassReplaceItem classReplaceItem ->
                    def isTargetExistShell = "jar -tf ${jarInputFile.path} | grep ${classReplaceItem.target}";
                    if (!execShell(isTargetExistShell, null).isEmpty()) {
                        printf PRINT_FORMAT, "find target class " + classReplaceItem.target + " success"

                        def sourceFilePath = findSourceFile(mProject, classReplaceItem.source)
                        def targetFilePath = classReplaceItem.target
                        def targetFileParent = new File(classReplaceItem.target).parent

                        // both '&&' and '｜' not work
                        [
                                "mkdir -p ${targetFileParent} ",
                                "cp ${sourceFilePath} ${targetFileParent} ",
                                "jar -uf ${jarInputFile.path} ${targetFilePath} ",
                                "rm -rf ${targetFilePath} "
                        ].each {
                            execShell(it, jarInputFile.parentFile)
                        }

                        printf PRINT_FORMAT, "replace " + classReplaceItem.target + " success"
                    }

                }
                FileUtils.copyFile(jarInputFile, dest)

                printf PRINT_FORMAT, "copy jar " + jarInputFile.path + " success";
            }
        }
    }

    private static String execShell(String shell, File directory) {
        return shell.execute(null, directory).getText()
    }

    private static File findSourceFile(Project project, String path) {
        def sourceProjectFile = project.file(path)
        def sourceRootProjectFile = project.rootProject.file(path)
        if (sourceProjectFile.exists()) {
            return sourceProjectFile
        } else if (sourceRootProjectFile.exists()) {
            return sourceRootProjectFile
        } else {
            printf PRINT_FORMAT, "source file not found -> " + sourceProjectFile.path;
            printf PRINT_FORMAT, "source file not found -> " + sourceRootProjectFile.path;

            throw new FileNotFoundException(path)
        }
    }
}
