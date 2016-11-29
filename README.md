# V14Leanback
================
  <p>  一个适用于Android TV端的分页加载列表库，控件继承自RecyclerView，部分源码抽取自Google Support v17 Leanback包下源码，可兼容低版本环境。相比原始的RecyclerView，拥有以下特点： </p>
  <p>1.自动回焦至被选中的item；</p>
  <p>2.item滚动居中;<p/>
  <p>3.焦点移动至边界位置时不会出现越界丢失焦点；<p/>
  <p>4.快速滑动和慢速滑动都不会出现丢焦现象；<p/>
  <p>5.支持分页加载；<p/>
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/horizontal.png)
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/horizontal1.gif)
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/vertical.png)
  ![image](https://github.com/Clendy/V14Leanback/blob/master/screenshots/vertical1.gif)
  
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
