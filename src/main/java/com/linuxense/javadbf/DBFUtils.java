/*

(C) Copyright 2015-2016 Alberto Fernández <infjaf@gmail.com>
(C) Copyright 2014 Jan Schlößin
(C) Copyright 2003-2004 Anil Kumar K <anil@linuxense.com>

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 3.0 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library.  If not, see <http://www.gnu.org/licenses/>.

*/

package com.linuxense.javadbf;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInput;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

/**
 * Miscelaneous functions required by the JavaDBF package.
 *
 */
public final class DBFUtils {
	
	private static final CharsetEncoder ASCII_ENCODER = Charset.forName("US-ASCII").newEncoder(); 
	
	private DBFUtils() {
		throw new AssertionError("No instances of this class are allowed");
	}
	
	/**
	 * Read a littleEndian integer(32b its) from DataInput
	 * @param in DataInput to read from
	 * @return int value of next 32 bits as littleEndian
	 * @throws IOException
	 */
	public static int readLittleEndianInt(DataInput in) throws IOException {
		int bigEndian = 0;
		for (int shiftBy = 0; shiftBy < 32; shiftBy += 8) {
			bigEndian |= (in.readUnsignedByte() & 0xff) << shiftBy;
		}
		return bigEndian;
	}

	
	/**
	 * Read a littleEndian short(16 bits) from DataInput
	 * @param in DataInput to read from
	 * @return short value of next 16 bits as littleEndian
	 * @throws IOException
	 */
	public static short readLittleEndianShort(DataInput in) throws IOException {
		int low = in.readUnsignedByte() & 0xff;
		int high = in.readUnsignedByte();
		return (short) (high << 8 | low);
	}
	
