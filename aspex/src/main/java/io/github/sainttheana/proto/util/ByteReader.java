/*Copyright (C) 2023-2025  Saint-Theana

This library is free software; you can redistribute it and/or
modify it under the terms of the GNU Lesser General Public
License as published by the Free Software Foundation; either
version 2.1 of the License, or (at your option) any later version.

This library is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
Lesser General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this library; if not, write to the Free Software
Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301
USA

Please contact Saint-Theana by email the.winter.will.come@gmail.com if you need
additional information or have any questions
*/
package io.github.sainttheana.proto.util;

import java.nio.*;
import io.netty.buffer.*;
import io.netty.util.*;

public class ByteReader {
  private ByteBufAllocator alloc = PooledByteBufAllocator.DEFAULT;

  private ByteBuf byteBuffer;

  public float readFloat() {
    return byteBuffer.readFloat();
  }

  public int readableBytes() {
    return byteBuffer.readableBytes();
  }

  public long readSignedVarLong() {
    long raw = readUnsignedVarLong();
    // This undoes the trick in writeSignedVarLong()
    long temp = (((raw << 63) >> 63) ^ raw) >> 1;
    // This extra step lets us deal with the largest signed values by treating
    // negative results from read unsigned methods as like unsigned values
    // Must re-flip the top bit if the original read value had it set.
    return temp ^ (raw & (1L << 63));
  }

  public long readUnsignedVarLong() {
    long value = 0L;
    int i = 0;
    long b;
    while (((b = byteBuffer.readByte()) & 0x80L) != 0) {
      value |= (b & 0x7F) << i;
      i += 7;
      //            if (i >= 63)
      //            {
      //                throw new RuntimeException("Variable length quantity is too long (must be <=
      // 63)");
      //            }
    }
    return value | (b << i);
  }

  public int readSignedVarInt() {
    int raw = readUnsignedVarInt();
    // This undoes the trick in writeSignedVarInt()
    int temp = (((raw << 31) >> 31) ^ raw) >> 1;
    // This extra step lets us deal with the largest signed values by treating
    // negative results from read unsigned methods as like unsigned values.
    // Must re-flip the top bit if the original read value had it set.
    return temp ^ (raw & (1 << 31));
  }

  public int readUnsignedVarInt() {
    int value = 0;
    int i = 0;
    int b;
    while (((b = byteBuffer.readByte()) & 0b1000_0000) != 0) {
      value |= (b & 0x7F) << i;
      i += 7;
      //            if (i >= 35)
      //            {
      //                throw new RuntimeException("Variable length quantity is too long (must be <=
      // 35)");
      //            }
    }
    return value | (b << i);
  }

  public int readUnsignedByte() {
    // TODO: Implement this method
    return this.byteBuffer.readUnsignedByte();
  }

  public long readLong() {
    return byteBuffer.readLong();
  }

  public int readByte() {
    return this.byteBuffer.readByte();
  }

  public boolean hasMore() {
    return this.byteBuffer.readableBytes() > 0;
  }

  public int restBytesCount() {
    return this.byteBuffer.readableBytes();
  }

  public void readerIndex(int index) {
    this.byteBuffer.readerIndex(index);
  }

  public int readerIndex() {
    return this.byteBuffer.readerIndex();
  }

  public void readerIndexDown(int i) {
    this.byteBuffer.readerIndex(this.byteBuffer.readerIndex() - i);
  }

  public ByteReader(byte[] _data) {
    this.byteBuffer = this.alloc.directBuffer();
    this.byteBuffer.writeBytes(_data);
  }

  public ByteReader update(byte[] data) {
    this.byteBuffer = this.alloc.directBuffer();
    this.byteBuffer.writeBytes(data);
    return this;
  }

  public ByteReader(ByteBuf _data) {
    this.byteBuffer = _data;
  }

  public void destroy() {
    this.byteBuffer.release();
  }

  public byte[] readRestBytes() {
    return this.readBytes(this.byteBuffer.readableBytes());
  }

  public byte[] readRestBytes(boolean destroy) {
    return this.readBytes(this.byteBuffer.readableBytes(), destroy);
  }

  public String readStringByShortLength() {
    int length = readShort();
    return this.readString(length);
  }

  public String readStringByShortLength(boolean destroy) {
    int length = readShort();
    return this.readString(length, true);
  }

  public String readString(int length) {
    return new String(this.readBytes(length));
  }

  public String readString(int length, boolean destroy) {
    String y = new String(this.readBytes(length));
    this.destroy();
    return y;
  }

  public byte[] readBytesByShortLength() {
    int length = readUnsignedShort();
    return this.readBytes(length);
  }

  public int readUnsignedShort() {
    return byteBuffer.readUnsignedShort();
  }

  public byte[] readBytesByShortLength(boolean destroy) {
    int length = readShort();
    return this.readBytes(length, true);
  }

  public byte[] readBytes(int length) {
    byte[] data = new byte[length];
    this.byteBuffer.readBytes(data);
    return data;
  }

  public byte[] readBytes(int length, boolean destroy) {
    byte[] data = new byte[length];
    this.byteBuffer.readBytes(data);
    this.destroy();
    return data;
  }

  public int readShort() {
    return this.byteBuffer.readShort();
  }

  public int readShort(boolean destroy) {
    int y = this.byteBuffer.readShort();
    this.destroy();
    return y;
  }

  public long readInt() {
    return this.byteBuffer.readInt();
  }

  public long readUnsignedInt() {
    return this.byteBuffer.readUnsignedInt();
  }

  public long readUnsignedInt(boolean destroy) {
    long y = this.byteBuffer.readUnsignedInt();
    this.destroy();
    return y;
  }

  public long readInt(boolean destroy) {
    int y = this.byteBuffer.readInt();
    this.destroy();
    return y;
  }
}
