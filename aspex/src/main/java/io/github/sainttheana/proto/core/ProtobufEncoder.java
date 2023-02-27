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
package io.github.sainttheana.proto.core;

import io.github.sainttheana.proto.util.ByteBuilder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import io.github.sainttheana.proto.util.Util;

public class ProtobufEncoder
{

	public static final int TYPE_NUMBER = 0;

	public static final int TYPE_NUMBER_64 = 1;

	public static final int TYPE_NUMBER_32 = 5;

	public static final int TYPE_BYTES = 2;

	private Object instance;

	private ByteBuf bb;

	private List<Field> getSortedField(Field[] fields)
	{
		List<Field> filedList = Arrays.asList(fields);
		Collections.sort(
			filedList,
			new Comparator<Field>() {

				@Override
				public int compare(Field l, Field r)
				{
					Tag annotationl = l.getAnnotation(Tag.class);
					if (annotationl == null)
					{
						throw new RuntimeException("a field " + l.getName() + " without @Tag in class ");
					}
					int lTag = annotationl.tag();
					Tag annotationr = r.getAnnotation(Tag.class);
					if (annotationr == null)
					{
						throw new RuntimeException("a field " + r.getName() + " without @Tag in class ");
					}
					int rTag = annotationr.tag();

					if (lTag > rTag)
					{
						return 1;
					}
					else
					{
						return -1;
					}
				}
			});
		return filedList;
	}

