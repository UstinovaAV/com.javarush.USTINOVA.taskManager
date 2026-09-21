package com.javarush.ustinova.taskManager.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;


    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // Достаем заголовок Authorization
        final String authHeader = request.getHeader("Authorization");
        final String requestUri = request.getRequestURI();

        //  Если заголовка нет или он не начинается с Bearer , пропускаем запрос дальше
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.debug("Запрос без Bearer токена, обрабатываем как анонимный: {}", requestUri);
            filterChain.doFilter(request, response);
            return;
        }

        // Отрезаем Bearer  и оставляем сам токен
        final String jwt = authHeader.substring(7);

        try {
            // Достаем данные прямо из токена (БД НЕ трогаем!)
            final String username = jwtService.extractUsername(jwt);
            final List<String> roles = jwtService.extractRoles(jwt);
            final Long userId = jwtService.extractUserId(jwt);

            // Если пользователь еще не аутентифицирован в этом запросе
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Преобразуем строки ролей в GrantedAuthority
                List<SimpleGrantedAuthority> authorities = roles.stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

                // Создаем наш CustomUserDetails прямо из данных токена (пароль тут не нужен)
                UserDetails userDetails = new CustomUserDetails(userId, username, "", authorities); // <-- ИЗМЕНЕНО

                // Создаем объект аутентификации и кладем его в SecurityContext
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null, // credentials не нужны, токен уже проверен подписью
                        userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);

                log.debug("Пользователь '{}' (ID={}) успешно аутентифицирован для запроса {}", username, userId, requestUri);
            }
        } catch (Exception e) {
            // Если токен протух или подделан, просто не аутентифицируем пользователя.
            // Дальше сработает правило .anyRequest().authenticated() и вернет 401.
            log.error("КРИТИЧЕСКАЯ ОШИБКА обработки JWT для запроса {}. Причина: {}", requestUri, e.getMessage(), e);
        }

        // Передаем управление следующему фильтру в цепочке
        filterChain.doFilter(request, response);
    }
}