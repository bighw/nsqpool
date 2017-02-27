> nsqpool的作用是将nsq的java客户端用连接池的形式封装起来，使客户端向通道发布信息时变得简单易用，性能更好！
> 最新的版本里面添加了监听的方法，监听可以变得更加简洁

### 使用方法
#### 1. 引入maven依赖(略)
#### 2. 它依赖这些包，你有可能需要在项目里面添加
```xml
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-pool2</artifactId>
            <version>2.0</version>
        </dependency>
        <dependency>
            <groupId>com.hyx</groupId>
            <artifactId>config</artifactId>
            <version>0.7</version>
        </dependency>
        <dependency>
            <groupId>com.maizuo.api</groupId>
            <artifactId>commons</artifactId>
            <version>0.0.5</version>
        </dependency>
```
#### 3. 在项目根目录添加配置文件`nsqpool.properties`
```
## ip
nsqpool.ip=192.168.1.204
## 端口号
nsqpool.port=4150
## 最大连接池数量
nsqpool.maxActive=50
## 是否验证֤
nsqpool.testOnBorrow=true
## 最大等待时间
nsqpool.maxWaitMillis=10000
```
#### 4. 调用
- 方式一：

```java
// 简单的一行代码即可压一条信息
NsqClientProxy.publishMessage("TOPIC", "MESSAGE");
```

- 方式二：

```java
// 获取通道
Channel channel = NsqClientProxy.getChannel();
// 构建发布信息
Publish publish = new Publish("TOPIC", "MESSAGE".getBytes());
// 发送信息
channel.write(publish);
// 回收通道资源
NsqClientProxy.returnChannel(channel);
```

#### 5. 监听的使用
- 1. 需要继承类NsqMessageThread(必须)

```java
package com.hyx.nsqdemo;

import com.hyx.monitor.NsqMessageThread;

public class MessageTest extends NsqMessageThread {
    @Override
    public void handlerMessage(String message) {
        // message 为从监听的通道中获取的信息
        System.out.println("监听测试---》"+message);
    }
}

```

- 2. 启动(必须)

```java
public static void main(String[] args) {
    NsqMonitorController.getInstance().setNsqHost("192.168.1.204").setNsqPort(4150).setNsqTopic("THIS_GAVINTEST")
            .setNsqChannel("gavinTestChannel_Second").setNsqMessageThread(new MessageTest()).start();
}
```
