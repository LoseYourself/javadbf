package com.linuxense.javadbf;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import org.junit.Assert;
import org.junit.Test;


public class UtilsTest {

	private static final Charset ISO_8859_1 = StandardCharsets.ISO_8859_1;
	private static final Charset UTF8 = StandardCharsets.UTF_8;
	public UtilsTest () {
		super();
	}
	@Test
	public void testIsPureAscii() {
		assertTrue(DBFUtils.isPureAscii("abcd"));
		assertFalse(DBFUtils.isPureAscii("á"));
		assertFalse(DBFUtils.isPureAscii("ñ"));
		assertTrue(DBFUtils.isPureAscii(""));
		assertTrue(DBFUtils.isPureAscii(null));
	}
	@Test
	public void testToBoolean() {
		assertEquals(Boolean.TRUE, DBFUtils.toBoolean((byte) 't'));
		assertEquals(Boolean.TRUE, DBFUtils.toBoolean((byte) 'T'));
		assertEquals(Boolean.TRUE, DBFUtils.toBoolean((byte) 'y'));
		assertEquals(Boolean.TRUE, DBFUtils.toBoolean((byte) 'Y'));
		
		assertEquals(Boolean.FALSE, DBFUtils.toBoolean((byte) 'f'));
		assertEquals(Boolean.FALSE, DBFUtils.toBoolean((byte) 'F'));
		assertEquals(Boolean.FALSE, DBFUtils.toBoolean((byte) 'n'));
		assertEquals(Boolean.FALSE, DBFUtils.toBoolean((byte) 'N'));
		
		assertNull(DBFUtils.toBoolean((byte) '?'));
		
	}
	
	@Test 
	public void testContains () {
		assertTrue(DBFUtils.contains("test?test".getBytes(), (byte) '?'));
		assertFalse(DBFUtils.contains("testtest".getBytes(), (byte) '?'));
		assertFalse(DBFUtils.contains("".getBytes(), (byte) '?'));
		assertFalse(DBFUtils.contains(null, (byte)'?'));
	}
	@Test
	public void doubleFormating() {
		Assert.assertEquals(
				" 0.00", 
				new String(DBFUtils.doubleFormating(new Double(0.0), Charset.defaultCharset(), 5, 2))
			);
			
			Assert.assertEquals(
					"10.00", 
					new String(DBFUtils.doubleFormating(new Double(10.0), Charset.defaultCharset(), 5, 2))
				);
			Assert.assertEquals(
					" 5.05", 
					new String(DBFUtils.doubleFormating(new Double(5.05), Charset.defaultCharset(), 5, 2))
	);
	}
	@Test
	public void testLittleEndian() {
		// TODO
	}
	@Test
	public void testreadLittleEndianInt() {
		// TODO
	}
	@Test
	public void testReadLittleEndianShort() {
		// TODO
	}
	@Test
	public void testTextPadding()  {
		assertEquals(
			"abc       ",
			new String(DBFUtils.textPadding("abc", ISO_8859_1, 10), ISO_8859_1)
		);
		
		assertEquals(
			"a",
			new String(DBFUtils.textPadding("abc", ISO_8859_1, 1), ISO_8859_1)
		);
		assertEquals(
			"001",
			new String(DBFUtils.textPadding("1", ISO_8859_1, 3, DBFAlignment.RIGHT, (byte) '0'), ISO_8859_1)
		);
		
		//TODO Test extreme cases (null, negative, etc)

	}
	
	@Test
	public void testTextPaddingUTF8() {
		assertEquals(
				"Simón    ",
				new String(DBFUtils.textPadding("Simón", UTF8, 10), UTF8)
		);		
	}

	public void testInvalidUT8Padding() {
		assertEquals(
				"est ",
				new String(DBFUtils.textPadding("está", UTF8, 4), UTF8));
	}
	@Test
	public void testRemoveSpaces() {
		assertEquals("123", new String(DBFUtils.removeSpaces("   123".getBytes())));
		assertEquals("123", new String(DBFUtils.removeSpaces("   123   ".getBytes())));
		assertEquals("123", new String(DBFUtils.removeSpaces("123   ".getBytes())));
		assertEquals("123", new String(DBFUtils.removeSpaces("123".getBytes())));
		assertEquals("", new String(DBFUtils.removeSpaces("".getBytes())));
	}

}
