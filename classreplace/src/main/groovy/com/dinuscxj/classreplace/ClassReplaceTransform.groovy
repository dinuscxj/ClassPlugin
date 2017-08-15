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
                def newJarInputFile = createTempFile(jarInputFile)

                def dest = outputProvider.getContentLocation(jarInput.name, jarInput.contentTypes,
                        jarInput.scopes, Format.JAR)

                try {
                    JarFile jarFile = new JarFile(jarInputFile);
                    classReplaceItems.each { ClassReplaceItem classReplaceItem ->
                        JarEntry targetJarEntry = jarFile.getJarEntry(classReplaceItem.target);
                        if (targetJarEntry != null) {
                            printf PRINT_FORMAT, "find target class " + targetJarEntry.name + " success"
                            JarOutputStream newJarOutputStream =
                                    new JarOutputStream(new FileOutputStream(newJarInputFile))

                            jarFile.entries().each { JarEntry jarEntry ->
                                InputStream inputStream
                                if (jarEntry.name == classReplaceItem.target) {
                                    def sourceFile = findSourceFile(mProject, classReplaceItem.source)
                                    inputStream = new FileInputStream(sourceFile)

                                    //Mac BetterZip can't unzip, Who know why ?
                                    //command (unzip path) can work well
                                    def newJarEntry = new JarEntry(jarEntry.name)
                                    newJarEntry.size = inputStream.available()
                                    newJarEntry.method = jarEntry.DEFLATED
                                    newJarEntry.time = 0
                                    newJarEntry.extra = jarEntry.extra
                                    newJarEntry.crc = FileUtils.checksumCRC32(sourceFile)
                                    jarEntry = newJarEntry
                                } else {
                                    inputStream = jarFile.getInputStream(jarEntry)
                                }

                                newJarOutputStream.putNextEntry(jarEntry)

                                try {
                                    IOUtils.copy(inputStream, newJarOutputStream);
                                } finally {
                                    IOUtils.closeQuietly(inputStream);
                                }

                                newJarOutputStream.closeEntry()
                            }
                            newJarOutputStream.finish()
                            newJarOutputStream.flush()
                            newJarOutputStream.close()
                            printf PRINT_FORMAT, "replace " + targetJarEntry.name + " success"
                        }
                    }

                    if (newJarInputFile.exists()) {
                        FileUtils.copyFile(newJarInputFile, dest)
                        FileUtils.deleteQuietly(newJarInputFile)
                    } else {
                        FileUtils.copyFile(jarInputFile, dest)
                    }

                } catch (Throwable e) {
                    e.printStackTrace()
                    if (newJarInputFile.exists()) {
                        FileUtils.deleteQuietly(newJarInputFile)
                    }

                    throw e;
                }

                printf PRINT_FORMAT, "copy jar " + jarInputFile.path + " success";
            }
        }
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

    private static File createTempFile(File file) {
        def targetFileName = "temp_" + System.currentTimeMillis() + ".jar"

        File targetFile = new File(file.getParentFile(), targetFileName);
        targetFile.delete()

        return targetFile
    }
}
