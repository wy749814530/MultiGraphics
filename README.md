# MultiGraphics
 
##自定义多区域可拖拽图形

### 参数讲解
```java
    <com.views.graphics.MultiGraphicsView
        android:id="@+id/graphicsView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        apps:multi_delImage="@mipmap/ic_launcher"
        apps:multi_generalColor="@color/colorAccent"
        apps:multi_max="6"
        apps:multi_selectColor="@color/blue_color"
        apps:multi_showPoint="true"
        apps:multi_showTable="true" />
       

    multi_delImage="@mipmap/ic_launcher"  //删除图标
    multi_generalColor="@color/colorAccent"// 未选中图形的边框颜色
    multi_max="6"                         // 做多支持多少个图形
    multi_selectColor="@color/blue_color" // 选中图标的颜色
    multi_showPoint="true"                // bool 是否显示当前点击位置
    multi_showTable="true"                // 是否显示底部网格
```
### 方法

#### 向控件中添加一个矩形区域图形
```java
 graphicsView.addNewArea();
```
#### 向控件中添加多个区域图形
```java
  ArrayList<PointBean> paintingArea = new ArrayList<>();
  paintingArea.add(new PointBean(100f, 100f, 0));
  paintingArea.add(new PointBean(340f, 120f, 1));
  paintingArea.add(new PointBean(320f, 320f, 2));
  paintingArea.add(new PointBean(370f, 380f, 3));
  paintingArea.add(new PointBean(90f, 330f, 4));

  MultiGraphicsView.GraphicsObj currentArea = new MultiGraphicsView.GraphicsObj();
  currentArea.setAraa(paintingArea, false);
  
  graphicsView.addAreaBeans(currentArea);
```

#### 设置控件中图形边框线条是否显示虚线
```java
 graphicsView.setDottedLine(false);
```

#### 删除控件中当前选中的图形
```java
 graphicsView.delCurrentGraphics(false);
```

#### 设置控件图形监听事件
```java
graphicsView.setOnDelClickListener(new MultiGraphicsView.OnDelClickListener() {
      @Override
      public void onDelClicked() {
          Toast.makeText(MutilAreaActivity.this, "点击了删除按钮，删除图形", Toast.LENGTH_LONG).show();
          graphicsView.delCurrentGraphics();
      }

      @Override
      public void onMiniArea() {
          Toast.makeText(MutilAreaActivity.this, "到达最小区域", Toast.LENGTH_LONG).show();
      }
  });
```
 
