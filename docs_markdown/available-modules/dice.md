---
tags:
  - Bot 可用模块 
hide:
  - navigation
---

# 掷骰（色）子

[返回模块列表](index.md)

## 使用方法
* 开启模块  
	```text
		#install dice   //安装模块
		#enable dice    //启用模块
	```

* 触发词方式
	```text
	<当前群触发词> dice (param)
	```

	* 返回（0~`param`数值，含0不含`param`值）之间的随机整数
	* `param`可以填入小于`INT_MAX（2147483647）`的任何正整数
	* 可以填写多个`param`，将取第一个返回有效结果
	* 可以不填写`param`，将返回0~6之间的随机整数，相当于扔一个普通六面骰子

*算是使用上最简单的模块啦！*
