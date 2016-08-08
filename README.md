# RiskControl —— 实时业务风控系统
## 背景
当前互联网企业存在很多业务风险，有些风险（比如薅羊毛）虽然没有sql注入漏洞利用来的直接，但是一直被羊毛党、刷单党光顾的企业长期生存下来的几率会很低！

* 账号：垃圾注册、撞库、盗号等
* 交易：盗刷、恶意占用资源、篡改交易金额等
* 活动：薅羊毛
* 短信：短信轰炸

## 项目介绍
**实时业务风控系统是分析风险事件，根据场景动态调整规则，实现自动精准预警风险的系统。**

> 本项目只提供实时风控系统框架基础和代码模板。

### 需要解决的问题
* 哪些是风险事件，注册、登录、交易、活动等事件，需要业务埋点配合提供实时数据接入
* 什么样的事件是有风险的，风险分析需要用到统计学，对异常用户的历史数据做统计分析，找出异于正常用户的特征
* 实时性，风险事件的分析必须毫秒级响应，有些场景下需要尽快拦截，能够给用户止损挽回损失
* 低误报，这需要人工风控经验，对各种场景风险阈值和评分的设置，需要长期不断的调整，所以灵活的规则引擎是很重要的
* 支持对历史数据的回溯，能够发现以前的风险，或许能够找到一些特征供参考

### 项目关键字
* 轻量级，可扩展，实时的Java业务风控系统
* 基于Spring boot构建，配置文件能少则少
* 使用drools规则引擎管理风控规则，原则上可以动态配置规则
* 使用redis、mongodb做风控计算和事件储存，历史事件支持水平扩展

## 原理
### 统计学
* 次数统计，比如1分钟内某账号的登录次数，可以用来分析盗号等
* 频数统计，比如1小时内某ip上出现的账号，可以用来分析黄牛党等
* 最大统计，比如用户交易金额比历史交易都大，可能有风险
* 最近统计，比如最近一次交易才过数秒，可能机器下单
* 行为习惯，比如用户常用登录地址，用户经常登录时间段，可以用来分析盗号等

**抽象：某时间段，在条件维度（可以是多个维度复合）下，利用统计方法统计结果维度的值。充分发挥你的想象吧！**

### 实时计算
要将任意维度的历史数据（可能半年或更久）实时统计出结果，需要将数据提前安装特殊结果准备好（由于事件的维度数量不固定的，选取统计的维度也是随意的，所以不是在关系数据库中建几个索引就能搞定的），需要利用空间换时间，来降低时间复杂度。

### redis
redis中数据结构sortedset，是个有序的集合，集合中只会出现最新的唯一的值。利用sortedset的天然优势，做频数统计非常有利。

比如1小时内某ip上出现的账号数量统计：

* 保存维度

	ZADD key score member（时间复杂度:O(M*log(N))， N 是有序集的基数， M 为成功添加的新成员的数量），key=ip，score=时间（比如20160807121314），member=账号。存储时略耗性能。
	结构如下：

		1.1.1.1
			|--账号1		20160807121314
			|--账号2		20160807121315
			|--账号n		20160807121316

		2.2.2.2
			|--账号3		20160807121314
			|--账号4		20160807121315
			|--账号m		20160807121316

* 计算频数

	ZCOUNT key min max（时间复杂度:O(1)），key=ip，min=起始时间，max=截止时间。计算的性能消耗极少，优势明显
* redis lua

	把保存维度，计算频数，过期维度数据等操作，使用lua脚本结合在一起，可以减少网络IO，提高性能


### mongodb
mongodb本身的聚合函数统计维度，支持很多比如：max，min，sum，avg，first，last，标准差，采样标准差，复杂的统计方法可以在基础聚合函数上建立，比如行为习惯：

	getDB().getCollection(collectionName).aggregate(
                Arrays.asList(
                        match(match)													--匹配条件维度
                        , group("$" + field, Accumulators.sum("_count", 1))				--求值维度的次数
                        , match(new Document("_count", new Document("$gte", minCount))) --过滤，超过minCount才统计
                        , sort(new Document("_count", -1))								--对次数进行倒叙排列
                )
        );

建议在mongodb聚合的维度上建立索引，这样可以使用内存计算，速度较快。

> redis性能优于mongodb，所以使用场景较多的频数计算默认在redis中运行，参考代码DimensionService.distinctCountWithRedis方法。但是redis为了性能牺牲了很多空间，数据重复存储，会占用很多内存。

### 风控流程
1. 黑名单
2. 白名单
3. 从细颗粒到粗颗粒，依次执行1和2，将所有黑白名单遍历
4. 风控规则
5. 阈值预警
6. 保存事件

## 环境准备
* mysql，数据结构在import.sql中定义了
* redis
* mongodb，建议使用分片集群

## 项目配置
* 应用配置：application.properties
* 日志配置：logback.xml
* 规则配置：rules/*.drl，规则都是用java语言编写。默认配置了登录事件的部分规则


drl文件说明：

		package rules;										--规则包路径

		import com.example.riskcontrol.model.LoginEvent		--引入类
		import com.example.riskcontrol.service.DimensionService
		import com.example.riskcontrol.model.EnumTimePeriod
		
		global DimensionService dimensionService			--引入外部服务

		rule "98_login_ip"          						--规则名称，全局唯一
		    salience 98										--规则优先级，值越大越先执行
		    lock-on-active true								--事件不重复执行该规则
		    when											--条件判断，是否需要进入action
		        event:LoginEvent()							--判断事件对象是否是LoginEvent类
		    then											--action
		        int count  = dimensionService.distinctCount(event,new String[]{LoginEvent.OPERATEIP},EnumTimePeriod.LASTHOUR,LoginEvent.MOBILE);		--近1小时内该事件ip上出现的mobile数量统计
		        if(event.addScore(count,20,10,1)){										--如果统计结果超过20个，则记10分，并且结果每超1个，再多记1分
		            dimensionService.insertRiskEvent(event,"近1小时内同ip出现多个mobile,count="+count);  --记录风险事件日志
		        }		
		end													--结束规则

> drools的详细文档，请参考官方	[http://docs.jboss.org/drools/release/6.4.0.Final/drools-docs/html_single/index.html](http://docs.jboss.org/drools/release/6.4.0.Final/drools-docs/html_single/index.html)

## 部署
系统默认采用jar打包和运行，建议集群方式部署，然后使用反向代理做负载均衡。
### 打包

	mvn clean install

### 运行
建议jdk 8

	java -jar riskcontrol-*.jar


### war包部署
如果需要tomcat等容器部署，也可将配置打包方式修改成war包方式，修改pom.xml

	<packaging>war</packaging>


## 风控分析入口
* 请求：http://domain/riskcontrol/req?json=JSON.toJsonString(LoginEvent)
* 响应：score字段代码该事件的风险值（超过100分预警）


## TODO
* 扩展黑白名单，ip，手机号，设备指纹等；
* 扩展维度信息，比如手机号地域运营商，ip地域运营商，ip出口类型，设备指纹，Referer，ua，密码hash，征信等，维度越多，可以建立规则越多，风控越精准；
* 扩展风控规则，针对需要解决的场景问题，添加特定规则，分值也应根据自身场景来调整。
* 将用户的行为轨迹综合考虑，建立复合场景的规则条件。比如：登录->活动->订单->支付，将事件关联分析综合考虑；
* **减少漏报和误报。当然，这将是个漫长的过程；**

## 献词	
我把本项目献给我的阳
