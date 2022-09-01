# JRbot/LetheBot 使用协议 ![agreement-version](https://img.shields.io/badge/%E5%8D%8F%E8%AE%AE%E7%89%88%E6%9C%AC-1.0-blue) ![agreement-date](https://img.shields.io/badge/%E5%8D%8F%E8%AE%AE%E7%94%9F%E6%95%88%E4%BA%8E-2020.6.27-important)

在使用JRbot/LetheBot（下称“本bot”）提供的服务时，您等同于同意下列所述的规定，并尽到自己应有的义务来使用它。

## 名词解释
* JRbot或LetheBot：本bot的唯一标识。
* 用户：所有欲使用本bot提供功能的QQ群成员或QQ私聊用户。
* 服务：本bot提供的服务包括其中附带的模块和帮助文档，及反馈通道。
* 触发词：为了使用bot提供的功能，需要在指令前增加的额外单词。例如某群的触发词为`bot`，则在调用所有普通功能前，需要在所有指令之前添加`bot`。
* 特殊指令：以符号`#`开头的指令，通常用于群管理(`#`)和模块管理(`##`)。
* 功能：完成一项给定任务，本bot代码组成的最小单位。
* 模块：是一系列“功能”的集合，通常包含一些目标相近或调用背景相同的功能。
* 状态：bot所处在的状态，会影响部分功能的调用情况。本bot目前拥有两种状态：正常使用状态和系统维护状态。  
    - 在系统维护状态时，您暂时不能添加本bot为好友，所有功能将暂时不可被调用（表现为无任何反应），但是定时任务将照常运行。

## Bot的服务范围和目标
* 目前，本Bot的服务重心为音游群，可服务于50人以上群组。在符合相关法律规定的前提下，开发者（本人）将尽力保证Bot正确、有效地提供服务。
* 本Bot使用[酷Q Java SDK](https://cqp.cc/t/37318 "JCQ下载地址") 开发，运行于酷Q Pro上，发图、撤回功能由酷Q Pro保证。且开发者（本人）会定期续费，保证酷Q Pro和本bot的正常运行。
* 所有用户均可以通过私聊bot来报告使用中遇到的各种问题。开发者（本人）会定期查看这些问题并尝试解决。
* 本bot的每次更新会发布在其QQ空间中，以方便用户查看。

## Bot的服务期限
* 在将bot拉入自己的群后，群管理员有义务在6天内及时使用`#init`功能来激活本bot。开发者有权使bot退出逾期未激活的群组，以保证bot运行资源不致浪费。
    - 已激活使用的群组可以无需执行此步骤，或者执行`#reinit`来重置功能，所有已经安装及启用的模块将被卸载。
* 本bot暂时不开通打赏/发电系统，任何以本bot为名义的打赏/发电活动均为诈骗，欢迎将有关信息告知我，经核实后将统一对非法来源进行处置。
* 本使用协议文本于2020.6.27生效，自将本bot拉入群并激活本bot后，视同您同意本使用协议的文本。开发者（本人）保留随时更改使用协议文本的权力。如果您不同意协议文本的某些条款，欲终止服务，您可以：
    - 在激活功能之后，群管理员或群主使用`#quit`指令，使bot退出该群。
    - 在激活功能之前，暂时先使用`#init`激活，转化为激活状态，然后使用`#quit`令其退群。
    - 私聊bot，使用[反馈功能](communicate-with-private-msg/bug-reporting.md)，写上要退群的群号。

## 您应尽到的义务
* 遵循中华人民共和国法律，正确使用本bot提供的一切服务，传播积极、正当的信息。
* 将本bot设置为管理员及更高等级权限时，合理利用禁言及欢迎语功能，教育群成员正确使用bot。
* 本bot所运行的服务器资源有限，切勿短时间内重复调用同一功能，除非开发者（本人）同意，禁止进行压测行为。
* 切勿尝试对开发者服务器进行拒绝服务、端口扫描、漏洞利用攻击，这是损人不利己的行为！

## 惩罚性处理
* 本bot已实装黑名单系统(blocklist)。一旦错误行为被记录，本bot将不再对其中记录的QQ号、群号提供服务。
* 可能引起被记录黑名单的行为不限于下列描述：
    - 使用不文明语言辱骂、诽谤、排挤bot
    - 滥用反馈功能传播广告
    - 禁言或踢出bot（以bot为直接目标，全群禁言或解散不算在内）
    - 爆发式调用功能（同一群、同一账号1分钟内调用同一功能超过20次）
    - 广告群、涉政群、涉黄群
* 黑名单持续时间均为永久，出黑看态度和所在环境。