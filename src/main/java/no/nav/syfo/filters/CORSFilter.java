package no.nav.syfo.filters;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import static java.lang.System.getProperty;

public class CORSFilter implements Filter {

    private List<String> whitelistedUrls = Arrays.asList(getProperty("WHITELIST_URL").split(","));

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        if (!requestUriErMotSystemEllerInternalEndepunkt(httpRequest.getRequestURI())) {
            String origin = httpRequest.getHeader("Origin");
            if (originErWhitelisted(origin, httpRequest.getRequestURI())) {
                httpResponse.setHeader("Access-Control-Allow-Origin", origin);
                httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, NAV_CSRF_PROTECTION");
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS");
            }
        }

        filterChain.doFilter(servletRequest, httpResponse);
    }

    @Override
    public void destroy() {}


    private boolean originErWhitelisted(String origin, String uri) {
        return whitelistedUrls.contains(origin);

    }

    private boolean requestUriErMotSystemEllerInternalEndepunkt(String requestUrl) {
        return requestUrl.contains("/api/system") || requestUrl.contains("/internal");
    }
}
