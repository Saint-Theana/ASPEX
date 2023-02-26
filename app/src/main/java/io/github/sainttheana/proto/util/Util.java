package io.github.sainttheana.proto.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Random;
import java.io.FileOutputStream;

public class Util
{

	public static void writeToFile(byte[] o, File file)
	{
		try
		{
			FileOutputStream out = new FileOutputStream(file);

			out.write(o);
			out.flush();
			out.close();

			
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		
	}
	
	public static byte[] bytesFromFile(File file)
	{

		try
		{
			InputStream in = new FileInputStream(file);

			byte[] data = Util.toByteArray(in);
			in.close();

			return data;
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] bytesFromFile(String file_name)
	{

		try
		{
			InputStream in = new FileInputStream(file_name);

			byte[] data = Util.toByteArray(in);
			in.close();

			return data;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			//e.printStackTrace();
		}
		return null;
	}
	
	private static byte[] toByteArray(InputStream in) throws IOException
	{

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024 * 4];
		int n = 0;
		while ((n = in.read(buffer)) != -1)
		{
			out.write(buffer, 0, n);
		}
		return out.toByteArray();
	}
	
	
	
	
	public static String byteArrayToHexStringWithoutBlank(byte[] bytes)
	{
        String hex= "";
        if (bytes != null)
		{
            for (Byte b : bytes)
			{
                hex += String.format("%02X", b.intValue() & 0xFF);
            }
        }
        return hex;

    }
	
	public static String getRandomString(int length){
		String str="abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
		Random random=new Random();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<length;i++){
			int number=random.nextInt(62);
			sb.append(str.charAt(number));
		}
		return sb.toString();
	}
	
	

}