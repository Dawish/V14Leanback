# V14Leanback
    一个适用于Android TV端的分页加载列表库，控件继承自RecyclerView，部分源码抽取自Google Support v17 Leanback包下源码，可兼容低版本环境。相比原始的RecyclerView，拥有以下特点：</br>
  * 1.自动回焦至被选中的item
  * 2.item滚动居中
  * 3.焦点移动至边界位置时不会出现越界丢失焦点
  * 4.快速滑动和慢速滑动都不会出现丢焦现象
  * 5.支持分页加载
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/Horizontal.gif)
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/vertical.gif)
  
Download
========
You can use Gradle:
```groovy
compile 'io.github.clendy.leanback:v14leanback:1.0.1'
```

Or Maven:
```groovy
<dependency>
  <groupId>io.github.clendy.leanback</groupId>
  <artifactId>v14leanback</artifactId>
  <version>1.0.1</version>
  <type>pom</type>
</dependency>
```

Or Ivy:
```groovy
<dependency org='io.github.clendy.leanback' name='v14leanback' rev='1.0.1'>
  <artifact name='v14leanback' ext='pom' ></artifact>
</dependency>
```
