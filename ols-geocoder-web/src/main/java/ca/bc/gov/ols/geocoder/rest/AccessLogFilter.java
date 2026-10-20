package ca.bc.gov.ols.geocoder.rest;

import java.io.IOException;
import java.time.Instant;

import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/** Writes one-line access log entries to STDOUT. */
@Component
public class AccessLogFilter extends OncePerRequestFilter {
	
	public static final String ERROR_MESSAGE_ATTR = "ols.accessLog.errorMessage";
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
			FilterChain chain) throws ServletException, IOException {
		long start = System.nanoTime();
		try {
			chain.doFilter(request, response);
		} catch (ServletException | IOException | RuntimeException e) {
			request.setAttribute(ERROR_MESSAGE_ATTR, e.toString());
			log(request, 500, start);
			throw e;
		}
		log(request, response.getStatus(), start);
	}
	
	private void log(HttpServletRequest request, int status, long start) {
		long ms = (System.nanoTime() - start) / 1_000_000;
		StringBuilder sb = new StringBuilder(256);
		sb.append("ACCESS ").append(Instant.now())
				.append(" client=").append(clean(clientIp(request)))
				.append(" method=").append(request.getMethod())
				.append(" path=").append(clean(request.getRequestURI()))
				.append(" params=\"").append(clean(request.getQueryString())).append('"')
				.append(" status=").append(status)
				.append(" time_ms=").append(ms);
		Object error = request.getAttribute(ERROR_MESSAGE_ATTR);
		if(error != null) {
			sb.append(" error=\"").append(clean(error.toString())).append('"');
		}
		System.out.println(sb);
	}
	
	private static String clientIp(HttpServletRequest request) {
		String xff = request.getHeader("X-Forwarded-For");
		if(xff != null && !xff.isBlank()) {
			return xff.split(",")[0].trim();
		}
		return request.getRemoteAddr();
	}
	
	/** Keeps each entry on one line, truncating the error text to its first line. */
	private static String clean(String s) {
		if(s == null) {
			return "";
		}
		int nl = s.indexOf('\n');
		if(nl >= 0) {
			s = s.substring(0, nl);
		}
		return s.replace('\r', ' ').replace('"', '\'');
	}
}
