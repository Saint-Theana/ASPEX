/*
Copyright (C) 2023-2025  Saint-Theana

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
import io.netty.buffer.*;
import io.netty.util.*;
public class ByteBuilder
{
	private ByteBuf byteBuffer;

    public ByteBuilder()
	{
		this.byteBuffer = Unpooled.directBuffer();
	}

	public void writeFloat(float naN)
	{
	}
	public void clean()
	{
		this.byteBuffer = Unpooled.directBuffer();
		this.byteBuffer.writerIndex(0);
	}

	public void destroy()
	{
		this.byteBuffer.release();
	}

	public int length()
	{
		return this.byteBuffer.writerIndex();
	}

	public byte[] toByteArray(boolean... destroy)
	{
		byte[] data = new byte[this.length()];
		this.byteBuffer.getBytes(0, data);
		if(destroy.length>0){
			this.destroy();
		}
		return data;
	}

	public ByteBuf toByteBuf()
	{
		return this.byteBuffer;
	}

	private byte[] selfGetData()
	{
		byte[] data = new byte[this.length()];
		this.byteBuffer.getBytes(0, data);
		return data;
	}

	public ByteBuilder writeBytes(byte[] to_write)
	{
		this.byteBuffer.writeBytes(to_write);
		return this;
	}


	public ByteBuilder writeBytes(String to_write)
	{
		this.writeBytes(to_write.getBytes());
		return this;
	}

	public ByteBuilder writeBytes(ByteBuf to_write)
	{
		this.byteBuffer.writeBytes(to_write);
		to_write.release();
		return this;
	}

	public ByteBuilder writeInt(long to_write)
	{
		this.byteBuffer.writeInt((int)to_write);
		return this;
	}
	public ByteBuilder writeShort(int to_write)
	{
		this.byteBuffer.writeShort((short)to_write);
		return this;
	}

	public ByteBuilder writeByte(byte to_write)
	{
		this.byteBuffer.writeByte(to_write);
		return this;
	}

	public ByteBuilder writeByte(int to_write)
	{
		this.byteBuffer.writeByte(to_write);
		return this;
	}

	public ByteBuilder writeLong(long to_write)
	{
		this.byteBuffer.writeLong(to_write);
		return this;
	}


	public ByteBuilder rewriteShort(short to_write)
	{
		byte[] data = this.selfGetData();
		this.byteBuffer.writerIndex(0);
		this.byteBuffer.writeShort(to_write);
		this.byteBuffer.writeBytes(data);
		return this;
	}

	public ByteBuilder rewriteByte(byte to_write)
	{
		byte[] data = this.selfGetData();
		this.byteBuffer.writerIndex(0);
		this.byteBuffer.writeByte(to_write);
		this.byteBuffer.writeBytes(data);
		return this;
	}

	public ByteBuilder rewriteInt(int to_write)
	{
		byte[] data = this.selfGetData();
		this.byteBuffer.clear();
		this.byteBuffer.writeInt(to_write);
		this.byteBuffer.writeBytes(data);
		return this;
	}

	public ByteBuilder rewriteBytes(byte[] to_write)
	{
		byte[] data = this.selfGetData();
		this.byteBuffer.writerIndex(0);
		this.byteBuffer.writeBytes(to_write);
		this.byteBuffer.writeBytes(data);
		return this;
	}

	
	
	public void writeSignedVarLong(long value) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarLong((value << 1) ^ (value >> 63));
    }


    public void writeUnsignedVarLong(long value)  {
        while ((value & 0xFFFFFFFFFFFFFF80L) != 0L) {
            byteBuffer.writeByte(((int) value & 0x7F) | 0x80);
            value >>>= 7;
        }
        byteBuffer.writeByte((int) value & 0x7F);
    }

    /**
     * @see #writeSignedVarLong(long, java.io.DataOutput)
     */
    public void writeSignedVarInt(int value) {
        // Great trick from http://code.google.com/apis/protocolbuffers/docs/encoding.html#types
        writeUnsignedVarInt((value << 1) ^ (value >> 31));
    }

    /**
     * @see #writeUnsignedVarLong(long, java.io.DataOutput)
     */
    public void writeUnsignedVarInt(int value) {
        while ((value & 0xFFFFFF80) != 0L) {
            byteBuffer.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
		byteBuffer.writeByte(value & 0x7F);
    }
	
	
	
	public ByteBuilder writeBytesByShortLength(byte[] to_write)
	{
		this.writeShort(to_write.length);
		this.writeBytes(to_write);
		return this;
	}

	public ByteBuilder rewriteSelfShortLength(int i)
	{
		rewriteShort((short)(this.length() + i));
		return this;
	}

	public ByteBuilder rewriteSelfIntLength(int i)
	{
		rewriteInt((this.length() + i));
		return this;
	}

	public ByteBuilder writePb(int paramInt1, int paramInt2)
	{
		writeVarint(paramInt1, paramInt2);
		return this;
	}

	public ByteBuilder writePb(int paramInt, long paramLong)
	{
		writeVarint(paramInt, paramLong);
		return this;
	}

	public ByteBuilder writePb(int paramInt, String paramString)
	{
		writeLengthDelimt(paramInt, paramString.getBytes());
		return this;
	}

	public ByteBuilder writePb(int paramInt, byte[] paramArrayOfByte)
	{
		writeLengthDelimt(paramInt, paramArrayOfByte);
		return this;
	}

	private void writeLengthDelimt(int paramInt, byte... paramVarArgs)
	{
		writeVarint(paramInt << 3 | 0x2);
		writeVarint(paramVarArgs.length);
		this.byteBuffer.writeBytes(paramVarArgs);
	}

	private void writeVarint(int paramInt, long paramLong)
	{
		writeVarint(paramInt << 3 | 0x0);
		writeVarint(paramLong);
	}

	private void writeVarint(long number)
	{
		while (true)
		{
			long towrite = number & 0x7f;
			number >>= 7;
			if (number != 0)
				byteBuffer.writeByte((int)(towrite | 0x80));
			else
			{
				byteBuffer.writeByte((int)towrite);
				break;
			}
		}


//        long abs = Math.abs(j);
//        do {
//            this.byteBuffer.writeByte((byte) ((int) ((127 & abs) | 128)));
//            abs >>= 7;
//        } while (abs != 0);
//		this.byteBuffer.readerIndex(this.byteBuffer.readableBytes()-1);
//		byte y =this.byteBuffer.readByte();
//		this.byteBuffer.readerIndex(0);
//		this.byteBuffer.writerIndex(this.byteBuffer.readableBytes()-1);
//		this.byteBuffer.writeByte(y&127);
    }
}
