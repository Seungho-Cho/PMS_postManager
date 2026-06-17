package com.mason.api.auth;

import java.util.HashMap;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

@Service
public class DiscordOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();
    private final DiscordGuildProperties guildProperties;
    private final RestClient restClient = RestClient.create();

    public DiscordOAuth2UserService(DiscordGuildProperties guildProperties) {
        this.guildProperties = guildProperties;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) {
        OAuth2User user = delegate.loadUser(userRequest);

        String guildNickname = getGuildMemberId(userRequest.getAccessToken().getTokenValue());
        if (guildNickname == null) {
            throw new OAuth2AuthenticationException(
                 new OAuth2Error("not_guild_member", "해당 Discord 서버 멤버만 로그인할 수 있습니다.", null)
            );
        }

        Map<String, Object> attributes = new HashMap<>(user.getAttributes());
        attributes.put("guildNickname", guildNickname);

        return new DefaultOAuth2User(user.getAuthorities(), attributes, "id");
    }

    private String getGuildMemberId(String accessToken) {
        Map<String, Object> member;
        try {
            member = restClient.get()
                .uri("https://discord.com/api/users/@me/guilds/{guildId}/member", guildProperties.guildId())
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .body(new ParameterizedTypeReference<Map<String, Object>>() {});
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }

        if (member == null) {
            return null;
        }

        String nick = (String) member.get("nick");
        if (nick != null) {
            return nick;
        }

        Map<String, Object> memberUser = (Map<String, Object>) member.get("user");
        return memberUser != null ? (String) memberUser.get("username") : null;
    }
}
