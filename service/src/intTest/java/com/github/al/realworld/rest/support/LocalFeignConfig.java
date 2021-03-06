/*
 * MIT License
 *
 * Copyright (c) 2020 - present Alexey Lapin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.al.realworld.rest.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.al.realworld.rest.auth.AuthSupport;
import feign.Client;
import feign.Feign;
import feign.RequestInterceptor;
import feign.Target;
import feign.codec.Decoder;
import feign.codec.Encoder;
import feign.jackson.JacksonDecoder;
import feign.jackson.JacksonEncoder;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Collections;

@RequiredArgsConstructor
@Configuration
public class LocalFeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor(Environment env) {
        return template -> {
            String token = AuthSupport.TokenHolder.token;
            if (token != null) {
                template.header(HttpHeaders.AUTHORIZATION, "Token " + token);
            }
            if (template.headers().get(HttpHeaders.CONTENT_TYPE) == null) {
                template.header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
            }
        };
    }

    @Bean
    public Encoder encoder() {
        ObjectMapper mapper = new ObjectMapper().enable(SerializationFeature.WRAP_ROOT_VALUE);
        return new JacksonEncoder(mapper);
    }

    @Bean
    public Decoder decoder() {
        return new JacksonDecoder(Collections.singletonList(new JavaTimeModule()));
    }

    @Bean
    public Client client() {
        return new Client.Default(null, null);
    }

    @Bean
    public Feign.Builder feignBuilder(Environment env) {
        return new Feign.Builder() {
            @Override
            public <T> T target(Target<T> target) {
                String port = System.getProperty("server.port");
                String name = target.name();
                String url = target.url().replace(name, "localhost:" + port);
                Target.HardCodedTarget<T> local = new Target.HardCodedTarget<>(target.type(), target.name(), url);
                return super.target(local);
            }
        };
    }

}
