package com.expiredminotaur.bcukbot.web.security;

import com.vaadin.flow.server.HandlerHelper;
import com.vaadin.flow.shared.ApplicationConstants;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.RequestEntity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.endpoint.DefaultAuthorizationCodeTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AccessTokenResponseClient;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequest;
import org.springframework.security.oauth2.client.endpoint.OAuth2AuthorizationCodeGrantRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequestEntityConverter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Objects;
import java.util.stream.Stream;

import static com.expiredminotaur.bcukbot.web.security.OAuth2UserAgentUtils.withUserAgent;

@EnableWebSecurity
@Configuration
public class SecurityConfiguration
{
    private static final String LOGIN_URL = "/login";
    private static final String LOGIN_PROCESS_IRL = "/login/process";
    private static final String LOGOUT_URL = "/logout";
    private static final String LOGOUT_SUCCESS_URL = "/";

    static boolean isFrameworkInternalRequest(HttpServletRequest request)
    {
        final String parameterValue = request
                .getParameter(ApplicationConstants.REQUEST_TYPE_PARAMETER);
        return parameterValue != null
                && Stream.of(HandlerHelper.RequestType.values()).anyMatch(
                r -> r.getIdentifier().equals(parameterValue));
    }

    @Bean
    protected SecurityFilterChain configure(HttpSecurity http) throws Exception
    {
        http
                .authorizeRequests().requestMatchers(SecurityConfiguration::isFrameworkInternalRequest).permitAll()
                .and().csrf().disable()
                .logout().logoutUrl(LOGOUT_URL).logoutSuccessUrl(LOGOUT_SUCCESS_URL)
                .and().oauth2Login()
                .tokenEndpoint().accessTokenResponseClient(accessTokenResponseClient())
                .and().userInfoEndpoint().userService(userService())
                .and().loginPage(LOGIN_URL).defaultSuccessUrl(LOGIN_PROCESS_IRL, true).permitAll();

        return http.build();
    }

    @Bean
    public OAuth2AccessTokenResponseClient<OAuth2AuthorizationCodeGrantRequest> accessTokenResponseClient()
    {
        DefaultAuthorizationCodeTokenResponseClient client = new DefaultAuthorizationCodeTokenResponseClient();

        client.setRequestEntityConverter(new OAuth2AuthorizationCodeGrantRequestEntityConverter()
        {
            @Override
            public RequestEntity<?> convert(OAuth2AuthorizationCodeGrantRequest oauth2Request)
            {
                return withUserAgent(Objects.requireNonNull(super.convert(oauth2Request)));
            }
        });

        return client;
    }

    @Bean
    public OAuth2UserService<OAuth2UserRequest, OAuth2User> userService()
    {
        DefaultOAuth2UserService service = new DefaultOAuth2UserService();

        service.setRequestEntityConverter(new OAuth2UserRequestEntityConverter()
        {
            @Override
            public RequestEntity<?> convert(OAuth2UserRequest userRequest)
            {
                return withUserAgent(Objects.requireNonNull(super.convert(userRequest)));
            }
        });

        return service;
    }
}
