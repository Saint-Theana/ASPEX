package io.github.sainttheana.proto.test;
import io.github.sainttheana.proto.core.Tag;

public class TestObject
{
	@Tag(tag=1) public String a= null;
	
	@Tag(tag=2) public Integer b= null;
	
	@Tag(tag=3,isFloat=true) public Double c= null;
	
	@Tag(tag=4) public Long d= null;
	
	@Tag(tag=5,isFloat=true) public Float e= null;
}
