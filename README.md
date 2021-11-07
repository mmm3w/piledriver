# PileDriver

字节码插装插件

在项目根目录的`build.gradle`中添加
```
    maven { url 'https://raw.githubusercontent.com/mmm3w/maven/main'}

    classpath 'com.mitsuki.armory:piledriver:$version'
```
version详见[maven](https://github.com/mmm3w/maven)README

#### 方法耗时统计

具体实现参照[MethodTraceMan](https://github.com/zhengcx/MethodTraceMan)

1、配置插件，在application模块的`build.gradle`中添加
```
plugins {
    id 'exectiming'
}
```
2、配置插件，同在上个文件中添加以下内容
execTiming {
    enable true //是否启用
    traceRules "${project.projectDir}/trace-rules.txt" //插装规则
}

3、添加`trace-rules.txt`，并配置插装规则
`-pick`:需要插装的类或者包名 例：`-pick com/mitsuki/armory`，在未配置pick的情况下会给所有包的类插装
`-keep`:排除插桩项目，相关类或包名下的类的方法不会插入代码
Tips：例：`-pick @com/mitsuki/armory/MainActivity`，通过@可以精准匹配类名，`-keep`同理

