# 使用常见问题Q&A

1. Q：我忘记了本群的触发词。  
   A：可以使用`sudo trigger-get`或`bot help`来查询本群触发词。  
2. Q：我不知道安装模块时要输入的模块id。  
   A：请在[这里](available-modules/index.md)查询所有模块对应的id名称。  
   	* 例如：模块[Arcaea]的内部名称为arcaea，故安装时需要输入 `##install arcaea`
3. Q：我无法调用某个指令或无法获得响应？  
   A：请依次检查：  

   1. 是否正确输入了模块提供的指令  
   2. 模块是否为安装状态  
      * 若未安装，请使用 `##install <模块id>` 指令安装模块，然后按照指引开启模块  

   3. 模块是否为启用状态
      * 若未启用，请使用 `##enable <模块id>` 指令启用模块

   4. 模块是否被设置为仅管理状态  
      * 如果是该状态，而调用者并非管理，但确需调用该功能时，请联系群主或管理员开启权限

4. Q：部分帮助信息好像不全哦……  
   A：由于没有意识到酷Q字符转义的问题，导致有些文字被转义而丢失，在这里向大家致歉。具体详细帮助说明以本网站为准，我也在努力修复这个问题  
   	* 各位看得比较明显的是`<>`符号转义的问题