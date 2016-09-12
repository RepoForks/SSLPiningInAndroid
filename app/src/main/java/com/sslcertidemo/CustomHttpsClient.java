package com.sslcertidemo;

import android.content.Context;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.SingleClientConnManager;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;

import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class CustomHttpsClient {

	public static HttpClient getMyHttpsClient(Context context, boolean isPining) {
		HttpClient instance = null;
		if (isPining) {
			instance = new PinningHttpClient(context);
		} else {
			instance = new UnPinningHttpClient();
		}
		return instance;
	}

	/**************************** Pinning Process HttpClient ****************************/
	public static class PinningHttpClient extends DefaultHttpClient {

		private Context context;
		private String password;

		public PinningHttpClient(Context context) {
			this.context = context;
			this.password = context.getString(R.string.Key_PiningPassword);
		}

		@Override
		protected ClientConnectionManager createClientConnectionManager() {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// Register for port 443 our SSLSocketFactory with our keystore
			// to the ConnectionManager
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
			return new SingleClientConnManager(getParams(), registry);
		}

		private SSLSocketFactory newSslSocketFactory() {
			try {
				// Get an instance of the Bouncy Castle KeyStore format
				KeyStore trusted = KeyStore.getInstance("BKS");
				// Get the raw resource, which contains the keystore with
				// your trusted certificates (root and any intermediate certs)
				InputStream in = context.getResources().openRawResource(R.raw.fbcer);
				try {
					// Initialize the keystore with the provided trusted
					// certificates
					// Also provide the password of the keystore
					trusted.load(in, password.toCharArray());
				} finally {
					in.close();
				}
				// Pass the keystore to the SSLSocketFactory. The factory is
				// responsible
				// for the verification of the server certificate.
				SSLSocketFactory sf = new SSLSocketFactory(trusted);
				// Hostname verification from certificate
				// http://hc.apache.org/httpcomponents-client-ga/tutorial/html/connmgmt.html#d4e506
				sf.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
				return sf;
			} catch (Exception e) {
				throw new AssertionError(e);
			}
		}
	}

	/**************************** UnPinning Process HttpClient ****************************/
	public static class UnPinningHttpClient extends DefaultHttpClient {

		@Override
		protected ClientConnectionManager createClientConnectionManager() {
			SchemeRegistry registry = new SchemeRegistry();
			registry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
			// Register for port 443 our SSLSocketFactory with our keystore
			// to the ConnectionManager
			registry.register(new Scheme("https", newSslSocketFactory(), 443));
			return new ThreadSafeClientConnManager(getParams(), registry);
		}

		private SSLSocketFactory newSslSocketFactory() {
			MySSLSocketFactory sf = null;
			try {
				KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
				trustStore.load(null, null);
				sf = new MySSLSocketFactory(trustStore);
				sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);

			} catch (KeyManagementException e) {
				e.printStackTrace();
			} catch (UnrecoverableKeyException e) {
				e.printStackTrace();
			} catch (KeyStoreException e) {
				e.printStackTrace();
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			} catch (CertificateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return sf;
		}
	}

	public static class MySSLSocketFactory extends SSLSocketFactory {
		SSLContext sslContext = SSLContext.getInstance("TLS");

		public MySSLSocketFactory(KeyStore truststore) throws NoSuchAlgorithmException, KeyManagementException, KeyStoreException, UnrecoverableKeyException {
			super(truststore);

			TrustManager tm = new X509TrustManager() {
				public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
				}

				public X509Certificate[] getAcceptedIssuers() {
					return null;
				}
			};

			sslContext.init(null, new TrustManager[] { tm }, null);
		}

		@Override
		public Socket createSocket(Socket socket, String host, int port, boolean autoClose) throws IOException, UnknownHostException {
			return sslContext.getSocketFactory().createSocket(socket, host, port, autoClose);
		}

		@Override
		public Socket createSocket() throws IOException {
			return sslContext.getSocketFactory().createSocket();
		}
	}

}
