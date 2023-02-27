package io.github.sainttheana.proto.test;
import io.github.sainttheana.proto.core.ProtobufEncoder;
import io.github.sainttheana.proto.util.Util;
import io.github.sainttheana.proto.core.ProtobufDecoder;
import java.util.Random;
import com.google.gson.GsonBuilder;

public class Main
{
	public static void main(String[] args){
		TestObject obj=new TestObject();
		obj.a="hello world";
		obj.b.add("hello protobuf");
		obj.b.add("hello aspex");
		obj.c=Integer.MAX_VALUE;
		obj.d=Long.MAX_VALUE;
		obj.e=Double.MAX_VALUE;
		obj.f=Float.MAX_VALUE;
		obj.g=-Integer.MAX_VALUE;
		obj.h=-Long.MAX_VALUE;
		obj.i=Integer.MAX_VALUE;
		obj.j=Long.MAX_VALUE;
		obj.k=-Integer.MAX_VALUE;
		obj.l=-Long.MAX_VALUE;
		obj.m.add(-Integer.MAX_VALUE);
		obj.n.add(-Long.MAX_VALUE);
		obj.o=new byte[16];
		new Random().nextBytes(obj.o);
		obj.p=new TestObject.EmbedMessage();
		obj.p.a="hello embed obj";
		obj.p.b.add("hello embed world");
		obj.p.b.add("hello embed town");
		obj.p.b.add("hello embed city");
		obj.q.add(obj.p);
		obj.q.add(obj.p);
		obj.q.add(obj.p);
		obj.r=TestObject.EnumTest.Value1;
		obj.s.add(TestObject.EnumTest.Value1);
		obj.s.add(TestObject.EnumTest.Value1);
		obj.s.add(TestObject.EnumTest.Value4);
		obj.s.add(TestObject.EnumTest.Value5);
		obj.s.add(TestObject.EnumTest.Value1);
		obj.s.add(TestObject.EnumTest.Value4);
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(obj));
		
		byte[] o =new ProtobufEncoder(obj).toByteArray();
		System.out.println(Util.bytesToHex(o));
		
		TestObject objNew=ProtobufDecoder.decodeFrom(new TestObject(),o);
		System.out.println(new GsonBuilder().setPrettyPrinting().create().toJson(objNew));
		
	}
}
