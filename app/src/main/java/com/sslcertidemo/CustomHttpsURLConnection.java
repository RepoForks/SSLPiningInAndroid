package com.sslcertidemo;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import android.content.Context;

public class CustomHttpsURLConnection {

	public static HttpsURLConnection getMyHttpsURLConnection(Context context, String linkurl, boolean isPining) {

		HttpsURLConnection urlConnection = null;

		try {
			URL url = new URL(linkurl);
			urlConnection = (HttpsURLConnection) url.openConnection();
			if (isPining) {
				KeyPinStore keystore = KeyPinStore.getInstance(context);
				urlConnection.setSSLSocketFactory(keystore.getContext().getSocketFactory());
			} else {
				KeyUnPinStore keystore = KeyUnPinStore.getInstance(context);
				urlConnection.setSSLSocketFactory(keystore.getContext().getSocketFactory());
			}
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return urlConnection;

	}

	/**
	 * Key Pining Process
	 */
	public static class KeyPinStore {
		private static Context context = null;
		private static KeyPinStore instance = null;
		private SSLContext sslContext = SSLContext.getInstance("TLS");

		public static synchronized KeyPinStore getInstance(Context mContext) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
				KeyManagementException {
			if (instance == null) {
				context = mContext;
				instance = new KeyPinStore();
			}
			return instance;
		}

		private KeyPinStore() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
			// Load CAs from an InputStream
			// (could be from a resource or ByteArrayInputStream or ...)
			CertificateFactory cf = CertificateFactory.getInstance("X.509");
			// randomCA.crt should be in the Assets directory
			InputStream caInput = new BufferedInputStream(context.getAssets().open("fbcer.cer"));
			Certificate ca;
			try {
				ca = cf.generateCertificate(caInput);
				System.out.println("ca=" + ((X509Certificate) ca).getSubjectDN());
			} finally {
				caInput.close();
			}

			// Create a KeyStore containing our trusted CAs
			String keyStoreType = KeyStore.getDefaultType();
			KeyStore keyStore = KeyStore.getInstance(keyStoreType);
			keyStore.load(null, null);
			keyStore.setCertificateEntry("ca", ca);

			// Create a TrustManager that trusts the CAs in our KeyStore
			String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
			TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
			tmf.init(keyStore);

			// Create an SSLContext that uses our TrustManager
			// SSLContext context = SSLContext.getInstance("TLS");
			sslContext.init(null, tmf.getTrustManagers(), null);
		}

		public SSLContext getContext() {
			return sslContext;
		}
	}

	/**
	 * Key UnPining Process
	 */
	public static class KeyUnPinStore {

		private static Context context = null;
		private static KeyUnPinStore instance = null;
		private SSLContext sslContext = SSLContext.getInstance("TLS");

		public static synchronized KeyUnPinStore getInstance(Context mContext) throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException,
				KeyManagementException {
			if (instance == null) {
				context = mContext;
				instance = new KeyUnPinStore();
			}
			return instance;
		}

		private KeyUnPinStore() throws CertificateException, IOException, KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
			// Create a trust manager that does not validate certificate chains
			TrustManager[] trustAllCerts = new TrustManager[] { new X509TrustManager() {
				public java.security.cert.X509Certificate[] getAcceptedIssuers() {
					return null;
				}

				public void checkClientTrusted(X509Certificate[] certs, String authType) {
				}

				public void checkServerTrusted(X509Certificate[] certs, String authType) {
				}
			} };

			// Create all-trusting host name verifier
			// Create an SSLContext that uses our TrustManager
			// SSLContext context = SSLContext.getInstance("TLS");
			sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
		}

		public SSLContext getContext() {
			return sslContext;
		}

	}

}
