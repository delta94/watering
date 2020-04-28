package com.coderteam.watering.security.config;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Date;
import java.util.List;

import javax.servlet.FilterChain;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.coderteam.watering.secutiry.config.JwtAuthentication;
import com.coderteam.watering.secutiry.config.JwtAuthenticationFilter;
import com.coderteam.watering.secutiry.service.JwtService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
public class JwtAuthenticationFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    private JwtAuthenticationFilter jwtAuthFilter;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private FilterChain filterChain;
    private JwtService service;
    private AuthenticationManager manager;

    @BeforeEach
    public void setUp() {
        // Create mock object
        manager = mock(AuthenticationManager.class);
        service = mock(JwtService.class);

        // Request, response and filter chain
        request = mock(HttpServletRequest.class);
        response = mock(HttpServletResponse.class);
        filterChain = mock(FilterChain.class);

        // Create test object
        jwtAuthFilter = new JwtAuthenticationFilter(manager, service);
    }

    @Test
    protected void testIfJwtServiceWasCalled() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("jwt 1234");
        jwtAuthFilter.doFilter(request, response, filterChain);
        verify(service).verifyToken("1234");
    }

    @Test
    protected void testIfJwtServiceWasnotCalled() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("jwt ");
        jwtAuthFilter.doFilter(request, response, filterChain);
        verify(service, never()).verifyToken("1234");
    }

    @Test
    protected void testIfAuthManagerWasSuccess() throws Exception {
        // Create mock object
        DecodedJWT decodedJwt = mock(DecodedJWT.class);
        Claim claim = mock(Claim.class);

        when(request.getHeader("Authorization")).thenReturn("jwt 1234");
        when(service.verifyToken("1234")).thenReturn(decodedJwt);
        when(decodedJwt.getSubject()).thenReturn("anhvan");
        when(decodedJwt.getClaim("authorities")).thenReturn(claim);
        when(claim.asList(String.class)).thenReturn(List.of("ROLE_USER"));

        jwtAuthFilter.doFilter(request, response, filterChain);
        verify(service).verifyToken("1234");
        verify(manager).authenticate(any(JwtAuthentication.class));
    }

    @Test
    public void testInfoController() throws Exception {
        mockMvc.perform(get("/info")).andExpect(status().isOk()).andExpect(content().string("Hello world"));
    }

    @Test
    public void testUnauthorizedUser() throws Exception {
        mockMvc.perform(get("/info/user")).andExpect(status().is4xxClientError());
    }

    @Test
    public void testAuthorizedUser() throws Exception {
        List<GrantedAuthority> authorityList = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtService.generateToken("danganhvan", authorityList, JwtService.JwtType.TOKEN, new Date());
        mockMvc.perform(get("/info/user").header("Authorization", "jwt " + token)).andDo(print())
                .andExpect(status().isOk()).andExpect(content().string("danganhvan"));
    }

    @Test
    public void testAuthenticationFailed() throws Exception {
        List<GrantedAuthority> authorityList = List.of(new SimpleGrantedAuthority("ROLE_USER"));
        String token = jwtService.generateToken("danganhvan", authorityList, JwtService.JwtType.TOKEN, new Date());
        mockMvc.perform(get("/info/user").header("Authorization", "jwt d" + token)).andDo(print())
                .andExpect(status().is4xxClientError());
    }

    @Test
    public void testForbbiden() throws Exception {
        List<GrantedAuthority> authorityList = List.of(new SimpleGrantedAuthority("ROLE_ADMIN"));
        String token = jwtService.generateToken("danganhvan", authorityList, JwtService.JwtType.TOKEN, new Date());
        mockMvc.perform(get("/info/user").header("Authorization", "jwt " + token)).andDo(print())
                .andExpect(status().isForbidden());
    }

}