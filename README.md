## ClassPlugin: Flexible Class Replacement Plugin               

English | [中文版](https://github.com/dinuscxj/ClassPlugin/blob/master/README-zh.md)<br/>

![](https://raw.githubusercontent.com/dinuscxj/ClassPlugin/master/logo/class_replace_logo.jpeg?width=300)<br/>

[ClassPlugin](https://github.com/dinuscxj/ClassPlugin) is a flexible class replacement plugin for gradle, 
the `ClassPlugin` can be used to replace the class of the jar which contained in both the libs and the gradle dependency.
if you encounter some bugs on the third-party jars, the `ClassPlugin` will be the best way to solve it.

## Installation
To use `ClassPlugin` in a `module`, add the plugin to your `buildscript`:

```gradle
    buildscript {
        repositories {
            maven {
               url "https://dl.bintray.com/dinuscxj/maven"
            }
        }
    
        dependencies {
            classpath 'com.dinuscxj:classreplace:1.0.2'
        }
    }
```

And then apply it in your `module`:

``` gradle 
    apply plugin: 'com.dinuscxj.classreplace'
``` 

Last config the `classreplace` Extension

``` gradle 
    classreplace {
        sourceType 'class' 
        configFiles file('src/classreplace/class-replace-config.txt')
    }
```  

## Config Files
The format of the config file
``` txt
    ${source class path}:${target class path}
``` 

**${source class path}:** The relative classpath based on the current project is used to replace $ {target class path}<br/>
**${target class path}:** The jar entry name of the class which you are replaced with the ${source class path}

# How to generate source class 

* New the `package name` same as the `target class` that you are replaced  
* New the `java class` same as the `target class` that you are replaced
* Copy the `target class` content to the new `java class`
* Do some changes
* Build the `module` or the `project`  
* Find the generated class on the path: build/intermediates/classes/{package name}/{class name}

## Misc

  ***QQ Group:*** **342748245**
  
## License

    Copyright 2015-2019 dinus

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
