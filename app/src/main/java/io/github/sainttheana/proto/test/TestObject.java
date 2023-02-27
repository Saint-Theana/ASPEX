package io.github.sainttheana.proto.test;
import io.github.sainttheana.proto.core.Tag;
import java.util.ArrayList;
import java.util.List;

public class TestObject
{
	public enum EnumTest
	{
		@Tag(tag=1)Value1,
		@Tag(tag=2)Value2,
		@Tag(tag=3)Value3,
		@Tag(tag=4)Value4,
		@Tag(tag=5)Value5,
		@Tag(tag=6)Value6,
		@Tag(tag=7)Value7;
		
	}
	
	//string
	@Tag(tag=1) public String a= null;

	//repeated string
	@Tag(tag=2) public List<String> b= new ArrayList<>();

	//uint32
	@Tag(tag=3) public Integer c= null;

	//uint64
	@Tag(tag=4) public Long d= null;

	//double
	@Tag(tag=5,isFloat=true) public Double e= null;

	//float
	@Tag(tag=6,isFloat=true) public Float f= null;

	//int32|sint32
	@Tag(tag=7,isSigned=true) public Integer g= null;

	//int64|sint64
	@Tag(tag=8,isSigned=true) public Long h= null;

	//fixed32
	@Tag(tag=9,isFixed=true) public Integer i= null;

	//fixed64
	@Tag(tag=10,isFixed=true) public Long j= null;

	//sfixed32
	@Tag(tag=11,isFixed=true,isSigned=true) public Integer k= null;

	//sfixed64
	@Tag(tag=12,isFixed=true,isSigned=true) public Long l= null;

	//repeated sfixed32
	@Tag(tag=13,isFixed=true,isSigned=true) public List<Integer> m= new ArrayList<>();

	//repeated sfixed64
	@Tag(tag=14,isFixed=true,isSigned=true) public List<Long> n= new ArrayList<>();

	//bytes
	@Tag(tag=15) public byte[] o= null;

	//embed message
	@Tag(tag=16) public EmbedMessage p= null;

	//repeated embed message
	@Tag(tag=17) public List<EmbedMessage> q= new ArrayList<>();

	//enum
	@Tag(tag=18) public EnumTest r= null;

	//repeated enum
	@Tag(tag=19) public List<EnumTest> s= new ArrayList<>();
	
	public static class EmbedMessage
	{
		@Tag(tag=1) public String a= null;
		
		@Tag(tag=2) public List<String> b= new ArrayList<>();

		@Tag(tag=3) public Integer c= null;

		@Tag(tag=4) public Long d= null;
	}

}
