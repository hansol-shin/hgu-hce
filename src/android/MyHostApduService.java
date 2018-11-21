package kr.co.itsm.plugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Arrays;
import android.annotation.SuppressLint;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

@SuppressLint("NewApi")
public class MyHostApduService extends HostApduService {

	private static final String TAG = "HANDONG UNIV";

	private static final byte[] SELECT_APDU_HEADER =  { 0x00, (byte)0xA4, 0x04, 0x00 };
	private static final byte[] AID_ANDROID = { (byte)0xF0, 0x48, 0x41, 0x4E, 0x44, 0x4F, 0x4E, 0x47, 0x55, 0x4E, 0x49, 0x56, 0x31, 0x30 };
	//private static final byte[] AID_ANDROID = { 0x53,0x4C,0x35,0x31,0x31,0x39};
	private static final byte[] APP_NAME = { (byte)0xF0, 0x48, 0x41, 0x4E, 0x44, 0x4F, 0x4E, 0x47, 0x55, 0x4E, 0x49, 0x56 };
	private static final byte[] APP_VERSION = { 0x01, 0x00 };

	private static final byte[] SID = "01".getBytes();
	private static final byte[] TID = "0001".getBytes();

	// class
	private static final byte CLA_HANDONG_UNIV	= (byte)0x90;

	// INS
	private static final byte INS_READ_RECORD = (byte)0xB2;

	// status word
	private static final byte[] SW_SUCCESS 			= { (byte)0x90, 0x00 };	// success
	private static final byte[] SW_CLA_NOT_SUPPORT	= { (byte)0x6E, 0x00 };	// cla not supported
	private static final byte[] SW_INS_NOT_SUPPORT	= { 0x6D, 0x00 };		// ins not supported
	private static final byte[] SW_INVALID_P1_P2		= { 0x6A, (byte)0x86 };	// invalid P1, P2
	private static final byte[] SW_FILE_NOT_FOUND		= { 0x6A, (byte)0x82 };	// file not found
	private static final byte[] SW_RECORD_NOT_FOUND	= { 0x6A, (byte)0x83 };	// record not found
	private static final byte[] SW_WRONG_LENGTH		= { 0x67, 0x00 };		// wroing length

	private static byte[] Card_number = null;

