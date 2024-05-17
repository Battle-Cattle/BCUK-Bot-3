package com.expiredminotaur.bcukbot.web.security;

import com.expiredminotaur.bcukbot.Role;
import com.expiredminotaur.bcukbot.sql.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.stereotype.Service;

@Service
public class UserTools
{
    @Autowired
    private OAuth2AuthorizedClientService clientService;

    @Autowired
    private UserRepository users;

    private final Logger logger = LoggerFactory.getLogger(UserTools.class);

    public String getCurrentUsersName()
    {
        return getPrincipalAttribute("username");
    }

    public long getCurrentUsersID()
    {
        String id = getPrincipalAttribute("id");
        try
        {
            return Long.parseLong(id);
        } catch (NumberFormatException e)
        {
            logger.warn("Failed to parse user ID: {}", id);
            return -1;
        }
    }

    public String getCurrentUsersToken()
    {
        OAuth2AuthenticationToken oauthToken = getAuthentication();
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(oauthToken.getAuthorizedClientRegistrationId(), oauthToken.getName());
        return client.getAccessToken().getTokenValue();
    }

    public OAuth2AuthenticationToken getAuthentication()
    {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth instanceof OAuth2AuthenticationToken)
        {
            return (OAuth2AuthenticationToken) auth;
        } else
        {
            return null;
        }
    }

    private String getPrincipalAttribute(String key)
    {
        String error = "ERROR please report to the Admin";
        OAuth2AuthenticationToken auth = getAuthentication();
        if (auth != null)
        {
            Object principal = auth.getPrincipal();
            if (principal instanceof DefaultOAuth2User)
            {
                return (String) ((DefaultOAuth2User) principal).getAttributes().get(key);
            }
        }
        return error;
    }

    public boolean hasAccess(Role accessLevel)
    {
        return users.findById(getCurrentUsersID()).map(u -> u.getAccessLevel().getValue() >= accessLevel.getValue()).orElse(false);
    }
}
