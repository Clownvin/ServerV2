package com.v2.packets;

import java.util.ArrayList;

import com.v2.lang.ArrayLengthException;
import com.v2.lang.CString;
import com.v2.util.BinaryOperations;
import com.v2.util.DataType;
import com.v2.packets.PacketData;

/**
 * 
 * @author Calvin Gene Hall
 *
 */

// TODO Rewrite methods... DEURRRRRRR.... This is currently SUPER messy....

public final class PacketData {
	public static PacketData fromBytes(byte[] bytes, int offset) {
		if (offset >= bytes.length) {
			return null;
		}
		PacketData packetData = new PacketData(DataType.getDataType((byte) (bytes[offset] & 0x7F)),
				(bytes[offset] & 0x80) >> 7 == 1);
		switch (packetData.getDataType()) {
		case STRING:
			ArrayList<Byte> stringBytes;
			boolean cont = true;
			int index = offset + 1;
			if (packetData.isArray()) {
				index++;
				cont = true;
				CString[] strings = new CString[bytes[offset + 1]];
				for (byte b = 0; b < bytes[offset + 1]; b++) {
					stringBytes = new ArrayList<Byte>();
					cont = true;
					while (cont) {
						stringBytes.add(bytes[index]);
						cont = ((byte) ((bytes[index] & 0x80) >> 7) == 1);
						index++;
					}
					strings[b] = new CString(stringBytes);
				}
				packetData.setObject(strings);
			} else {
				stringBytes = new ArrayList<Byte>();
				cont = true;
				while (cont) {
					stringBytes.add(bytes[index]);
					cont = ((bytes[index] & 0x80) >> 7) == 1;
					index++;
				}
				packetData.setObject(new CString(stringBytes));
			}
			break;
		case INT:
			if (packetData.isArray()) {
				int[] ints = new int[bytes[offset + 1]];
				for (byte b = 0; b < bytes[offset + 1]; b++) {
					int i = BinaryOperations.bytesToInteger(bytes, offset + (b * 4) + 2);
					ints[b] = i;
				}
				packetData.setObject(ints);
			} else {
				int i = BinaryOperations.bytesToInteger(bytes, offset + 1);
				packetData.setObject(i);
			}
			break;
		case DOUBLE:
			long longValue;
			if (packetData.isArray()) {
				double[] doubles = new double[bytes[offset + 1]];
				for (byte b = 0; b < bytes[offset + 1]; b++) {
					doubles[b] = BinaryOperations.bytesToDouble(bytes, offset + (b * 8) + 2);
				}
				packetData.setObject(doubles);
			} else {
				longValue = bytes[offset + 1] << 56;
				byte k = 2;
				for (byte j = 6; j > -1; j--) {
					longValue += bytes[offset + k] << 8 * j;
					k++;
				}
				packetData.setObject(Double.longBitsToDouble(longValue));
			}
			break;
		default:
			System.out.println("Unknown type: " + packetData.getDataType());
			break;
		}
		return packetData;
	}

	private DataType type = null;
	private boolean array = false;

	private Object o = null;

	public PacketData(DataType type, boolean array) {
		this.type = type;
		this.array = array;
	}

	public DataType getDataType() {
		return type;
	}

	public Object getObject() {
		return o;
	}

	public boolean isArray() {
		return array;
	}

	public PacketData setArray(boolean array) {
		this.array = array;
		return this;
	}

	public PacketData setDataType(DataType type) {
		this.type = type;
		return this;
	}

	public PacketData setObject(Object o) {
		this.o = o;
		return this;
	}

	public byte[] toBytes() throws ArrayLengthException {
		ArrayList<Byte> bytes = new ArrayList<Byte>();
		byte typeByte = type.getByte();
		byte[] returnBytes;
		switch (type) {
		case STRING:
			if (array) {
				bytes.add((byte) (typeByte + (1 << 7)));
				java.lang.String[] strings = (java.lang.String[]) o;
				if (strings.length > 255) {
					throw new ArrayLengthException(strings.length + " : Array length cannot be greater than 255");
				}
				bytes.add((byte) (strings.length & 0x000000ff));
				for (java.lang.String string : strings) {
					CString s = new CString(string);
					for (byte b : s.toBytes()) {
						bytes.add(b);
					}
				}
			} else {
				bytes.add(typeByte);
				CString string;
				if (!(o instanceof CString))
					string = new CString((java.lang.String) o);
				else
					string = (CString) o;
				for (byte b : string.toBytes()) {
					bytes.add(b);
				}
			}
			break;
		case INT:
			if (array) {
				bytes.add((byte) (typeByte + (1 << 7)));
				int[] ints = (int[]) o;
				if (ints.length > 255) {
					throw new ArrayLengthException(ints.length + " : Array length cannot be greater than 255");
				}
				bytes.add((byte) (ints.length & 0x000000ff));
				for (int i = 0; i < ints.length; i++) {
					bytes.add((byte) ((ints[i] >> 24) & 0xFF));
					bytes.add((byte) ((ints[i] >> 16) & 0xFF));
					bytes.add((byte) ((ints[i] >> 8) & 0xFF));
					bytes.add((byte) (ints[i] & 0xFF));
				}
			} else {
				bytes.add(typeByte);
				int i = (int) o;
				bytes.add((byte) ((i >> 24) & 0xFF));
				bytes.add((byte) ((i >> 16) & 0xFF));
				bytes.add((byte) ((i >> 8) & 0xFF));
				bytes.add((byte) (i & 0xFF));
			}
			break;
		case DOUBLE:
			long longValue;
			if (array) {
				bytes.add((byte) (typeByte + 1 << 7));
				double[] doubles = (double[]) o;
				if (doubles.length > 255) {
					throw new ArrayLengthException(doubles.length + " : Array length cannot be greater than 255"); // TODO
					// fix
					// this,
					// make
					// it
					// Integer.MAX_VALUE,
					// like
					// all
					// arrays..
					// Do same for other "array" cases.
				}
				bytes.add((byte) (doubles.length & 0x000000ff));
				for (int i = 0; i < doubles.length; i++) {
					longValue = Double.doubleToRawLongBits(doubles[i]);
					for (int j = 7; j > -1; j--)
						bytes.add((byte) ((longValue >> j * 8) & 0xFF));
				}
			} else {
				bytes.add(typeByte);
				for (byte b : BinaryOperations.toBytes((double) o))
					// TODO More use of BinaryOperations utility..
					bytes.add(b);
			}
			break;
		default:
			System.out.println("Unknown type: " + type);
			break;
		}
		returnBytes = new byte[bytes.size()];
		for (int i = 0; i < returnBytes.length; i++) {
			returnBytes[i] = bytes.get(i);
		}
		return returnBytes;
	}
}
