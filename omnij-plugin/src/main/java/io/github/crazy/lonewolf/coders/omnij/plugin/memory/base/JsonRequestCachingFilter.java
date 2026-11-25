package io.github.crazy.lonewolf.coders.omnij.plugin.memory.base;

import io.github.crazy.lonewolf.coders.omnij.plugin.memory.config.OmniJConfig;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;

import java.io.IOException;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class JsonRequestCachingFilter implements Filter{
    @Autowired
    private OmniJConfig config;
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException{

        HttpServletRequest httpRequest = (HttpServletRequest) request;


        if (!(httpRequest instanceof ContentCachingRequestWrapper)) {
            httpRequest = new ContentCachingRequestWrapper(httpRequest, 0);
        }

        filterChain.doFilter(httpRequest, response);
    }
}
