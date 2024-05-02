package com.moli.redis;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 项目主要用来实现 Redis能做的事情：
 *
 *  1. 限流操作，步骤如下；
 *  	1.1 定义一个注解 @AccessLimit - 定义请求频率。
 *  	1.2 定义一个 AccessLimitInterceptor，在该拦截器中进行aop操作和具体的限流操作。
 *  	1.3 具体限流：使用 Redis + Lua 脚本实现
 *
 *  2. 实现延时队列，步骤如下：
 *  	2.1 使用 zset 命令实现 见 -> queue.RedisDelayQueue
 *  		2.1.1 客户端根据当前的（系统时间 + delay 作为 score）的时间发送请求向redis中对应key - zSet 中添加延迟消息
 *  		2.1.2 服务端创建一个线程根据当前的 系统时间，去redis服务器中轮询相应的数据
 *  		2.1.3 如果找到了，就可以使用lua脚本删除该消息，并进行相关业务
 *  		2.1.4 如果没有找到， 线程睡眠一定时间，继续轮询
 *  	2.2 使用 Redission 实现 见 queue.RedissonDelayQueue
 *  		2.2.1 客户端可以根据 redisDelayQueueUtil.addDelayQueue() 方法向对应的延迟队列中发送消息
 *  		2.2.2 服务端创建一个线程，通过	redisDelayQueueUtil.getDelayQueue() 方法获取延迟队列中的消息
 *  		2.2.3 如果延迟队列中没有消息，则会返回为null
 */

@SpringBootApplication
public class RedisOpersApplication {

	public static void main(String[] args) {
		SpringApplication.run(RedisOpersApplication.class, args);
	}
}