	@Override
	public byte[] processCommandApdu(byte[] apdu, Bundle extras) {


		/*

		if (selectAidApdu(apdu)) {
			Log.i("HCEDEMO", "Application selected");
			return getWelcomeMessage();
		} else if (readRecordApud(apdu)) {
			Log.i("HCEDEMO", "Send date");
			return getIdCard(apdu);
		} else {
			// 지원하지 않는 CLA 코드
			byte[] result = new byte[2];
			result[0] = 0x6E;
			result[1] = 0x00;

			return result;
		}
		*/

		Log.d(TAG, "RECV APDU : " + ByteArrayToHexString(apdu));

		if (true==compareSelectCommand(apdu))
		{
			Log.i(TAG, "Application selected");
			String sdPath = "/Android/data/" + getPackageName() + "/card.txt";
			File f = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), sdPath);
			if (f.exists()) {
				try {
					InputStream is = new FileInputStream(f);
					int readcount = (int)f.length();
					Card_number = new byte[readcount];
					is.read(Card_number);
				} catch (Exception e) {
					return SW_FILE_NOT_FOUND;
				}

				return getWelcomeMessage();
			} else {
				return SW_FILE_NOT_FOUND;
			}
		}
		else
		{
			if((byte)CLA_HANDONG_UNIV!=(byte)apdu[0])
				return SW_CLA_NOT_SUPPORT;

			if((byte)INS_READ_RECORD==(byte)apdu[1])
				return getIdCard(apdu);

			return SW_INS_NOT_SUPPORT;
		}


	}

	private byte[] getWelcomeMessage() {
		byte[] result = new byte[ 13 + AID_ANDROID.length + APP_NAME.length + APP_VERSION.length ];

		int index = 0;
		// FCI Template
		result[index++] = 0x6F;
		result[index++] = (byte)(9 + AID_ANDROID.length + APP_NAME.length + APP_VERSION.length);
		// DF Name
		result[index++] = (byte)0x84;
		result[index++] = (byte)AID_ANDROID.length;
		System.arraycopy(AID_ANDROID, 0, result, index, AID_ANDROID.length);
		index += AID_ANDROID.length;
		// FCI Proprietary Template
		result[index++] = (byte)0xA5;
		result[index++] = (byte)(5 + APP_NAME.length + APP_VERSION.length);
		// Application Label
		result[index++] = 0x50;
		result[index++] = (byte)APP_NAME.length;
		System.arraycopy(APP_NAME, 0, result, index, APP_NAME.length);
		index += APP_NAME.length;
		// Application Version
		result[index++] = (byte)0x9F;
		result[index++] = 0x08;
		result[index++] = (byte)APP_VERSION.length;
		System.arraycopy(APP_VERSION, 0, result, index, APP_VERSION.length);
		index += APP_VERSION.length;
		// SW
		result[index++] = (byte)0x90;
		result[index] = 0x00;

		return result;
	}

	private byte[] getIdCard(byte[] apdu) {


/*
		if (apdu.length < 3 || apdu[2] != 0x01) {
			// 레코드를 찾을 수 없음
			byte[] result = new byte[2];
			result[0] = 0x6A;
			result[1] = (byte)0x83;

			return result;
*/

		if(0x04!=(byte)(apdu[3] & 0x04))
			return SW_INVALID_P1_P2;

		if(0x08!=(byte)(apdu[3] & 0x08))
			return SW_FILE_NOT_FOUND;

		if(0x01!=apdu[2])
			return SW_RECORD_NOT_FOUND;

		if(0x00!=apdu[4] && (byte)(6 + 16)!=apdu[4])
			return SW_WRONG_LENGTH;

		byte[] result = new byte[ 12 + 16 + SID.length + TID.length ];

		int index = 0;
		// READ Record Template
		result[index++] = 0x70;
		result[index++] = (byte)( 8 + 16 + SID.length + TID.length );
			// 학생정보
		result[index++] = 0x5A;
		result[index++] = (byte)( 16 );
		System.arraycopy(Card_number, 0, result, index, Card_number.length);
		index += 16;
		// SID
		result[index++] = (byte)0xDF;
		result[index++] = (byte)0x01;
		result[index++] = (byte)( SID.length );
		System.arraycopy(SID, 0, result, index, SID.length);
		index += SID.length;
		// TID
		result[index++] = (byte)0xDF;
		result[index++] = (byte)0x02;
		result[index++] = (byte)( TID.length );
		System.arraycopy(TID, 0, result, index, TID.length);
		index += TID.length;
				// SW
		result[index++] = (byte)0x90;
		result[index] = 0x00;

		return result;
	}

	/*
	private boolean selectAidApdu(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte)0 && apdu[1] == (byte)0xa4;
	}

	private boolean readRecordApud(byte[] apdu) {
		return apdu.length >= 2 && apdu[0] == (byte)0x90 && apdu[1] == (byte)0xB2;
	}
	*/

	@Override
	public void onDeactivated(int reason) {
		Log.i(TAG, "Deactivated: " + reason);
	}

	public static boolean compareSelectCommand(byte[] apdu)
	{
		int length = (int)(apdu[4] & (int)0x00FF);
		byte[] header = Arrays.copyOfRange(apdu, 0, 4);
		byte[] aid = Arrays.copyOfRange(apdu, 5, (5 + length));

		// check apdu header
		if(false == Arrays.equals(header, SELECT_APDU_HEADER) && length == AID_ANDROID.length)
			return false;

		// check aids
		Log.d(TAG, "AID : " + ByteArrayToHexString(aid));
		if(false == Arrays.equals(aid, AID_ANDROID))
			return false;

		return true;
	}

	public static String ByteArrayToHexString(byte[] bytes)
	{
		final char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
		char[] hexChars = new char[bytes.length * 2]; // Each byte has two hex characters (nibbles)
		int v;
		for (int j = 0; j < bytes.length; j++)
		{
			v = bytes[j] & 0xFF; // Cast bytes[j] to int, treating as unsigned value
			hexChars[j * 2] = hexArray[v >>> 4]; // Select hex character from upper nibble
			hexChars[j * 2 + 1] = hexArray[v & 0x0F]; // Select hex character from lower nibble
		}
		return new String(hexChars);
	}

	public static byte[] HexStringToByteArray(String s) throws IllegalArgumentException
	{
		int len = s.length();
		if (len % 2 == 1) {
			throw new IllegalArgumentException("Hex string must have even number of characters");
		}
		byte[] data = new byte[len / 2]; // Allocate 1 byte per 2 hex characters
		for (int i = 0; i < len; i += 2) {
			// Convert each character into a integer (base-16), then bit-shift into place
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}
}