	public ProtobufEncoder(Object instance)
	{
		try
		{
			this.instance = instance;
			this.bb = Unpooled.directBuffer();
			if (instance == null)
			{
				throw new RuntimeException("instanceTarget is null");
			}
			Class instanceTarget = instance.getClass();
			for (Field field : getSortedField(instanceTarget.getFields()))
			{
				Tag annotation = field.getAnnotation(Tag.class);
				if (annotation == null)
				{
					throw new RuntimeException(
						"a field " + field.getName() + " without @Tag in class " + instanceTarget.getName());
				}
				int tag = annotation.tag();
				Class<?> type = field.getType();
				if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						if (annotation.isSigned())
						{
							writeSignedNumberByTag(tag, (byte) value);
						}
						else
						{
							writeNumberByTag(tag, (byte) value);
						}
					}
				}
				else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						if (annotation.isFixed())
						{
							if (annotation.isSigned())
							{
								writeFixed32NumberByTag(tag, (int) value);
							}
							else
							{
								writeUnsignedFixed32NumberByTag(tag, (int) value);
							}
							
						}
						else
						{
							if (annotation.isSigned())
							{
								writeSignedNumberByTag(tag, (int) value);
							}
							else
							{
								writeNumberByTag(tag, (int) value);
							}
						}
					}
				}
				else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						if (annotation.isFixed())
						{
							if (annotation.isSigned())
							{
								writeFixed64NumberByTag(tag, (long) value);
							}
							else
							{
								writeUnsignedFixed64NumberByTag(tag, (long) value);
							}
						}
						else
						{
							if (annotation.isSigned())
							{
								writeSignedNumberByTag(tag, (long) value);
							}
							else
							{
								writeNumberByTag(tag, (long) value);
							}
						}
					}
				}
				else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						writeNumberByTag(tag, ((boolean) value) ? 1 : 0);
					}
				}
				else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class))
				{

					Object value = field.get(instance);
					if (value != null)
					{
						// System.out.println("float " + field + " " + value);
						writeNumberByTag(tag, (float) value);
					}
				}
				else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						writeNumberByTag(tag, (double) value);
					}
				}
				else if (type.isArray())
				{
					Object value = field.get(instance);
					if (value != null)
					{
						writeBytesByTag(tag, (byte[]) value);
					}
				}
				else if (type.isAssignableFrom(String.class))
				{
					Object value = field.get(instance);
					if (value != null)
					{
						writeBytesByTag(tag, ((String) value).getBytes());
					}
				}
				else if (type.isEnum())
				{
					Object value = field.get(instance);
					String enumName=(String)Enum.class.getDeclaredMethod("name").invoke(value);
					Tag enumTag=type.getField(enumName).getAnnotation(Tag.class);
					if (enumTag == null)
					{
						throw new RuntimeException("enum must use @Tag");
					}
					writeNumberByTag(tag, enumTag.tag());
				}
				else if (type.isAssignableFrom(List.class))
				{
					Object list = field.get(instance);
					if (list != null)
					{
						Type genericType = field.getGenericType();
						ParameterizedType pt = (ParameterizedType) genericType;
						Class actualTypeArgument = (Class) pt.getActualTypeArguments()[0];

						if (actualTypeArgument.isAssignableFrom(Integer.class)
							|| actualTypeArgument.isAssignableFrom(int.class))
						{
							// list有东西才写东西
							if (((List) list).size() > 0)
							{
								ByteBuilder builder = new ByteBuilder();
								for (int obj : (List<Integer>) list)
								{
									// System.err.println("field "+field.getName());
									if (annotation.isSigned())
									{
										builder.writeSignedVarInt(obj);
									}
									else
									{
										builder.writeUnsignedVarInt(obj);
									}
								}
								writeBytesByTag(tag, builder.toByteArray(true));
							}
						}
						else if (actualTypeArgument.isAssignableFrom(Long.class)
								 || actualTypeArgument.isAssignableFrom(long.class))
						{
							if (((List) list).size() > 0)
							{
								ByteBuilder builder = new ByteBuilder();
								for (long obj : (List<Long>) list)
								{
									// System.err.println("field "+field.getName());
									if (annotation.isSigned())
									{
										builder.writeSignedVarLong(obj);
									}
									else
									{
										builder.writeUnsignedVarLong(obj);
									}
								}
								writeBytesByTag(tag, builder.toByteArray(true));
							}
						}
						else if (actualTypeArgument.isAssignableFrom(String.class))
						{
							//   obj = readCurrentBytes();
							for (String obj : (List<String>) list)
							{
								// System.err.println("field "+field.getName());
								writeBytesByTag(tag, obj.getBytes());
							}
						}
						else if (actualTypeArgument.isAssignableFrom(Boolean.class)
								 || actualTypeArgument.isAssignableFrom(boolean.class))
						{
							if (((List) list).size() > 0)
							{
								ByteBuilder builder = new ByteBuilder();
								for (boolean obj : (List<Boolean>) list)
								{
									// System.err.println("field "+field.getName());
									builder.writeByte(obj ? 1 : 0);
								}
								writeBytesByTag(tag, builder.toByteArray(true));
							}
						}
						else if (actualTypeArgument.isEnum())
						{
							//note: not sure if this is correct
							ByteBuilder builder = new ByteBuilder();
							for (Object obj : (List) list)
							{
								// System.err.println("field "+field.getName());
								String enumName=(String)Enum.class.getDeclaredMethod("name").invoke(obj);
								Tag enumTag=actualTypeArgument.getField(enumName).getAnnotation(Tag.class);
								if (enumTag == null)
								{
									throw new RuntimeException("enum must use @Tag");
								}
								builder.writeByte(enumTag.tag());
							}
							writeBytesByTag(tag, builder.toByteArray(true));
						}
						else
						{
							for (Object obj : (List) list)
							{
								// System.err.println("field "+field.getName());
								byte[] output = new ProtobufEncoder(obj).toByteArray();
								writeBytesByTag(tag, output);
							}
						}
					}
				}
				else
				{
					Object t = field.get(instance);
					if (t != null)
					{
						byte[] output = new ProtobufEncoder(t).toByteArray();
						writeBytesByTag(tag, output);
					}
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}

	

	
	public byte[] toByteArray()
	{
		byte[] b = new byte[bb.readableBytes()];
		bb.readBytes(b);
		bb.release();
		return b;
	}

	public ByteBuf toByteBuf()
	{
		return bb;
	}

	private void writeNumberByTag(int tag, double value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_64);
		bb.writeDoubleLE(value);
	}

	private void writeNumberByTag(int tag, float value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_32);

		bb.writeFloatLE(value);
	}

	
	
	private void flipBytes(byte[] tmp)
	{
		byte[] newBytes=new byte[tmp.length];
		for(int i=0;i<tmp.length;i++){
			newBytes[i]=tmp[tmp.length-1-i];
		}
		for(int i=0;i<tmp.length;i++){
			tmp[i]=newBytes[i];
		}
	}
	
	private void writeUnsignedFixed64NumberByTag(int tag, long value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_64);
	    String hex=Long.toUnsignedString(value,16);
		while(hex.length()<16){
			hex="0"+hex;
		}
		byte[] bytes=Util.hexToBytes(hex);
		flipBytes(bytes);
		bb.writeBytes(bytes);
	}
	
	private void writeFixed64NumberByTag(int tag, long value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_64);
		bb.writeLongLE(value);
	}

	private void writeUnsignedFixed32NumberByTag(int tag, int value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_32);
	    String hex=Integer.toUnsignedString(value,16);
		while(hex.length()<8){
			hex="0"+hex;
		}
		byte[] bytes=Util.hexToBytes(hex);
		flipBytes(bytes);
		bb.writeBytes(bytes);
	}
	
	private void writeFixed32NumberByTag(int tag, int value)
	{
		writeUnsignedVarInt(tag << 3 | TYPE_NUMBER_32);
		bb.writeIntLE(value);
	}

	public void writeBytesByTag(int paramInt, byte[] paramVarArgs)
	{
		writeUnsignedVarInt(paramInt << 3 | TYPE_BYTES);
		writeUnsignedVarInt(paramVarArgs.length);
		bb.writeBytes(paramVarArgs);
	}

	public void writeNumberByTag(int paramInt, int paramLong)
	{
		writeUnsignedVarInt(paramInt << 3 | TYPE_NUMBER);
		writeUnsignedVarInt(paramLong);
	}

	public void writeSignedNumberByTag(int paramInt, int paramLong)
	{
		writeUnsignedVarInt(paramInt << 3 | TYPE_NUMBER);
		writeSignedVarInt(paramLong);
	}

	public void writeNumberByTag(int paramInt, long paramLong)
	{
		writeUnsignedVarInt(paramInt << 3 | TYPE_NUMBER);
		writeUnsignedVarLong(paramLong);
	}

	public void writeSignedNumberByTag(int paramInt, long paramLong)
	{
		writeUnsignedVarInt(paramInt << 3 | TYPE_NUMBER);
		writeSignedVarLong(paramLong);
	}

	private void writeSignedVarLong(long value)
	{
		writeUnsignedVarLong((value << 1) ^ (value >> 63));
	}

	private void writeUnsignedVarLong(long value)
	{
		while ((value & 0xFFFFFFFFFFFFFF80L) != 0L)
		{
			bb.writeByte(((int) value & 0x7F) | 0x80);
			value >>>= 7;
		}
		bb.writeByte((int) value & 0x7F);
	}

	private void writeSignedVarInt(int value)
	{

		writeUnsignedVarInt((value << 1) ^ (value >> 31));
	}

	private void writeUnsignedVarInt(int value)
	{
		while ((value & 0xFFFFFF80) != 0L)
		{
			bb.writeByte((value & 0x7F) | 0x80);
			value >>>= 7;
		}
		bb.writeByte(value & 0x7F);
	}

	public ByteBuf encode()
	{
		return bb;
	}
}
