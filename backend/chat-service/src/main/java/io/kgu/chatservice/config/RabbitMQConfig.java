package io.kgu.chatservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.FanoutExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableRabbit
@Configuration
public class RabbitMQConfig {

    @Bean
    public Queue queue() {
        return new Queue("match.queue", false, false, false);
    }

    @Bean
    public FanoutExchange fanoutExchange() {
        return new FanoutExchange("match.direct.exchange", false, false);
    }

    @Bean
    public Binding binding() {
        return BindingBuilder
                .bind(queue())
                .to(fanoutExchange());
    }

}
