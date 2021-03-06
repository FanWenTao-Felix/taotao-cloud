package com.taotao.cloud.gateway.handler;

import com.taotao.cloud.common.constant.CommonConstant;
import com.taotao.cloud.common.model.Result;
import com.taotao.cloud.common.utils.CaptchaUtil;
import com.taotao.cloud.common.utils.ResponseUtil;
import com.taotao.cloud.redis.template.RedisRepository;
import com.wf.captcha.ArithmeticCaptcha;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.HandlerFunction;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

/**
 * 生成图形验证码处理器
 *
 * @author dengtao
 * @date 2020/4/29 22:11
 */
@Slf4j
@Component
public class ImageCodeHandler implements HandlerFunction<ServerResponse> {

    @Autowired
    private RedisRepository redisRepository;

    @Override
    public Mono<ServerResponse> handle(ServerRequest request) {
        ArithmeticCaptcha captcha = CaptchaUtil.getArithmeticCaptcha();
        String text = captcha.text();
        try {
            MultiValueMap<String, String> params = request.queryParams();
            String t = params.getFirst("t");
            redisRepository.setExpire(CommonConstant.TAOTAO_CAPTCHA_KEY + t, text.toLowerCase(), 120);
        } catch (Exception e) {
            return ServerResponse
                    .status(HttpStatus.OK.value())
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(BodyInserters.fromValue(Result.failed("服务异常,请稍后重试")));
        }

        return ServerResponse
                .status(HttpStatus.OK)
                .bodyValue(Result.succeed(captcha.toBase64()));
    }
}
