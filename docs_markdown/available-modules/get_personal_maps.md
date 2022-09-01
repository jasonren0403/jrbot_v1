# 个人自制谱下载

!!! note   
	本功能只限我的个人谱群使用 <a target="_blank" href="//shang.qq.com/wpa/qunwpa?idkey=9b4f3231f6aaffcf5f9bd3e9ad074e297edbd0d9cc1f457b49c5a3a1e0919843"><img border="0" src="//pub.idqqimg.com/wpa/images/group.png" alt="音游自制谱个人发布群|Arcaea→RM计划|加入群" title="音游自制谱个人发布群|Arcaea→RM计划"></a>

* 提供一个方便的方法以下载到我的音游自制谱面（在个人自制谱下载网站开发完成之前）

## 使用方法
### 查找自制谱面
```text
<本群触发词> find_map <mapName> [/private]
```

* `mapName`指要查找的谱面名称，可模糊查找，**必填**，例如：
  ```text
  > bot find_map 病名为爱
    [省略at人]
    找到一个可能的结果
    imd谱面: 病名为爱（女声中文版）
    请使用dl_map <type> <mapname> 命令获取下载链接
  ```

* 指定`/private`开关，可将查询到的结果发送至私聊，不会打扰到其他群员
	- 以后将支持私聊查询功能，敬请期待

### 获取自制谱面

```text
<本群触发词> dl_map <type> <mapName> [/private]
```

* `type`为上述查找过程中获得的`xxx谱面`中的`xxx`的值，可能的值有`imd|cyt`两种， **必填**
* `mapName`为要下载自制谱的曲名
	- 请用引号括起带空格的曲名

* 指定`/private`开关，可将查询到的结果发送至私聊，不会打扰到其他群员

[返回模块列表](index.md)
