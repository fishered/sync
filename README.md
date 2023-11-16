
### Biz组件设计

### 拓展

    在biz中定义了AbstractProcessData和BizSyncLoad，如果需要实现其他类型业务数据同步至es 
    *自定义业务实现实现BizSyncLoad
    *自定义BizType 对应的业务类型
    *实现BizSyncLoad方法， loadData， count等...
    *调用时指定对应的BizType，其他的事情交给sync去做
    *需要你指定你要同步的数据，同步多少...

### 同步的数据会不会丢失
    
    同步分为全量同步和增量同步，主要是由具体业务实现获取上一次操作记录决定的。
    目前是第一次全量后，后续都为增量，增量根据update决定。
    想强制同步全量？删除对应你指定的标记就好 参考@See isWhole()

    同步收集的数据按理说是不会丢失的，因为引用了基于rocket的防消息丢失机制：
    开一个新的线程，去监听一个blockQueue 如果同步线程失败了就往fail里扔，但是这里
    有个隐患，如果大量失败 还要考虑你的消费能力 
    当然，我认为有必要将failContext保存起来，做后续的操作，这是后面优化的地方

### 为什么同步成功后数据没有全部进来

    *第一，再同步程序中，可能会有超时崩溃等异常，会交给补偿的线程池处理 它们的动作都是异步的，
    只是在主要同步的方法中做了 all 阻塞等待，所以有异常的情况可能需要等待补偿执行完成
    *第二，在elastic中，异步写入的方式 会先在缓冲池中不会立刻刷新到index doc下，所以尝试加载重新刷新index

### 在业务中要手动同步数据

    在业务中要手动同步数据，需要调用sync的同步方法，参考@See manualThrowEx

### 组件结构树
```
├─src
│  ├─main
│  │  ├─java
│  │  │  └─com
│  │  │      └─asset
│  │  │          └─sync
│  │  │              ├─biz ##同步biz的包 也sync的核心
│  │  │              │  ├─asset ##对应asset的实现包
│  │  │              │  ├─compensate ##任务补偿处理
│  │  │              │  ├─config ##配置文件
│  │  │              │  ├─context ##biz thread处理的上下文
│  │  │              │  ├─core ##核心处理
│  │  │              │  ├─exec ##定时任务的入口
│  │  │              │  ├─format ##格式化处理
│  │  │              │  ├─type ##封装的biz的类型 用于spring ioc注册实现类
│  │  │              │  └─util ##工具类
│  │  │              ├─config ##各种公共配置 elastic redis pg...
│  │  │              ├─controller ##对应控制层
│  │  │              ├─domain
│  │  │              ├─factory
│  │  │              ├─param
│  │  │              │  └─req
│  │  │              └─service ##对应业务数据的方法，一般为业务crud
│  │  │                  └─asset
│  │  │                      ├─mapper
│  │  │                      └─service
│  │  │                          └─impl
│  │  └─resources
│  └─test
│      └─java
│          └─com
│              └─asset
└─              └─sync ##测试脚本

```
### 配置说明
    打开application.yml 设置如下
    pg：（变更自行替换）
        driver-class-name: org.postgresql.Driver
        url: jdbc:postgresql://10.35.30.105:5433/asset218
        type: com.zaxxer.hikari.HikariDataSource
        password: idss@1234
        username: postgres
    
    es:
        cluster-hosts: 10.66.11.57:9200
        userName: elastic
        password: X1@dataorihost: 10.35.30.105:9200

    redis:
        host: 10.66.11.57
        port: 6379
        password: dat@ori123
        database: 0

    port:8080
    自行替换为实际配置

### 使用
    默认已经集成了asset_info 自动同步 
    同步时间为每天0点同步 全量/增量
    如需手动调用参考接口manual_data
