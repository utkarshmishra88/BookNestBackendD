package com.booknest.auth.resource;

import com.booknest.auth.entity.User;
import com.booknest.auth.repository.UserRepository;
import com.booknest.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Map;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OAuth2ControllerTest {

    private MockMvc mockMvc;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private OAuth2Controller oAuth2Controller;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(oAuth2Controller).build();
    }

    @Test
    void testOAuth2Success_ExistingUser() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "oauth@ex.com", "name", "OAuth User"));
        
        mockMvc = MockMvcBuilders.standaloneSetup(oAuth2Controller)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(OAuth2User.class);
                    }
                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        return oAuth2User;
                    }
                })
                .build();

        User user = User.builder().userId(1).email("oauth@ex.com").build();
        when(userRepository.findByEmailIgnoreCase("oauth@ex.com")).thenReturn(Optional.of(user));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("oauthToken");

        mockMvc.perform(get("/oauth2/success"))
                .andExpect(status().isFound())
                .andExpect(redirectedUrlPattern("**/oauth/callback?token=oauthToken"));
    }

    @Test
    void testOAuth2Success_NewUser() throws Exception {
        OAuth2User oAuth2User = mock(OAuth2User.class);
        when(oAuth2User.getAttributes()).thenReturn(Map.of("email", "new@ex.com", "name", "New User"));
        
        mockMvc = MockMvcBuilders.standaloneSetup(oAuth2Controller)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(OAuth2User.class);
                    }
                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        return oAuth2User;
                    }
                })
                .build();

        when(userRepository.findByEmailIgnoreCase("new@ex.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));
        when(jwtUtil.generateToken(any(User.class))).thenReturn("newToken");

        mockMvc.perform(get("/oauth2/success"))
                .andExpect(status().isFound());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testOAuth2Success_NullPrincipal() throws Exception {
        mockMvc = MockMvcBuilders.standaloneSetup(oAuth2Controller)
                .setCustomArgumentResolvers(new org.springframework.web.method.support.HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(org.springframework.core.MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(OAuth2User.class);
                    }
                    @Override
                    public Object resolveArgument(org.springframework.core.MethodParameter parameter, org.springframework.web.method.support.ModelAndViewContainer mavContainer, org.springframework.web.context.request.NativeWebRequest webRequest, org.springframework.web.bind.support.WebDataBinderFactory binderFactory) {
                        return null;
                    }
                })
                .build();
        mockMvc.perform(get("/oauth2/success"))
                .andExpect(status().isUnauthorized());
    }
}
