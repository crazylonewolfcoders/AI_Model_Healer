package io.github.crazy.lonewolf.coders.omnij.plugin.servlet;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;
import java.io.IOException;

@Component
public class ContentCachingFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        if (request instanceof HttpServletRequest) {
            // Swap the raw request with our re-readable wrapper
            CachedBodyHttpServletRequest wrappedRequest = new CachedBodyHttpServletRequest((HttpServletRequest) request);
            chain.doFilter(wrappedRequest, response);
        } else {
            chain.doFilter(request, response);
        }
    }
}
