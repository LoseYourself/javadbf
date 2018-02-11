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
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.Arrays;
import java.nio.charset.Charset;

/**
 * DBFField represents a field specification in an dbf file.
 * 
 * DBFField objects are either created and added to a DBFWriter object or
 * obtained from DBFReader object through getField( int) query.
 */
public class DBFField {

	/**
	 * Code for character data
	 * @deprecated You must use {@link DBFDataType#CHARACTER} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_C = (byte) 'C';
	
	/**
	 * Code for logical data
	 * @deprecated You must use {@link DBFDataType#LOGICAL} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_L = (byte) 'L';
	
	/**
	 * Code for numeric data
	 * @deprecated You must use {@link DBFDataType#NUMERIC} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_N = (byte) 'N';
	
	/**
	 * Code for floating poing data
	 * @deprecated You must use {@link DBFDataType#FLOATING_POINT} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_F = (byte) 'F';
	
	/**
	 * Code for dates
	 * @deprecated You must use {@link DBFDataType#DATE} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_D = (byte) 'D';
	
	/**
	 * Code for memo data (not supported by JavaDBF)
	 * @deprecated You must use {@link DBFDataType#MEMO} instead
	 */
	@Deprecated
	public static final byte FIELD_TYPE_M = (byte) 'M';


	//byte[] fieldName = new byte[ 11]; /* 0-10*/
	private DBFDataType type; /* 11 */
	private int reserv1; /* 12-15 */
	private int fieldLength; /* 16 */
	private byte decimalCount; /* 17 */
	private short reserv2; /* 18-19 */
	private byte workAreaId; /* 20 */
	private short reserv3; /* 21-22 */
	private byte setFieldsFlag; /* 23 */
	private byte[] reserv4 = new byte[7]; /* 24-30 */
	private byte indexFieldFlag; /* 31 */

	private String name;

	public DBFField() {
		super();
	}

	/**
	 * Creates a DBFField object from the data read from the given
	 * DataInputStream.
	 * 
	 * The data in the DataInputStream object is supposed to be organised
	 * correctly and the stream "pointer" is supposed to be positioned properly.
	 * 
	 * @param in
	 *            DataInputStream
	 *
	 * @return Returns the created DBFField object.
	 * @throws IOException
	 *             If any stream reading problems occures.
	 */
	protected static DBFField createField(DataInput in, Charset charset) throws IOException {

		DBFField field = new DBFField();

		byte t_byte = in.readByte(); /* 0 */
		if (t_byte == (byte) 0x0d) {
			return null;
		}
		byte[] fieldName = new byte[11];
		in.readFully(fieldName, 1, 10); /* 1-10 */
		fieldName[0] = t_byte;
		int nameNullIndex = fieldName.length -1;
		for (int i = 0; i < fieldName.length; i++) {
			if (fieldName[i] == (byte) 0) {
				nameNullIndex = i;
				break;
			}
		}
		field.name = new String(fieldName, 0, nameNullIndex,charset);
		try {
			field.type = DBFDataType.fromCode(in.readByte()); /* 11 */
		} catch (Exception e) {
			field.type = DBFDataType.UNKNOWN;
		}
		field.reserv1 = DBFUtils.readLittleEndianInt(in); /* 12-15 */
		field.fieldLength = in.readUnsignedByte(); /* 16 */
		field.decimalCount = in.readByte(); /* 17 */
		field.reserv2 = DBFUtils.readLittleEndianShort(in); /* 18-19 */
		field.workAreaId = in.readByte(); /* 20 */
		field.reserv2 = DBFUtils.readLittleEndianShort(in); /* 21-22 */
		field.setFieldsFlag = in.readByte(); /* 23 */
		in.readFully(field.reserv4); /* 24-30 */
		field.indexFieldFlag = in.readByte(); /* 31 */
		adjustLengthForBigCharSupport(field);
		return field;
	}
	private static void adjustLengthForBigCharSupport(DBFField field) {
		// if field type is char, then read length and decimalCount as one number to allow char fields to be
		// longer than 256 bytes. 
		// This is the way Clipper and FoxPro do it, and there is really no downside
		// since for char fields decimal count should be zero for other versions that do not support this extended functionality.
		if (field.type == DBFDataType.CHARACTER) {
			field.fieldLength |= field.decimalCount << 8;
			field.decimalCount = 0;
		}
	}
	/**
	 * Writes the content of DBFField object into the stream as per DBF format
	 * specifications.
	 *
	 * @param out
	 *            OutputStream
	 * @param charset dbf file's charset
	 * @throws IOException
	 *             if any stream related issues occur.
	 */
	protected void write(DataOutput out, Charset charset) throws IOException {
		// Field Name
		out.write(this.name.getBytes(charset)); /* 0-10 */
		out.write(new byte[11 - this.name.length()]);

		// data type
		out.writeByte(this.type.getCode()); /* 11 */
		out.writeInt(0x00); /* 12-15 */
		out.writeByte(this.fieldLength); /* 16 */
		out.writeByte(this.decimalCount); /* 17 */
		out.writeShort((short) 0x00); /* 18-19 */
		out.writeByte((byte) 0x00); /* 20 */
		out.writeShort((short) 0x00); /* 21-22 */
		out.writeByte((byte) 0x00); /* 23 */
		out.write(new byte[7]); /* 24-30 */
		out.writeByte((byte) 0x00); /* 31 */
	}

