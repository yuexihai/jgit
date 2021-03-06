/*
 * Copyright (C) 2010, Marc Strapetz <marc.strapetz@syntevo.com>
 * Copyright (C) 2015, Ivan Motsch <ivan.motsch@bsiag.com> and others
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Distribution License v. 1.0 which is available at
 * https://www.eclipse.org/org/documents/edl-v10.php.
 *
 * SPDX-License-Identifier: BSD-3-Clause
 */

package org.eclipse.jgit.util.io;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Test;

public class AutoLFInputStreamTest {

	@Test
	public void testLF() throws IOException {
		final byte[] bytes = asBytes("1\n2\n3");
		test(bytes, bytes, false);
	}

	@Test
	public void testCR() throws IOException {
		final byte[] bytes = asBytes("1\r2\r3");
		test(bytes, bytes, false);
	}

	@Test
	public void testCRLF() throws IOException {
		test(asBytes("1\r\n2\r\n3"), asBytes("1\n2\n3"), false);
	}

	@Test
	public void testLFCR() throws IOException {
		final byte[] bytes = asBytes("1\n\r2\n\r3");
		test(bytes, bytes, false);
	}

	@Test
	public void testEmpty() throws IOException {
		final byte[] bytes = asBytes("");
		test(bytes, bytes, false);
	}

	@Test
	public void testBinaryDetect() throws IOException {
		final byte[] bytes = asBytes("1\r\n2\r\n3\0");
		test(bytes, bytes, true);
	}

	@Test
	public void testBinaryDontDetect() throws IOException {
		test(asBytes("1\r\n2\r\n3\0"), asBytes("1\n2\n3\0"), false);
	}

	private static void test(byte[] input, byte[] expected,
			boolean detectBinary) throws IOException {
		try (InputStream bis1 = new ByteArrayInputStream(input);
				InputStream cis1 = new AutoLFInputStream(bis1, detectBinary)) {
			int index1 = 0;
			for (int b = cis1.read(); b != -1; b = cis1.read()) {
				assertEquals(expected[index1], (byte) b);
				index1++;
			}

			assertEquals(expected.length, index1);

			for (int bufferSize = 1; bufferSize < 10; bufferSize++) {
				final byte[] buffer = new byte[bufferSize];
				try (InputStream bis2 = new ByteArrayInputStream(input);
						InputStream cis2 = new AutoLFInputStream(bis2,
								detectBinary)) {

					int read = 0;
					for (int readNow = cis2.read(buffer, 0,
							buffer.length); readNow != -1
									&& read < expected.length; readNow = cis2
											.read(buffer, 0, buffer.length)) {
						for (int index2 = 0; index2 < readNow; index2++) {
							assertEquals(expected[read + index2],
									buffer[index2]);
						}
						read += readNow;
					}

					assertEquals(expected.length, read);
				}
			}
		}
	}

	private static byte[] asBytes(String in) {
		return in.getBytes(UTF_8);
	}
}
