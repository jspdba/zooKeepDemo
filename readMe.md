#zookeeper介绍
    Zookeeper是一个高性能，分布式的，开源分布式应用协调服务。它提供了简单原始的功能，分布式应用可以基于它实现更高级的服（如Dubbo基于Zookeeper），比如，配置管理，集群管理，名空间。它被设计为易于编程，使用文件系统目录树作为数据模型。服在端跑在Java上，提供java和C的客户端API。

#zookeeper 参考
[zookeeper 学习](http://blog.csdn.net/zuoanyinxiang/article/details/50937892)
##zoodemo-client
##zoodemo-server
    数据的发布与订阅模式
##zoodemo-queue
    分布式消息队列使用
##zoodemo-lock
    分布式锁(缺少代码)
##zoodemo-name
    zookeeper 命名服务
## zkCli命令
[zk 常用命令](http://www.cnblogs.com/likehua/p/3999588.html)
    zkClient -server localhost:2181
    ls /
    get /