	/**
	 * Remove all spaces (32) found in the data.
	 * @param array the data
	 * @return the data cleared of whitespaces
	 */
	public static byte[] removeSpaces(byte[] array) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(array.length);
		for (byte b: array) {
			if (b != ' '){
				baos.write(b);
			}
		}
		return baos.toByteArray();
	}
	
	/**
	 * Convert a short value to littleEndian
	 * @param value value to be converted
	 * @return littleEndian short
	 */
	public static short littleEndian(short value) {

		short num1 = value;
		short mask = (short) 0xff;

		short num2 = (short) (num1 & mask);
		num2 <<= 8;
		mask <<= 8;

		num2 |= (num1 & mask) >> 8;

		return num2;
	}

	/**
	 * Convert an int value to littleEndian
	 * @param value value to be converted
	 * @return littleEndian int
	 */
	public static int littleEndian(int value) {

		int num1 = value;
		int mask = 0xff;
		int num2 = 0x00;

		num2 |= num1 & mask;

		for (int i = 1; i < 4; i++) {
			num2 <<= 8;
			mask <<= 8;
			num2 |= (num1 & mask) >> (8 * i);
		}

		return num2;
	}
	
	/**
	 * pad a string and convert it to byte[] to write to a dbf file (by default, add whitespaces to the end of the string)
	 * @param text The text to be padded
	 * @param charset Charset to use to encode the string
	 * @param length Size of the padded string
	 * @return bytes to write to the dbf file
	 */
	public static byte[] textPadding(String text, Charset charset, int length) {
		return textPadding(text, charset, length, DBFAlignment.LEFT, (byte) ' ');
	}
	
	/**
	 * pad a string and convert it to byte[] to write to a dbf file
	 * @param text The text to be padded
	 * @param charset Charset to use to encode the string
	 * @param length Size of the padded string
	 * @param alignment alignment to use to padd
	 * @param paddingByte the byte used to pad the string
	 * @return bytes to write to the dbf file
	 */
	public static byte[] textPadding(String text, Charset charset, int length, DBFAlignment alignment, byte paddingByte) {
		byte response[] = new byte[length];
		Arrays.fill(response, paddingByte);		
		byte[] stringBytes = text.getBytes(charset);
		
		if (stringBytes.length > length){
			return textPadding(text.substring(0, text.length() -1), charset, length, alignment, paddingByte);
		}

		int t_offset = 0;
		switch (alignment) {
		case RIGHT:
			t_offset = length - stringBytes.length;
			break;
		case LEFT:
		default:
			t_offset = 0;
			break;

		}		
		System.arraycopy(stringBytes, 0, response, t_offset, stringBytes.length);

		return response;
	}
	
	/**
	 * Format a number to write to a dbf file
	 * @param doubleNumber number to write
	 * @param charset charset to use
	 * @param fieldLength 
	 * @param sizeDecimalPart
	 * @return bytes to write to the dbf file
	 */
	
	public static byte[] doubleFormating(Double doubleNumber, Charset charset, int fieldLength, int sizeDecimalPart) {
		return doubleFormating((Number) doubleNumber, charset, fieldLength, sizeDecimalPart);
	}
	
	/**
	 * Format a double number to write to a dbf file
	 * @param num Number to write
	 * @param charset charset to use
	 * @param fieldLength 
	 * @param sizeDecimalPart
	 * @return bytes to write to the dbf file
	 */

	public static byte[] doubleFormating(Number num, Charset charset, int fieldLength, int sizeDecimalPart) {
		int sizeWholePart = fieldLength - (sizeDecimalPart > 0 ? (sizeDecimalPart + 1) : 0);

		StringBuilder format = new StringBuilder(fieldLength);
		for (int i = 0; i < sizeWholePart-1; i++) {
			format.append("#");
		}
		if (format.length() < sizeWholePart) {
			format.append("0");
		}
		if (sizeDecimalPart > 0) {
			format.append(".");
			for (int i = 0; i < sizeDecimalPart; i++) {
				format.append("0");
			}
		}

		DecimalFormat df = (DecimalFormat) NumberFormat.getInstance(Locale.ENGLISH);
		df.applyPattern(format.toString());
		return textPadding(df.format(num).toString(), charset, fieldLength, DBFAlignment.RIGHT, (byte) ' ');
	}
	
	/**
	 * Checks that a byte array contains some specific byte
	 * @param array The array to search in
	 * @param value The byte to search for
	 * @return true if the array contains spcified value
	 */
	public static boolean contains(byte[] array, byte value) {
		if (array != null) {
			for (byte data: array) {
				if (data == value) {
					return true;
				}
			}
		}
		return false;		
	}
	
	
	/**
	 * Checks if a string is pure Ascii
	 * @param stringToCheck the string
	 * @return true if is ascci
	 */
	public static boolean isPureAscii(String stringToCheck) {
		if (stringToCheck == null || stringToCheck.length() == 0) {
			return true;
		}
		synchronized (ASCII_ENCODER) {			
			return ASCII_ENCODER.canEncode(stringToCheck);
		}
	}

	/**
	 * Convert LOGICAL (L) byte to boolean value
	 * @param t_logical The byte value as stored in the file
	 * @return The boolean value
	 */
	public static Object toBoolean(byte t_logical) {
		if (t_logical == 'Y' || t_logical == 'y' || t_logical == 'T' || t_logical == 't') {
			return Boolean.TRUE;
		} else if (t_logical == 'N' || t_logical == 'n' || t_logical == 'F' || t_logical == 'f'){
			return Boolean.FALSE;
		}
		return null;
	}
	
	public static byte[] trimRightSpaces(byte[] b_array) {
		if (b_array == null || b_array.length == 0) {
			return new byte[0];
		}
		int pos = getRightPos(b_array);
		int length = pos+1;
		byte[] newBytes = new byte[length];
		System.arraycopy(b_array, 0, newBytes, 0, length);
		return newBytes;		
	}
	
	private static int getRightPos(byte[] b_array) {
		int pos = b_array.length - 1;
		while (pos >= 0 && b_array[pos] == (byte)' ') {
			pos--;
		}
		return pos;
	}
	
	/**
	 * Closes silently a #{@link java.io.Closeable}.
	 * it can be null or throws an exception, will be ignored.
	 * @param closeable The item to close
	 */
	public static void close(Closeable closeable) {
		if (closeable != null) {
			try {
				closeable.close();
			} catch (Exception e) { //NOPMD
				// nop
			}
		}
	}

}
