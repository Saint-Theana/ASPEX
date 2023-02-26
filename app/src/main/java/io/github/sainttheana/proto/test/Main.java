package io.github.sainttheana.proto.test;
import io.github.sainttheana.proto.core.ProtobufEncoder;
import io.github.sainttheana.proto.util.Util;
import io.github.sainttheana.proto.core.ProtobufDecoder;

public class Main
{
	public static void main(String[] args){
		TestObject obj=new TestObject();
		obj.a="aaa";
		obj.b=Integer.MAX_VALUE;
		obj.c=Double.MAX_VALUE;
		obj.d=Long.MAX_VALUE;
		obj.e=Float.MAX_VALUE;
		System.out.println(obj.a);
		System.out.println(obj.b);
		System.out.println(obj.c);
		System.out.println(obj.d);
		System.out.println(obj.e);
		byte[] o =new ProtobufEncoder(obj).toByteArray();
		System.out.println(Util.byteArrayToHexStringWithoutBlank(o));
		
		TestObject objNew=ProtobufDecoder.decodeFrom(new TestObject(),o);
		System.out.println(objNew.a);
		System.out.println(objNew.b);
		System.out.println(objNew.c);
		System.out.println(objNew.d);
		System.out.println(objNew.e);
		
	}
}
