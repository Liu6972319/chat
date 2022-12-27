package com.east.chat.config;

import com.east.chat.common.RabbitCommon;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {

    // 创建队列
    @Bean
    public Queue createFanoutQueue() {
        return new Queue(RabbitCommon.JOIN_QUEUE);
    }
//    // 创建交换机
//    @Bean
//    public FanoutExchange defFanoutExchange() {
//        return new FanoutExchange(TEST_FANOUT_EXCHANGE);
//    }
//    // 队列与交换机进行绑定
//    @Bean
//    Binding bindingFanout() {
//        return BindingBuilder.bind(createFanoutQueue()).to(defFanoutExchange());
//    }

//    @Bean
//    DirectExchange directExchange() {
//        return new DirectExchange(TEST_DIRECT_EXCHANGE);
//    }
    //队列与交换机绑定并添加路由key（direct和topic模式）
//    @Bean
//    Binding bindingDirect() {
//        return BindingBuilder.bind(createDirectQueue()).to(directExchange()).with(DIRECT_ROUTINGKEY);
//    }
}
