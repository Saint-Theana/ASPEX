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

import io.github.sainttheana.proto.util.ByteReader;
import io.github.sainttheana.proto.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.lang.reflect.InvocationTargetException;

public class ProtobufDecoder {
  private ByteBuf bb;

  private ProtobufDecoder(ByteBuf buf) {
    this.bb = buf;
  }

  private List<Field> getSortedField(Field[] fields) {
    List<Field> filedList = Arrays.asList(fields);

    Collections.sort(
        filedList,
        new Comparator<Field>() {

          @Override
          public int compare(Field l, Field r) {
            Tag annotationl = l.getAnnotation(Tag.class);
            if (annotationl == null) {
              throw new RuntimeException("a field " + l.getName() + " without @Tag in class ");
            }
            int lTag = annotationl.tag();
            Tag annotationr = r.getAnnotation(Tag.class);
            if (annotationr == null) {
              throw new RuntimeException("a field " + r.getName() + " without @Tag in class ");
            }
            int rTag = annotationr.tag();

            if (lTag > rTag) {
              return 1;
            } else {
              return -1;
            }
          }
        });
    return filedList;
  }

 /* public static <T> T decodeFrom(T instance, ByteBuf data) {

    return new ProtobufDecoder(data).decodeFrom0(instance);
  }*/

  public static <T> T decodeFrom(T instance, byte[] data) {
    ByteBuf buf = Unpooled.directBuffer();
    buf.writeBytes(data);
    return new ProtobufDecoder(buf).decodeFrom0(instance);
  }

  private <T> T decodeFrom0(T instance) {

    if (instance == null) {
      throw new RuntimeException("instanceTarget is null");
    }

    try {
      for (Field field : getSortedField(instance.getClass().getFields())) {
        Tag annotation = field.getAnnotation(Tag.class);
        if (annotation == null) {
          throw new RuntimeException("a field " + field.getName() + " without @Tag");
        }
        int tag = annotation.tag();
        Class<?> type = field.getType();
        HeadData headData = getHeadDataByTag(tag);
        if (headData != null) {

          Object obj = readCurrentObject(headData, annotation);

          if (type.isAssignableFrom(Byte.class) || type.isAssignableFrom(byte.class)) {
            if (obj != null) {
              field.set(instance,castToByte(obj));
            }

          } else if (type.isAssignableFrom(Integer.class) || type.isAssignableFrom(int.class)) {
            if (obj != null) {
              field.set(instance, castToInt(obj));
            }
          } else if (type.isAssignableFrom(Long.class) || type.isAssignableFrom(long.class)) {
            if (obj != null) {
				field.set(instance, castToLong(obj));
            }
          } 
		  else if (type.isAssignableFrom(Boolean.class) || type.isAssignableFrom(boolean.class)) {

            if (obj != null) {
              field.set(instance, castToInt(obj) == 1);
            }

          } else if (type.isAssignableFrom(Float.class) || type.isAssignableFrom(float.class)) {
            if (obj != null) {
              field.set(instance, (float) obj);
            }

          } else if (type.isAssignableFrom(Double.class) || type.isAssignableFrom(double.class)) {
            if (obj != null) {
              field.set(instance, (double) obj);
            }

          } else if (type.isArray()) {

            if (obj != null) {
              field.set(instance, obj);
            }
          } else if (type.isAssignableFrom(String.class)) {
            if (obj != null) {
              field.set(instance, new String((byte[]) obj));
            }
          }else if (type.isEnum()) {
			  if (obj != null) {
				  field.set(instance,getEnumTagByValue(type,castToInt(obj)));
			  }
          }
		  else if (type.isAssignableFrom(List.class)) {
            Type genericType = field.getGenericType();
            if (genericType instanceof ParameterizedType) {
              ParameterizedType pt = (ParameterizedType) genericType;
              Class<?> actualTypeArgument = (Class<?>) pt.getActualTypeArguments()[0];
              List<Object> list = new ArrayList<>();

              while (headData != null) {

                if (actualTypeArgument.isAssignableFrom(Byte.class)
                    || actualTypeArgument.isAssignableFrom(byte.class)) {
                  list.add((byte) (long) obj);
                } else if (actualTypeArgument.isAssignableFrom(Integer.class)
                    || actualTypeArgument.isAssignableFrom(int.class)) {
                  ByteReader byteReader = new ByteReader((byte[]) obj);
                  while (byteReader.readableBytes() > 0) {
                    if (annotation.isSigned()) {
                      list.add(byteReader.readSignedVarInt());
                    } else {
                      list.add(byteReader.readUnsignedVarInt());
                    }
                  }

                } else if (actualTypeArgument.isAssignableFrom(Long.class)
                    || actualTypeArgument.isAssignableFrom(long.class)) {
                  ByteReader byteReader = new ByteReader((byte[]) obj);
                  while (byteReader.readableBytes() > 0) {
                    if (annotation.isSigned()) {
                      list.add(byteReader.readSignedVarLong());
                    } else {
                      list.add(byteReader.readUnsignedVarLong());
                    }
                  }
                } else if (actualTypeArgument.isAssignableFrom(Boolean.class)
                    || actualTypeArgument.isAssignableFrom(boolean.class)) {
                  ByteReader byteReader = new ByteReader((byte[]) obj);
                  while (byteReader.readableBytes() > 0) {
                    list.add(byteReader.readByte() == 1);
                  }
                } else if (actualTypeArgument.isAssignableFrom(String.class)) {
                  if (obj != null) {
                    list.add(new String((byte[]) obj));
                  }
                }else if (actualTypeArgument.isEnum())
				{
					//note: not sure if this is correct
					ByteReader byteReader = new ByteReader((byte[]) obj);
					while (byteReader.readableBytes() > 0) {
						//list.add(byteReader.readByte() == 1);
						list.add(getEnumTagByValue(actualTypeArgument,byteReader.readByte()));
					}
				}
				else {
                  Object t =
                      ProtobufDecoder.decodeFrom(actualTypeArgument.newInstance(), (byte[]) obj);
                  list.add(t);
                }
                headData = getHeadDataByTag(tag);
                obj = readCurrentObject(headData, annotation);
              }
              field.set(instance, list);
            } else {
              throw new RuntimeException("un expacted erro");
            }
          } else {

            Object t = ProtobufDecoder.decodeFrom(type.newInstance(), (byte[]) obj);
            if (t != null) {
              field.set(instance, t);
            }
          }
        }
      }
      this.bb.release();
      return instance;
    } catch (Exception e) {
      e.printStackTrace();
      return null;
    } 
  }

