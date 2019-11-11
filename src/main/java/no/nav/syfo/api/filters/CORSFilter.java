package no.nav.syfo.api.filters;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Order(Ordered.HIGHEST_PRECEDENCE)
@Component
public class CORSFilter implements Filter {

    private List<String> whitelist = Arrays.asList(
            "https://syfooversikt.nais.adeo.no",
            "https://syfooversikt.nais.preprod.local",
            "https://syfooversikt-q1.nais.preprod.local",
            "https://syfomodiaperson.nais.adeo.no",
            "https://syfomodiaperson.nais.preprod.local",
            "https://modiasyfofront.nais.adeo.no",
            "https://modiasyfofront.nais.preprod.local",
            "https://modiasyfofront-q1.nais.preprod.local",
            "https://fastlegefront.nais.adeo.no",
            "https://fastlegefront.nais.preprod.local",
            "https://finnfastlege.nais.adeo.no",
            "https://finnfastlege.nais.preprod.local",
            "https://fastlegefront-q1.nais.preprod.local",
            "https://app.adeo.no",
            "https://app-q1.adeo.no"
    );

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {}

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpResponse = (HttpServletResponse) servletResponse;
        HttpServletRequest httpRequest = (HttpServletRequest) servletRequest;

        String reqUri = httpRequest.getRequestURI();
        if (requestUriErIkkeMotFeedEllerInternalEndepunkt(reqUri)) {
            String origin = httpRequest.getHeader("origin");
            if (erWhitelisted(origin)) {
                httpResponse.setHeader("Access-Control-Allow-Origin", httpRequest.getHeader("Origin"));
                httpResponse.setHeader("Access-Control-Allow-Credentials", "true");
                httpResponse.setHeader("Access-Control-Allow-Headers", "Origin, Content-Type, Accept, NAV_CSRF_PROTECTION, authorization");
                httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, PUT, DELETE, OPTIONS, HEAD");
            }
        }

        filterChain.doFilter(servletRequest, httpResponse);
    }

    @Override
    public void destroy() {}

    private boolean requestUriErIkkeMotFeedEllerInternalEndepunkt(String reqUri) {
        return !(reqUri.contains("/api/system") || reqUri.contains("/internal"));
    }

    private boolean erWhitelisted(String origin) {
        return origin != null && whitelist.contains(origin);
    }
}
