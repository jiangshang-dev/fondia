package org.jeecg.modules.listener;

import com.rabbitmq.client.Channel;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.jeecg.common.annotation.RabbitComponent;
import org.jeecg.common.constant.RabbitConstant;
import org.jeecg.modules.service.IRecipeSearchService;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

@Slf4j
@RabbitComponent(value = "recipeSearchListener")
public class RabbitMqListener {

    @Resource
    private IRecipeSearchService recipeSearchService;

    @RabbitHandler
    @RabbitListener(queues = RabbitConstant.SAVE_RECIPE)
    public void onMessage(@Payload String message,
                          Channel channel,
                          @Header(AmqpHeaders.DELIVERY_TAG) long deliveryTag) {
        try {
            log.info("[RecipeSearch] 收到同步消息: {}", message);
            recipeSearchService.syncFromMessage(message);
            channel.basicAck(deliveryTag, false);
        } catch (Exception e) {
            log.error("[RecipeSearch] 处理同步消息失败", e);
            try {
                channel.basicNack(deliveryTag, false, false);
            } catch (Exception ex) {
                log.error("[RecipeSearch] nack failed", ex);
            }
        }
    }
}
