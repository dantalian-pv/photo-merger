package ru.dantalian.photomerger.core.utils;

import java.util.zip.Checksum;

public final class Crc64 implements Checksum {

	// use the ISO-3309 polynom
	private static final long POLY = 0xd800000000000000L;

	private static final long[][] TABLE = new long[8][256];

	static {
		for (int i = 0; i < 256; i++) {
			long value = i;
			for (int j = 0; j < 8; j++) {
				if ((value & 1) == 1) {
					value = (value >>> 1) ^ POLY;
				} else {
					value = value >>> 1;
				}
			}
			TABLE[0][i] = value;
		}

		// generate other 8 bit slices
		for (int i = 0; i < 256; i++) {
			long value = TABLE[0][i];
			for (int j = 1; j < 8; j++) {
				value = TABLE[0][(int) (value & 0xff)] ^ (value >>> 8);
				TABLE[j][i] = value;
			}
		}
	}

	private long crc;

	@Override
	public void update(final int aByte) {
		this.crc = (this.crc >>> 8) ^ TABLE[0][(int) ((this.crc ^ aByte) & 0xff)];
	}

	@SuppressWarnings("checkstyle:BooleanExpressionComplexity")
	@Override
	public void update(final byte[] aBuffer, final int aOffset, final int aLength) {
		int idx = aOffset;
		int len = aLength;

		// loop on 8 bytes per loop and use the precomputed lookup table
		while (len >= 8) {
			this.crc = TABLE[7][(int) (this.crc & 0xff ^ (aBuffer[idx] & 0xff))]
					^ TABLE[6][(int) ((this.crc >>> 8) & 0xff ^ (aBuffer[idx + 1] & 0xff))]
					^ TABLE[5][(int) ((this.crc >>> 16) & 0xff ^ (aBuffer[idx + 2] & 0xff))]
					^ TABLE[4][(int) ((this.crc >>> 24) & 0xff ^ (aBuffer[idx + 3] & 0xff))]
					^ TABLE[3][(int) ((this.crc >>> 32) & 0xff ^ (aBuffer[idx + 4] & 0xff))]
					^ TABLE[2][(int) ((this.crc >>> 40) & 0xff ^ (aBuffer[idx + 5] & 0xff))]
					^ TABLE[1][(int) ((this.crc >>> 48) & 0xff ^ (aBuffer[idx + 6] & 0xff))]
					^ TABLE[0][(int) ((this.crc >>> 56) ^ aBuffer[idx + 7] & 0xff)];
			idx += 8;
			len -= 8;
		}

		// the remaining 1-7 bytes are calculated sequentially
		while (len > 0) {
			this.crc = (this.crc >>> 8) ^ TABLE[0][(int) ((this.crc ^ aBuffer[idx]) & 0xff)];
			idx++;
			len--;
		}
	}

	@Override
	public long getValue() {
		return this.crc;
	}

	@Override
	public void reset() {
		this.crc = 0;
	}

	public String getHexValue() {
		return String.format("%1x", Long.valueOf(this.crc));
	}

}