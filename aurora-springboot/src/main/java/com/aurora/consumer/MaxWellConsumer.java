package com.aurora.consumer;

import com.alibaba.fastjson.JSON;
import com.aurora.model.dto.ArticleSearchDTO;
import com.aurora.model.dto.MaxwellDataDTO;
import com.aurora.entity.Article;
import com.aurora.mapper.ElasticsearchMapper;
import com.aurora.util.BeanCopyUtil;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import static com.aurora.constant.RabbitMQConstant.MAXWELL_QUEUE;

@Component
@RabbitListener(queues = MAXWELL_QUEUE)
public class MaxWellConsumer {

    @Autowired
    private ElasticsearchMapper elasticsearchMapper;

    @RabbitHandler
    public void process(byte[] data) {
        MaxwellDataDTO maxwellDataDTO = JSON.parseObject(new String(data), MaxwellDataDTO.class);
        // 转化成文章内容
        Article article = JSON.parseObject(JSON.toJSONString(maxwellDataDTO.getData()), Article.class);
        // 根据 type 字段的值进行不同的操作
        switch (maxwellDataDTO.getType()) {
            // "insert" 或 "update"，则将 article 对象转换为 ArticleSearchDTO 对象，并使用 elasticsearchMapper 将其保存到 Elasticsearch 中。
            case "insert":
            case "update":
                elasticsearchMapper.save(BeanCopyUtil.copyObject(article, ArticleSearchDTO.class));
                break;
            // 如果 type 是 "delete"，则根据 article 对象的 id 使用 elasticsearchMapper 删除相应的记录
            case "delete":
                elasticsearchMapper.deleteById(article.getId());
                break;
            // 如果 type 不匹配以上条件，则不执行任何操作
            default:
                break;
        }
    }
}