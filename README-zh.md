## ClassPlugin: 灵活的Class替换插件               

[English](https://github.com/dinuscxj/ClassPlugin) | 中文版 <br/>

![](https://raw.githubusercontent.com/dinuscxj/ClassPlugin/master/logo/class_replace_logo.jpeg?width=300)<br/>

[ClassPlugin](https://github.com/dinuscxj/ClassPlugin) 是一个灵活的class替换插件, 
`ClassPlugin` 可以用来替换包含在libs目录的jar和gradle依赖引入的jar中的class文件。
如果你遇到一些第三方jar的class有一些bug, `ClassPlugin` 或许是最好解决的方式。

## 注入
添加这个plugin到你的 `buildscript`:

```gradle
    buildscript {
        repositories {
            maven {
               url "https://dl.bintray.com/dinuscxj/maven"
            }
        }
    
        dependencies {
            classpath 'com.dinuscxj:classreplace:1.0.3'
        }
    }
```

然后应用到你的`module`中

``` gradle 
    apply plugin: 'com.dinuscxj.classreplace'
``` 

最后配置`classreplace` 

``` gradle 
    classreplace {
        sourceType 'class' 
        configFiles file('src/classreplace/class-replace-config.txt')
    }
```  

## 配置文件
配置文件的格式
``` txt
    ${原类路径}:${目标类路径}
``` 

**${原类路径}:** 基于当前项目的相对路径 <br/>
**${目标类路径}:** 在jar中的entry name（和文件路径相似）

# 如何声称原始类

* 新建一个与目标类一样的包名
* 新建一个与目标类类名一样的类
* 复制目标类的`java`代码到新建的类
* 做一些代码修改
* 构建新建类所在`module`（构建完不要忘了删除此类）
* 在build/intermediates/classes/{包名}/{类名}查找所生成的类

## Misc

  ***QQ群:*** **342748245**
  
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
