package server.mainproject.auth.filter;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.shaded.json.JSONObject;
import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.support.incrementer.HsqlMaxValueIncrementer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import server.mainproject.auth.dto.LoginDto;
import server.mainproject.auth.jwt.JwtTokenizer;
import server.mainproject.member.entity.Member;


import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class JwtAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenizer jwtTokenizer;

    public JwtAuthenticationFilter(AuthenticationManager authenticationManager, JwtTokenizer jwtTokenizer) {
        this.authenticationManager = authenticationManager;
        this.jwtTokenizer = jwtTokenizer;
    }

    @SneakyThrows
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginDto loginDto = objectMapper.readValue(request.getInputStream(), LoginDto.class);

        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(loginDto.getEmail(), loginDto.getPassword());

        return authenticationManager.authenticate(authenticationToken);


    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws ServletException, IOException {


        Member member = (Member) authResult.getPrincipal();
        String accessToken = delegateAccessToken(member);
        String refreshToken = delegateRefreshToken(member);

        Long memberId = member.getMemberId();
        String userName = member.getUserName();

        // Todo : response에 memberId 및 userName 추가! (login)

        String Authorization = "Bearer_" + accessToken;
        String refresh = "Bearer_" + refreshToken;

        Cookie cookie1 = new Cookie("Authorization", Authorization);
        Cookie cookie2 = new Cookie("Refresh", refresh);
        Cookie cookie3 = new Cookie("memberId", String.valueOf(memberId));
//        Cookie cookie4 = new Cookie("userName", userName);

        cookie1.setHttpOnly(true);
        cookie2.setHttpOnly(true);
        cookie3.setHttpOnly(true);
//        cookie4.setHttpOnly(true);

        cookie1.setPath("/");
        cookie2.setPath("/");
        cookie3.setPath("/");
//        cookie4.setPath("/");

//        cookie1.setMaxAge(3600);
//        cookie2.setMaxAge(3600);
//        cookie3.setMaxAge(3600);
//        cookie4.setMaxAge(3600);

//        cookie1.setDomain("localhost");
        cookie1.setDomain("http://mainmay.s3-website.ap-northeast-2.amazonaws.com");
//        cookie2.setDomain("localhost");
        cookie2.setDomain("http://mainmay.s3-website.ap-northeast-2.amazonaws.com");
//        cookie3.setDomain("localhost");
        cookie3.setDomain("http://mainmay.s3-website.ap-northeast-2.amazonaws.com");

//
        response.addCookie(cookie1);
        response.addCookie(cookie2);
        response.addCookie(cookie3);
//        response.addCookie(cookie4);

        JSONObject jsonObject = new JSONObject();
        jsonObject.put("memberId", memberId);
        jsonObject.put("userName", userName);
        jsonObject.put("accessToken", Authorization);
        jsonObject.put("refreshToken", refresh);


        // 응답 헤더 설정
        response.setContentType("application/json");

        // 응답 데이터 전송
        response.getWriter().write(jsonObject.toString());

//        response.getWriter().write("{\"memberId\": " + memberId + "}");
//        response.getWriter().write("{\"userName\": " + userName + "}");

//        response.setHeader("Authorization", "Bearer_" + accessToken);
//        response.setHeader("Refresh", "Bearer_" + refreshToken);
//        response.setHeader("access-token-expiration-minutes", String.valueOf(jwtTokenizer.getAccessTokenExpirationMinutes()));

        this.getSuccessHandler().onAuthenticationSuccess(request, response, authResult);  // 추가
    }

    public String delegateAccessToken(Member member) {
        Map<String, Object> claims = new HashMap<>();

        claims.put("memberId", member.getMemberId());  // 식별자도 포함할 수 있다.
        claims.put("userName", member.getUserName());
        claims.put("email", member.getEmail());
        claims.put("roles", member.getRoles());

        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getAccessTokenExpirationMinutes());

        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String accessToken = jwtTokenizer.generateAccessToken(claims, subject, expiration, base64EncodedSecretKey);

        return accessToken;
    }

    public String delegateRefreshToken(Member member) {
        String subject = member.getEmail();
        Date expiration = jwtTokenizer.getTokenExpiration(jwtTokenizer.getRefreshTokenExpirationMinutes());
        String base64EncodedSecretKey = jwtTokenizer.encodeBase64SecretKey(jwtTokenizer.getSecretKey());

        String refreshToken = jwtTokenizer.generateRefreshToken(subject, expiration, base64EncodedSecretKey);

        return refreshToken;
    }


}
