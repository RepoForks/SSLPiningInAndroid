package com.sslcertidemo;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.content.Context;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;

public class MyCrypter {

	private static String TAG = "MyCrypter";
	// private static String password="KEY";

	private static String password = "29041992AXIS@INT04062015";

	// EnCrypted Data for empty string

	public final static String encrp_blankstring = "aVc7tuSFH6iHNKiJ5Dd4Sw==";

	/**
	 * Encodes a String in AES-128 with a given key
	 * 
	 * @param context
	 * @param password
	 * @param text
	 * @return String Base64 and AES encoded String
	 * @throws NoPassGivenException
	 * @throws NoTextGivenException
	 */
	public static String encode(Context context, String text)
			throws NoPassGivenException, NoTextGivenException {
		if (password.length() == 0 || password == null) {
			throw new NoPassGivenException("Please give Password");
		}

		if (TextUtils.isEmpty(text)) {
			return encrp_blankstring;
		}

		try {
			SecretKeySpec skeySpec = getKey();
			byte[] clearText = text.getBytes("UTF8");

			// IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
			final byte[] iv = new byte[16];
			Arrays.fill(iv, (byte) 0x00);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			// Cipher is not thread safe
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.ENCRYPT_MODE, skeySpec, ivParameterSpec);

			String encrypedValue = Base64.encodeToString(
					cipher.doFinal(clearText), Base64.NO_WRAP);
			Log.d(TAG, "Encrypted: " + text + " -> " + encrypedValue);
			return encrypedValue;

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Decodes a String using AES-128 and Base64
	 * 
	 * @param context
	 * @param password
	 * @param text
	 * @return desoded String
	 * @throws NoPassGivenException
	 * @throws NoTextGivenException
	 */
	public static String decode(Context context, String text)
			throws NoPassGivenException, NoTextGivenException {

		if (password.length() == 0 || password == null) {
			throw new NoPassGivenException("Please give Password");
		}

		if (TextUtils.isEmpty(text)) {
			return "";
		}

		try {
			SecretKey key = getKey();

			// IMPORTANT TO GET SAME RESULTS ON iOS and ANDROID
			final byte[] iv = new byte[16];
			Arrays.fill(iv, (byte) 0x00);
			IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

			byte[] encrypedPwdBytes = Base64.decode(text, Base64.NO_WRAP);
			// cipher is not thread safe
			Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding");
			cipher.init(Cipher.DECRYPT_MODE, key, ivParameterSpec);
			byte[] decrypedValueBytes = (cipher.doFinal(encrypedPwdBytes));

			String decrypedValue = new String(decrypedValueBytes);
			Log.d(TAG, "Decrypted: " + text + " -> " + decrypedValue);
			return decrypedValue;

		} catch (InvalidKeyException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (BadPaddingException e) {
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			e.printStackTrace();
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
		} catch (InvalidAlgorithmParameterException e) {
			e.printStackTrace();
		}
		return "";
	}

	/**
	 * Generates a SecretKeySpec for given password
	 * 
	 * @param password
	 * @return SecretKeySpec
	 * @throws UnsupportedEncodingException
	 */
	public static SecretKeySpec getKey() throws UnsupportedEncodingException {

		int keyLength = 128;
		byte[] keyBytes = new byte[keyLength / 8];
		// explicitly fill with zeros
		Arrays.fill(keyBytes, (byte) 0x0);

		// if password is shorter then key length, it will be zero-padded
		// to key length
		byte[] passwordBytes = password.getBytes("UTF-8");
		int length = passwordBytes.length < keyBytes.length ? passwordBytes.length
				: keyBytes.length;
		System.arraycopy(passwordBytes, 0, keyBytes, 0, length);
		SecretKeySpec key = new SecretKeySpec(keyBytes, "AES");
		return key;
	}

	public static class NoTextGivenException extends Exception {
		public NoTextGivenException(String message) {
			super(message);
		}

	}

	public static class NoPassGivenException extends Exception {
		public NoPassGivenException(String message) {
			super(message);
		}

	}

}
