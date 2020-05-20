# AndroidTestTool

这是一个安卓测试工具,可以查看App运行过程中输出的日志，并且可以对日志进行筛选和选择复制。当发生崩溃时，会对异常进行捕获并弹出错误日志窗口，你可以进行复制或上传至服务器等操作。

### 预览
![add image](https://github.com/ZKTian/AndroidTestTool/blob/master/screenshots/shot1.jpg)
![add image](https://github.com/ZKTian/AndroidTestTool/blob/master/screenshots/shot2.jpg)
![add image](https://github.com/ZKTian/AndroidTestTool/blob/master/screenshots/shot3.jpg)
![add image](https://github.com/ZKTian/AndroidTestTool/blob/master/screenshots/shot4.jpg)


### 使用说明

你可以直接将testtool引用为library module,也可以引用为.aar到libs中。

```
     在获取悬浮窗权限后在任意位置打开调试工具
     TestToolManager.initTestTool(this);
     
 
     关闭调试工具
    TestToolManager.closeTestTool(this);
  
```

## 参考

* https://github.com/bamsbamx/UltimateLogcat
* https://github.com/Ereza/CustomActivityOnCrash
* https://github.com/fatangare/LogcatViewer

