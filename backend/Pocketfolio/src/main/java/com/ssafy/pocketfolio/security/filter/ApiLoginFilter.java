package com.ssafy.pocketfolio.security.filter;

import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import com.ssafy.pocketfolio.security.dto.UserAuthDto;
import com.ssafy.pocketfolio.security.util.JWTUtil;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Log4j2
public class ApiLoginFilter extends AbstractAuthenticationProcessingFilter {

    private JWTUtil jwtUtil;

    public ApiLoginFilter(String defaultFilterProcessesUrl, JWTUtil jwtUtil) {

        super(defaultFilterProcessesUrl);
        this.jwtUtil = jwtUtil;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        log.info("-----------------ApiLoginFilter---------------------");
        log.info("attemptAuthentication");

        String email = request.getParameter("email");
        String pw = "1111"; //request.getParameter("pw");

//        if(email == null){
//            throw new BadCredentialsException("email cannot be null");
//        }
//
//        return null;
//
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(email, pw);

        return getAuthenticationManager().authenticate(authToken);
    }


    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {

        log.info("-----------------ApiLoginFilter---------------------");
        log.info("successfulAuthentication: " + authResult);

        log.info(authResult.getPrincipal());

        // Long.toString(userSeq)
        String userSeqStr = ((UserAuthDto)authResult.getPrincipal()).getUsername();

        String token = null;
        try {
            token = jwtUtil.generateAccessToken(userSeqStr);

            log.info(token);

            response.setContentType("text/plain");
            response.getOutputStream().write(token.getBytes());

//            response.addHeader("Authorization", "Bearer " + token);

//            response.addHeader("Refresh", "Bearer " + token);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