	/**
	 * Returns the name of the field.
	 * 
	 * @return Name of the field as String.
	 */
	public String getName() {
		return this.name;
	}


	/**
	 * Returns field length.
	 * 
	 * @return field length as int.
	 */
	public int getFieldLength() {
		return this.fieldLength;
	}

	/**
		Returns the decimal part. This is applicable
		only if the field type if of numeric in nature.

		If the field is specified to hold integral values
		the value returned by this method will be zero.

		@return decimal field size as int.
	*/
	public int getDecimalCount() {
		return this.decimalCount;
	}




	/**
		Sets the name of the field.

		@param name of the field as String.
		@since 0.3.3.1
	*/
	public void setName(String name) {
		if (name == null) {
			throw new IllegalArgumentException("Field name cannot be null");
		}

		if (name.length() == 0 || name.length() > 10) {
			throw new IllegalArgumentException("Field name should be of length 0-10");
		}
		if (!DBFUtils.isPureAscii(name)) {
			throw new IllegalArgumentException("Field name must be ASCII");
		}
		this.name = name;
	}



		

	

	public int getReserv1() {
		return this.reserv1;
	}

	public short getReserv2() {
		return this.reserv2;
	}

	public byte getWorkAreaId() {
		return this.workAreaId;
	}

	public short getReserv3() {
		return this.reserv3;
	}

	public byte getSetFieldsFlag() {
		return this.setFieldsFlag;
	}

	public byte[] getReserv4() {
		return Arrays.copyOf(this.reserv4, this.reserv4.length);
	}

	public byte getIndexFieldFlag() {
		return this.indexFieldFlag;
	}

	/**
		Length of the field.
		This method should be called before calling setDecimalCount().

		@param length of the field as int.
	*/
	public void setFieldLength(int length) {
		if (length > this.type.getMaxSize() || length < this.type.getMinSize()) {
			throw new UnsupportedOperationException("Length for " + this.type + " must be between "
					+ this.type.getMinSize() + " and " + this.type.getMaxSize());
		}
		this.fieldLength = length;
	}
	
	/**
	 * Gets the type for this field
	 * @return The type for this field
	 */

	public DBFDataType getType() {
		return this.type;
	}
	/**
	 * Set the type for this field
	 * @param type The type for this field
	 * @throws IllegalArgumentException if type is not write supported
	 */

	public void setType(DBFDataType type) {
		if (!type.isWriteSupported()) {
			throw new IllegalArgumentException("No support for writting " + type);
		}
		this.type = type;
		if (type.getDefaultSize() > 0) {
			this.fieldLength = type.getDefaultSize();
		}
		
	}

	/**
		Sets the decimal place size of the field.
		Before calling this method the size of the field
		should be set by calling setFieldLength().

		@param size of the decimal field.
		
	*/
	public void setDecimalCount(int size) {
		if (size < 0) {
			throw new IllegalArgumentException("Decimal length should be a positive number");
		}
		if (size > this.fieldLength) {
			throw new IllegalArgumentException("Decimal length should be less than field length");
		}
		if (this.type != DBFDataType.NUMERIC && this.type != DBFDataType.FLOATING_POINT) {
			throw new UnsupportedOperationException("Cannot set decimal count on this field:" + this.type);
		}
		this.decimalCount = (byte) size;
	}
	
	public String toString() {
		return 
			this.name+"|" + this.type + " (" + this.type.getCharCode() + ")" +
			"\nLength: " + this.fieldLength +
			"\nDecimalCount:" + this.decimalCount +			
			"\nIndex:" + this.indexFieldFlag;		
	}
	

	/**
	 * Sets the data type of the field.
	 * 
	 * @param type
	 *            of the field. One of the following:<br>
	 *            C, L, N, F, D, M
	 * @deprecated This method is deprecated and is replaced by {@link #setType(DBFDataType)}
	 */
	@Deprecated
	public void setDataType(byte type) {
		setType(DBFDataType.fromCode(type));
	}

	/**
	 * @deprecated This method is deprecated and is replaced by {@link #setName(String)}.
	 */
	@Deprecated
	public void setFieldName(String value) {
		setName(value);
	}

	/**
	 * Returns the data type of the field.
	 * 
	 * @return Data type as byte.
	 * @deprecated This method is deprecated and is replaced by {@link #getType()}
	 */
	@Deprecated
	public byte getDataType() {
		if (this.type != null) {
			return this.type.getCode();
		}
		return 0;
	}

}
