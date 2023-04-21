# ASPEX
[![](https://jitpack.io/v/Saint-Theana/ASPEX.svg)](https://jitpack.io/#Saint-Theana/ASPEX)

# Another Software for Protobuf EXperiences

# A Small? Light-Weight? Fast? Protobuf Encoder/Decoder 

# This library might not be as good as you think,use it at your own risk.

# Compiler: [ASPEX-Compiler](https://github.com/Saint-Theana/ASPEX-Compiler)

To add this library:
[jitpack](https://jitpack.io/#Saint-Theana/ASPEX)
```groovy
lallprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
	
dependencies {
	implementation 'com.github.Saint-Theana:ASPEX:1.0.2'
}
```


# How to use

## 1: write a Object yourself

```java
    //treat message as class message xxxx{}-> class xxxx{}
    //every members as fields.
    //almost all basic types were supported.
    int32 -> @Tag(isSigned=true) Integer
    int64 -> @Tag(isSigned=true) Long
    uint32 -> Integer
    uint64 -> Long
    sint32 -> @Tag(isSigned=true) Integer
    sint64 -> @Tag(isSigned=true) Long
    fixed32 -> @Tag(isFixed=true) Integer
    fixed64 -> @Tag(isFixed=true) Long
    sfixed32 -> @Tag(isFixed=true,isSigned=true) Integer
    sfixed64 -> @Tag(isFixed=true,isSigned=true) Long
    bool -> Boolean
    string -> String
    double -> @Tag(isFloat=true) Double 
    float -> @Tag(isFloat=true) Float
    bytes -> byte[]
    enum -> EnumName
    embed message -> ClassName
    
    //some other type
    //repeated does not work on bytes,dont tell me you want that working
    //map and repeated type will both work as list.
    repeated xxx -> List<xxx>
    map<a,b> xxx ->
       public static class AMap{
           @Tag(tag=1) ... a ...
           @Tag(tag=2) ... b ...
       }
       List<AMap> xxx
    //enum class:
    enum xxx{
      xxx =1
      .... =2
    }  ->
    public enum xxx{
        @Tag(tag=1) xxx,
        @Tag(tag=2) ....
    }
    //forget about oneof service extend,these are not working.
    //oneof will be transformed as normal type.
    //you can refer to the source code of the compiler.
    //have fun.
    
    
```

## or use the compiler: [ASPEX-Compiler](https://github.com/Saint-Theana/ASPEX-Compiler)

## 2.encode Object to bytes
```java
    byte[] bytes = new ProtobufEncoder(your object).toByteArray();
```

## 2.decode bytes to Object
```java
    YourObject obj = ProtobufDecoder.decodeFrom(new YourObject(),bytes);
```



## used library:
### netty-bytebuf


## License
```
Copyright (C) 2023  Saint-Theana

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
```
