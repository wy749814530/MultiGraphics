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


 
