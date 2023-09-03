package com.aurora.consumer;


import com.alibaba.fastjson.JSON;
import com.aurora.entity.Article;
import com.aurora.entity.UserInfo;
import com.aurora.model.dto.EmailDTO;
import com.aurora.service.ArticleService;
import com.aurora.service.UserInfoService;
import com.aurora.util.EmailUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.aurora.constant.CommonConstant.TRUE;
import static com.aurora.constant.RabbitMQConstant.SUBSCRIBE_QUEUE;

/**
 * 订阅消费者
 */
@Component
@RabbitListener(queues = SUBSCRIBE_QUEUE)
public class SubscribeConsumer {

    @Value("${website.url}")
    private String websiteUrl;

    @Autowired
    private ArticleService articleService;

    @Autowired
    private UserInfoService userInfoService;

    @Autowired
    private EmailUtil emailUtil;

    @RabbitHandler
    public void process(byte[] data) {
        // 将收到的数据转化成文章id
        Integer articleId = JSON.parseObject(new String(data), Integer.class);
        // 通过文章id查询对应的文章信息
        Article article = articleService.getOne(new LambdaQueryWrapper<Article>().eq(Article::getId, articleId));
        // 查询订阅的用户
        List<UserInfo> users = userInfoService.list(new LambdaQueryWrapper<UserInfo>()
                .eq(UserInfo::getIsSubscribe, TRUE));
        // 获取订阅的用户邮件
        List<String> emails = users.stream().map(UserInfo::getEmail).collect(Collectors.toList());

        // 对订阅的用户信息推送消息
        for (String email : emails) {
            EmailDTO emailDTO = new EmailDTO();
            Map<String, Object> map = new HashMap<>();
            emailDTO.setEmail(email);
            emailDTO.setSubject("文章订阅");
            emailDTO.setTemplate("common.html");
            String url = websiteUrl + "/articles/" + articleId;
            if (article.getUpdateTime() == null) {
                map.put("content", "布凡君的JARVIS发布了新的文章，"
                        + "<a style=\"text-decoration:none;color:#12addb\" href=\"" + url + "\">点击查看</a>");
            } else {
                map.put("content", "布凡君的JARVIS对《" + article.getArticleTitle() + "》进行了更新，"
                        + "<a style=\"text-decoration:none;color:#12addb\" href=\"" + url + "\">点击查看</a>");
            }
            emailDTO.setCommentMap(map);
            emailUtil.sendHtmlMail(emailDTO);
        }
    }

}
