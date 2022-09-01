---
tags:
  - Bot 可用模块
hide:
  - navigation
---

# 密码学小助手

[返回模块列表](index.md)

## 使用方法
* 开启模块
	```text
		##install crypto //安装模块
		##enable crypto  //启用模块
	```
	
* 触发词方式
	```text
	加密工具：<本群触发词> crypto digest <算法名> <message>
	摘要工具：<本群触发词> crypto [encode|decode] <算法名> <message> <key>
	显示帮助：<本群触发词> crypto help
	```
	- 使用标准加密及摘要算法对某一段信息进行运算
	- 支持的摘要算法：md5、sha1、sha256、sha512
	- 支持的密钥算法：base64、DES、3DES
		* DES使用的模式是DES/ECB/PKCS5Padding，只支持8位密钥
		* 3DES使用的模式为DESede
		* 所有的输出使用Hex编码，后续将考虑支持base64输出选项
		