	private Object getEnumTagByValue(Class<?> type, int value) throws NoSuchMethodException, SecurityException, InvocationTargetException, IllegalArgumentException, IllegalAccessException
  {
	  for(Field field:type.getDeclaredFields()){
		  Tag enumTag=field.getAnnotation(Tag.class);
		  if(enumTag==null){
			  throw new RuntimeException("enum must use @Tag");
		  }
		  if(enumTag.tag()==value){
		     return type.getDeclaredMethod("valueOf", String.class).invoke(null, field.getName());
		  }
	  }
	  throw new RuntimeException("cannot find enum field for value "+value);
  }

  private long castToLong(Object obj)
  {
	  if(obj instanceof Long){
		  return ((Long)obj).longValue();
	  }else if(obj instanceof Integer){
		  return ((Integer)obj).longValue();
	  }
	  throw new RuntimeException("unknow type "+obj.getClass()+" castting for long");
  }

  private int castToInt(Object obj)
  {
	  if(obj instanceof Long){
		  return ((Long)obj).intValue();
	  }else if(obj instanceof Integer){
		  return ((Integer)obj).intValue();
	  }
	  throw new RuntimeException("unknow type "+obj.getClass()+" castting for int");
  }

  private byte castToByte(Object obj)
  {
	  if(obj instanceof Long){
		  return ((Long)obj).byteValue();
	  }else if(obj instanceof Integer){
		  return ((Integer)obj).byteValue();
	  }
	  throw new RuntimeException("unknow type "+obj.getClass()+" castting for byte");
  }

