###### Eclipse导入Maven项目 报**Plugin** **execution** **not** **covered** **by** **lifecycle** **configuration**

- 如果项目能够正常进行部署打包，这个问题其实不需要解决，因为并不涉及业务关系，只需要忽略即可。之所以报错，只是环境问题导致的。忽略方法如下：

**手动配置：**

    1. 首先Eclipse中找到Window》Preference》Maven》Lifecycle Mappings找到lifecycle-mapping-metadata.xml文件的加载路径。
    2. http://git.eclipse.org/c/m2e/m2e-core.git/tree/org.eclipse.m2e.lifecyclemapping.defaults/lifecycle-mapping-metadata.xml 使用该文件替换lifecycle-mapping-metadata.xml中的内容（或者根据报错内容进行局部更新）。如果没有lifecycle-mapping-metadata.xml文件，新建即可。
    3. http://git.eclipse.org/c/m2e/m2e-core.git/tree/org.eclipse.m2e.lifecyclemapping.defaults/lifecycle-mapping-metadata.xml 使用该文件替换lifecycle-mapping-metadata.xml中的内容（或者根据报错内容进行局部更新）。如果没有lifecycle-mapping-metadata.xml文件，新建即可。

**自动配置：**

    1. 鼠标悬浮eclipse中报错的pom.xml文件中的空色提示部分，如果给出提示忽略。也可以在这里进行忽略。最终改变的都是lifecycle-mapping-metadata.xml文件。