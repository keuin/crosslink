package com.keuin.crosslink.api.request;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.keuin.crosslink.util.HttpQuery;
import com.keuin.crosslink.util.LazyEvaluated;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpContext;
import com.sun.net.httpserver.HttpExchange;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.Map;

public class JsonHttpExchange implements AutoCloseable {

    private static final ObjectMapper mapper = new ObjectMapper();

    private final HttpExchange exchange;
    private int rCode = -1; // 200 by default (<= 0)
    private final JsonNode requestBody;
    private final JsonNode responseBody = mapper.readTree("{}");
    private final LazyEvaluated<Map<String, String>> lazyQueryMap;
    private byte[] responseBytes = new byte[0];

    JsonHttpExchange(HttpExchange exchange) throws IOException {
        this.exchange = exchange;
        this.requestBody = mapper.readTree(exchange.getRequestBody());
        this.lazyQueryMap = new LazyEvaluated<>(() -> HttpQuery.getParamMap(exchange.getRequestURI().getQuery()));
        this.getResponseHeaders().set("Content-Type", "application/json");
    }

    public Map<String, String> queryMap() {
        // FIXME string parameter parsing
        return lazyQueryMap.get();
    }

    /**
     * Returns an immutable {@link Map} containing the HTTP headers that were
     * included with this request. The keys in this {@code Map} will be the header
     * names, while the values will be a {@link java.util.List} of
     * {@linkplain java.lang.String Strings} containing each value that was
     * included (either for a header that was listed several times, or one that
     * accepts a comma-delimited list of values on a single line). In either of
     * these cases, the values for the header name will be presented in the
     * order that they were included in the request.
     *
     * <p> The keys in {@code Map} are case-insensitive.
     *
     * @return a read-only {@code Map} which can be used to access request headers
     */
    public Headers getRequestHeaders() {
        return exchange.getRequestHeaders();
    }

    /**
     * Returns a mutable {@link Map} into which the HTTP response headers can be
     * stored and which will be transmitted as part of this response. The keys in
     * the {@code Map} will be the header names, while the values must be a
     * {@link java.util.List} of {@linkplain java.lang.String Strings} containing
     * each value that should be included multiple times (in the order that they
     * should be included).
     *
     * <p> The keys in {@code Map} are case-insensitive.
     *
     * @return a writable {@code Map} which can be used to set response headers.
     */
    public Headers getResponseHeaders() {
        return exchange.getResponseHeaders();
    }

    /**
     * Get the request {@link URI}.
     *
     * @return the request {@code URI}
     */
    public URI getRequestURI() {
        return exchange.getRequestURI();
    }

    /**
     * Get the request method.
     *
     * @return the request method
     */
    public String getRequestMethod() {
        return exchange.getRequestMethod();
    }

    /**
     * Get the {@link HttpContext} for this exchange.
     *
     * @return the {@code HttpContext}
     */
    public HttpContext getHttpContext() {
        return exchange.getHttpContext();
    }

    /**
     * Ends this exchange by doing the following in sequence:
     * <ol>
     *      <li> close the request {@link InputStream}, if not already closed.
     *      <li> close the response {@link OutputStream}, if not already closed.
     * </ol>
     */
    public void close() throws IOException {
        this.getResponseHeaders().set("Content-Type", "application/json");
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        mapper.writeValue(baos, responseBody);
        responseBytes = baos.toByteArray();
        exchange.sendResponseHeaders(getResponseCode(), responseBytes.length);
        exchange.getResponseBody().write(responseBytes);
        exchange.close();
    }

    /**
     * Returns a stream from which the request body can be read.
     * Multiple calls to this method will return the same stream.
     * It is recommended that applications should consume (read) all of the data
     * from this stream before closing it. If a stream is closed before all data
     * has been read, then the {@link InputStream#close()} call will read
     * and discard remaining data (up to an implementation specific number of
     * bytes).
     *
     * @return the stream from which the request body can be read
     */
    public JsonNode getRequestBody() {
        return requestBody;
    }

    /**
     * Returns the response JSON body.
     */
    public ObjectNode getResponseBody() {
        return (ObjectNode) responseBody;
    }


    public void setResponseCode(int code) {
        rCode = code;
    }

    /**
     * Returns the address of the remote entity invoking this request.
     *
     * @return the {@link InetSocketAddress} of the caller
     */
    public InetSocketAddress getRemoteAddress() {
        return exchange.getRemoteAddress();
    }

    /**
     * Returns the response code, if it has already been set.
     *
     * @return the response code, if available. {@code -1} if not available yet.
     */
    public int getResponseCode() {
        return (rCode <= 0) ? 200 : rCode;
    }

    /**
     * Returns the local address on which the request was received.
     *
     * @return the {@link InetSocketAddress} of the local interface
     */
    public InetSocketAddress getLocalAddress() {
        return exchange.getLocalAddress();
    }

    /**
     * Returns the protocol string from the request in the form
     * <i>protocol/majorVersion.minorVersion</i>. For example,
     * "{@code HTTP/1.1}".
     *
     * @return the protocol string from the request
     */
    public String getProtocol() {
        return exchange.getProtocol();
    }

}
