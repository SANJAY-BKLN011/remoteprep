package com.remoteprep;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Verification of Spring Boot embedded static frontend resource serving.
 * Ensures GET /, GET /index.html, static CSS/JS assets, and REST APIs
 * are properly accessible on the embedded Tomcat server.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class StaticResourceTests {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    @DisplayName("GET / serves the assessment frontend HTML")
    void testRootEndpointServesFrontend() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("<title>Offline Aptitude & DSA Assessment System</title>"),
                "Root page should contain Assessment System title");
        assertTrue(response.getBody().contains("Offline Assessment Portal"),
                "Root page should contain Offline Assessment Portal header");
    }

    @Test
    @DisplayName("GET /index.html serves the assessment frontend HTML")
    void testIndexHtmlServesFrontend() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/index.html", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("<title>Offline Aptitude & DSA Assessment System</title>"));
    }

    @Test
    @DisplayName("GET /css/style.css serves the CSS stylesheet")
    void testCssStylesheetServed() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/css/style.css", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getHeaders().getContentType());
        assertTrue(response.getHeaders().getContentType().toString().contains("text/css"));
    }

    @Test
    @DisplayName("GET /js/app.js serves frontend JavaScript")
    void testJsFileServed() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/js/app.js", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("AppState") || response.getBody().contains("init"));
    }

    @Test
    @DisplayName("GET /api/test continues to work as expected")
    void testApiTestEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/test", String.class);
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("RemotePrep backend is working"));
    }
}
