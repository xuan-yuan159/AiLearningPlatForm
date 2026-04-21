package org.xuanyuan.upload.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.xuanyuan.upload.service.UploadTaskService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
public class UploadProgressFilter extends OncePerRequestFilter {

    private static final String HEADER_USER_ID = "X-User-Id";

    private final UploadTaskService uploadTaskService;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String uri = request.getRequestURI();
        if (!"/upload/resources".equals(uri) && !"/upload/video".equals(uri) && !"/upload/image".equals(uri)) {
            return true;
        }
        String contentType = request.getContentType();
        return !StringUtils.hasText(contentType) || !contentType.toLowerCase().startsWith("multipart/form-data");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String uploadTaskId = resolveUploadTaskId(request);
        Long teacherId = resolveTeacherId(request);
        if (!StringUtils.hasText(uploadTaskId) || teacherId == null) {
            filterChain.doFilter(request, response);
            return;
        }

        long totalBytes = request.getContentLengthLong();
        ProgressHttpServletRequestWrapper wrappedRequest = new ProgressHttpServletRequestWrapper(
                request,
                (loadedBytes) -> uploadTaskService.recordReceivingProgress(uploadTaskId, teacherId, loadedBytes, totalBytes),
                totalBytes
        );
        filterChain.doFilter(wrappedRequest, response);
    }

    private Long resolveTeacherId(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (!StringUtils.hasText(userId)) {
            return null;
        }
        try {
            return Long.valueOf(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private String resolveUploadTaskId(HttpServletRequest request) {
        String query = request.getQueryString();
        if (!StringUtils.hasText(query)) {
            return null;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            if (parts.length == 2 && "uploadTaskId".equals(parts[0]) && StringUtils.hasText(parts[1])) {
                return parts[1];
            }
        }
        return null;
    }

    @FunctionalInterface
    private interface ProgressReporter {
        void report(long loadedBytes);
    }

    private static class ProgressHttpServletRequestWrapper extends HttpServletRequestWrapper {

        private final ProgressReporter progressReporter;
        private final long totalBytes;
        private ServletInputStream inputStream;
        private BufferedReader reader;

        private ProgressHttpServletRequestWrapper(HttpServletRequest request, ProgressReporter progressReporter, long totalBytes) {
            super(request);
            this.progressReporter = progressReporter;
            this.totalBytes = totalBytes;
        }

        @Override
        public ServletInputStream getInputStream() throws IOException {
            if (inputStream == null) {
                inputStream = new ProgressServletInputStream(super.getInputStream(), progressReporter, totalBytes);
            }
            return inputStream;
        }

        @Override
        public BufferedReader getReader() throws IOException {
            if (reader == null) {
                reader = new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
            }
            return reader;
        }
    }

    private static class ProgressServletInputStream extends ServletInputStream {

        private static final long REPORT_STEP_BYTES = 256 * 1024;

        private final ServletInputStream delegate;
        private final ProgressReporter progressReporter;
        private final long totalBytes;

        private long loadedBytes;
        private long lastReportedBytes;
        private int lastReportedPercent = -1;

        private ProgressServletInputStream(ServletInputStream delegate, ProgressReporter progressReporter, long totalBytes) {
            this.delegate = delegate;
            this.progressReporter = progressReporter;
            this.totalBytes = totalBytes;
        }

        @Override
        public int read() throws IOException {
            int value = delegate.read();
            if (value != -1) {
                afterRead(1);
            }
            return value;
        }

        @Override
        public int read(byte[] b) throws IOException {
            int count = delegate.read(b);
            afterRead(count);
            return count;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            int count = delegate.read(b, off, len);
            afterRead(count);
            return count;
        }

        @Override
        public boolean isFinished() {
            return delegate.isFinished();
        }

        @Override
        public boolean isReady() {
            return delegate.isReady();
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            delegate.setReadListener(readListener);
        }

        private void afterRead(int count) {
            if (count <= 0) {
                return;
            }
            loadedBytes += count;
            int percent = totalBytes > 0 ? (int) Math.min(90L, loadedBytes * 90L / totalBytes) : 0;
            boolean shouldReport = loadedBytes - lastReportedBytes >= REPORT_STEP_BYTES
                    || percent != lastReportedPercent
                    || (totalBytes > 0 && loadedBytes >= totalBytes);
            if (!shouldReport) {
                return;
            }
            lastReportedBytes = loadedBytes;
            lastReportedPercent = percent;
            progressReporter.report(loadedBytes);
        }
    }
}