  private Object readCurrentObject(HeadData headData, Tag annotation) {
    if (headData == null) {
      return null;
    }
    Object obj;
    if (headData.getType() == HeadData.TYPE_NUMBER_64) {
      if (annotation.isFloat()) {
        obj = bb.readDoubleLE();
      }
      else if (annotation.isSigned()) {
        obj = bb.readLongLE();
      } else {
		  byte[] tmp=new byte[8];
		  bb.readBytes(tmp);
		  flipBytes(tmp);
          obj = Long.parseUnsignedLong(Util.bytesToHex(tmp),16);
      }
    } else if (headData.getType() == HeadData.TYPE_NUMBER_32) {
      if (annotation.isFloat()) {
        //int y = bb.readerIndex();
        float f = bb.readFloatLE();
        obj = f;
      } else if (annotation.isSigned()) {
        obj = bb.readIntLE();
      } else {
        obj = bb.readUnsignedIntLE();
      }
    } else if (headData.getType() == HeadData.TYPE_NUMBER) {
      if (annotation.isSigned()) {
        obj = readCurrentSignedVarLong();
      } else {
        obj = readCurrentUnsignedVarLong();
      }
    } else if (headData.getType() == HeadData.TYPE_BYTES) {
      obj = readCurrentBytes();
    } else {
      throw new UnsupportedOperationException("未知类型 " + headData.getType());
    }
    return obj;
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

  /*
  private int readCurrentUnsignedVarInt() {
    return readUnsignedVarInt();
  }

  private int readCurrentSignedVarInt() {
    return readSignedVarInt();
  }
  */

  private long readCurrentUnsignedVarLong() {
    return readUnsignedVarLong();
  }

  private long readCurrentSignedVarLong() {
    return readSignedVarLong();
  }

  private class HeadData {
    public static final int TYPE_NUMBER = 0;

    public static final int TYPE_NUMBER_64 = 1;

    public static final int TYPE_NUMBER_32 = 5;

    public static final int TYPE_BYTES = 2;

    private long tag;

    private long type;

    public HeadData(long tag, long type) {
      this.tag = tag;
      this.type = type;
    }

    public long getTag() {
      return tag;
    }

    public long getType() {
      return type;
    }
  }

  String decimalToBinaryString(int number) {
    return String.format("%16s", Integer.toString(number, 2)).replace(' ', '0');
  }

  public byte[] readBytesByTag(int tag, byte[] defualt) {

    HeadData headData = getHeadDataByTag(tag);
    if (headData == null) {

      return defualt;
    } else if (headData.getType() != HeadData.TYPE_BYTES) {

      return defualt;
    }
    return readCurrentBytes();
  }

  private byte[] readCurrentBytes() {
    if (bb.readableBytes() == 0) {
      return null;
    }
    return readBytes(readUnsignedVarInt());
  }

  public int readSignedIntByTag(int tag, int defualt) {

    HeadData headData = getHeadDataByTag(tag);

    if (headData == null) {

      return defualt;
    } else if (headData.getType() == HeadData.TYPE_NUMBER) {

      return defualt;
    }
    return readSignedVarInt();
  }

  public long readSignedLongByTag(int tag, long defualt) {

    HeadData headData = getHeadDataByTag(tag);

    if (headData == null) {

      return defualt;
    } else if (headData.getType() == HeadData.TYPE_NUMBER) {

      return defualt;
    }
    return readSignedVarLong();
  }

  public int readUnsignedIntByTag(int tag, int defualt) {

    HeadData headData = getHeadDataByTag(tag);

    if (headData == null) {

      return defualt;
    } else if (headData.getType() == HeadData.TYPE_NUMBER) {

      return defualt;
    }
    return readUnsignedVarInt();
  }

  public long readUnsignedLongByTag(int tag, long defualt) {

    HeadData headData = getHeadDataByTag(tag);

    if (headData == null) {

      return defualt;
    } else if (headData.getType() == HeadData.TYPE_NUMBER) {

      return defualt;
    }
    return readUnsignedVarLong();
  }

  private HeadData getHeadDataByTag(int tag) {
    int readerIndex = bb.readerIndex();
    while (bb.readableBytes() > 0) {
      HeadData headData = readHeadData();

      if (headData.getTag() == tag) {
        return headData;
      } else {
        skipField(headData);
      }
    }
    bb.readerIndex(readerIndex);
    return null;
  }

  private void skipField(HeadData headData) {
    if (bb.readableBytes() == 0) {
      return;
    }
    switch ((int) headData.getType()) {
      case HeadData.TYPE_NUMBER:
        {
          readSignedVarLong();
        }
        break;
      case HeadData.TYPE_NUMBER_64:
        {
          readBytes(8);
        }
        break;
      case HeadData.TYPE_NUMBER_32:
        {
          readBytes(4);
        }
        break;
      case HeadData.TYPE_BYTES:
        {
          readBytes(readUnsignedVarLong());
        }
        break;
      default:
        {
          throw new RuntimeException(
              "unknown type "
                  + headData.getType()
                  + " readerIndex="
                  + bb.readerIndex()
                  + " "
                  + bb.readerIndex(0)
                  + " "
                  + Util.bytesToHex((Util.bufToBytes(bb))));
        }
    }
  }

  private byte[] readBytes(long size) {
    byte[] temp = new byte[(int) size];
    bb.readBytes(temp);
    return temp;
  }

  private HeadData readHeadData() {
    long head = readUnsignedVarInt();
    HeadData headData = new HeadData(head >> 3, head & 0b0000_0111);
    return headData;
  }

  private long readSignedVarLong() {
    long raw = readUnsignedVarLong();
    long temp = (((raw << 63) >> 63) ^ raw) >> 1;
    return temp ^ (raw & (1L << 63));
  }

  private long readUnsignedVarLong() {
    long value = 0L;
    int i = 0;
    long b;
    while (((b = bb.readByte()) & 0x80L) != 0) {
      value |= (b & 0x7F) << i;
      i += 7;
    }
    return value | (b << i);
  }

  private int readSignedVarInt() {
    int raw = readUnsignedVarInt();
    int temp = (((raw << 31) >> 31) ^ raw) >> 1;
    return temp ^ (raw & (1 << 31));
  }

  private int readUnsignedVarInt() {
    int value = 0;
    int i = 0;
    int b;
    while (((b = bb.readByte()) & 0b1000_0000) != 0) {
      value |= (b & 0x7F) << i;
      i += 7;
    }
    return value | (b << i);
  }
}
