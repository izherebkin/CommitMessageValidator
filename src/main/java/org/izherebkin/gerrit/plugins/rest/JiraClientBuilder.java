package org.izherebkin.gerrit.plugins.rest;

import net.rcarz.jiraclient.BasicCredentials;
import net.rcarz.jiraclient.ICredentials;
import net.rcarz.jiraclient.JiraClient;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.ssl.SSLSocketFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

public class JiraClientBuilder {

    private static final String TLS = "TLS";
    private static final String HTTPS = "https";
    private static final int SSL_PORT = 443;

    private final String url;
    private final String username;
    private final String password;

    private boolean notVerifySSLCert;

    private JiraClientBuilder(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    public static JiraClientBuilder params(String url, String username, String password) {
        return new JiraClientBuilder(url, username, password);
    }

    public JiraClientBuilder notVerifySSLCert() {
        this.notVerifySSLCert = true;
        return this;
    }

    public JiraClient build() throws NoSuchAlgorithmException, KeyManagementException {
        JiraClient jiraClient;
        if (username == null) {
            jiraClient = new JiraClient(url);
        } else {
            ICredentials iCredentials = new BasicCredentials(username, password);
            jiraClient = new JiraClient(url, iCredentials);
        }
        if (notVerifySSLCert) {
            SSLContext sslContext = SSLContext.getInstance(TLS);
            X509TrustManager x509TrustManager = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] xcs, String string) {
                }

                public void checkServerTrusted(X509Certificate[] xcs, String string) {
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
            };
            sslContext.init(null, new TrustManager[]{x509TrustManager}, null);
            SSLSocketFactory sslSocketFactory = new SSLSocketFactory(sslContext, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
            Scheme scheme = new Scheme(HTTPS, SSL_PORT, sslSocketFactory);
            jiraClient.getRestClient().getHttpClient().getConnectionManager().getSchemeRegistry().register(scheme);
        }
        return jiraClient;
    }
}
