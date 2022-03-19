package org.jeecg.common.util;

import java.security.Key;
import java.security.SecureRandom;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;
public class PasswordUtil {

	/**
	 * JAVA6           PBEWITHMD5ANDDES PBEWITHMD5ANDTRIPLEDES
	 * PBEWITHSHAANDDESEDE PBEWITHSHA1ANDRC2_40 PBKDF2WITHHMACSHA1
	 * */

	/**
	 *         :PBEWITHMD5andDES
	 */
	public static final String ALGORITHM = "PBEWithMD5AndDES";//
	public static final String Salt = "63293188";//

	/**
	 *        1000
	 */
	private static final int ITERATIONCOUNT = 1000;

	/**
	 *             ,                         .       8
	 * 
	 * @return byte[]
	 * */
	public static byte[] getSalt() throws Exception {
		//
		SecureRandom random = new SecureRandom();
		//
		return random.generateSeed(8);
	}

	public static byte[] getStaticSalt() {
		//
		return Salt.getBytes();
	}

	/**
	 *   PBE
	 * 
	 * @param password
	 *
	 * @return Key PBE    
	 * */
	private static Key getPBEKey(String password) {
		//         
		SecretKeyFactory keyFactory;
		SecretKey secretKey = null;
		try {
			keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
			//   PBE    
			PBEKeySpec keySpec = new PBEKeySpec(password.toCharArray());
			//     
			secretKey = keyFactory.generateSecret(keySpec);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return secretKey;
	}

	/**
	 *        
	 * 
	 * @param plaintext
	 *                     
	 * @param password
	 *                       
	 * @param salt
	 *              
	 * @return          
	 * @throws Exception
	 */
	public static String encrypt(String plaintext, String password, String salt) {

		Key key = getPBEKey(password);
		byte[] encipheredData = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.ENCRYPT_MODE, key, parameterSpec);
			//update-begin-author:sccott date:20180815 for:        ，     windows linux         gitee/issues/IZUD7
			encipheredData = cipher.doFinal(plaintext.getBytes("utf-8"));
			//update-end-author:sccott date:20180815 for:        ，     windows linux         gitee/issues/IZUD7
		} catch (Exception e) {
		}
		return bytesToHexString(encipheredData);
	}

	/**
	 *        
	 * 
	 * @param ciphertext
	 *                     
	 * @param password
	 *                       (    ,              )
	 * @param salt
	 *              (    ,              )
	 * @return          
	 * @throws Exception
	 */
	public static String decrypt(String ciphertext, String password, String salt) {

		Key key = getPBEKey(password);
		byte[] passDec = null;
		PBEParameterSpec parameterSpec = new PBEParameterSpec(salt.getBytes(), ITERATIONCOUNT);
		try {
			Cipher cipher = Cipher.getInstance(ALGORITHM);

			cipher.init(Cipher.DECRYPT_MODE, key, parameterSpec);

			passDec = cipher.doFinal(hexStringToBytes(ciphertext));
		}

		catch (Exception e) {
			// TODO: handle exception
		}
		return new String(passDec);
	}

	/**
	 *                
	 * 
	 * @param src
	 *                
	 * @return
	 */
	public static String bytesToHexString(byte[] src) {
		StringBuilder stringBuilder = new StringBuilder("");
		if (src == null || src.length <= 0) {
			return null;
		}
		for (int i = 0; i < src.length; i++) {
			int v = src[i] & 0xFF;
			String hv = Integer.toHexString(v);
			if (hv.length() < 2) {
				stringBuilder.append(0);
			}
			stringBuilder.append(hv);
		}
		return stringBuilder.toString();
	}

	/**
	 *                
	 * 
	 * @param hexString
	 *                   
	 * @return
	 */
	public static byte[] hexStringToBytes(String hexString) {
		if (hexString == null || hexString.equals("")) {
			return null;
		}
		hexString = hexString.toUpperCase();
		int length = hexString.length() / 2;
		char[] hexChars = hexString.toCharArray();
		byte[] d = new byte[length];
		for (int i = 0; i < length; i++) {
			int pos = i * 2;
			d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
		}
		return d;
	}

	private static byte charToByte(char c) {
		return (byte) "0123456789ABCDEF".indexOf(c);
	}


}