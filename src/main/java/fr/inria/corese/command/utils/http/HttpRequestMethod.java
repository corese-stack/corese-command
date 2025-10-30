package fr.inria.corese.command.utils.http;

/** Enumeration of HTTP request methods for SPARQL queries. */
public enum HttpRequestMethod {
    GET("get"),
    POST_URLENCODED("post-urlencoded"),
    POST_DIRECT("post-direct");

    private final String name;

    /**
     * Constructor.
     *
     * @param name The name of the HTTP request method.
     */
    private HttpRequestMethod(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return this.name;
    }
}
