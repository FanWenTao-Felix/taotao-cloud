package com.taotao.cloud.auth.authentication.social.github.config;

import com.taotao.cloud.auth.authentication.properties.GithubProperties;
import com.taotao.cloud.auth.authentication.properties.SocialOauth2Properties;
import com.taotao.cloud.auth.authentication.social.github.connect.GitHubConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.social.config.annotation.ConnectionFactoryConfigurer;
import org.springframework.social.config.annotation.EnableSocial;
import org.springframework.social.config.annotation.SocialConfigurerAdapter;
import org.springframework.social.connect.ConnectionFactory;
import org.springframework.social.connect.ConnectionFactoryLocator;
import org.springframework.social.connect.UsersConnectionRepository;

/**
 * github 社交登录的自动配置
 *
 * @author dengtao
 * @date 2020/4/29 20:59
 */
@Configuration
@EnableSocial
@ConditionalOnProperty(prefix = "taotao.cloud.social.security.github", name = "app-id")
public class GithubAutoAuthConfig extends SocialConfigurerAdapter {

    @Autowired
    private SocialOauth2Properties socialOauth2Properties;

    @Override
    public void addConnectionFactories(ConnectionFactoryConfigurer configurer, Environment environment) {
        configurer.addConnectionFactory(createConnectionFactory());
    }

    public ConnectionFactory<?> createConnectionFactory() {
        GithubProperties github = socialOauth2Properties.getSecurity().getGithub();
        return new GitHubConnectionFactory(github.getProviderId(), github.getAppId(), github.getAppSecret());
    }

    @Override
    public UsersConnectionRepository getUsersConnectionRepository(ConnectionFactoryLocator connectionFactoryLocator) {
        return null;
    }

}
