package com.sobinary.clockplus.base;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream; 
import java.io.ObjectOutputStream;

import android.content.Context;
import android.content.res.AssetManager;

public class IO 
{
  
	public static void printStream(InputStream in)
	{
		BufferedReader reader = new BufferedReader( new InputStreamReader(in) );
		String line;
		try{
			while( (line=reader.readLine()) != null ) Core.print(line);
		}
		catch(Exception e){
			Core.printe("[printStream]ERROR PRINTING STREAM", e);
		}
	}

	public static Object load(String fname , Context cont)
	{
		try
		{
			FileInputStream fis = cont.openFileInput(fname);
			ObjectInputStream ois = new ObjectInputStream(fis);
			Object loadedObject = ois.readObject();
			ois.close();
			fis.close();
			return loadedObject;
		}
		catch(Exception e)
		{
			Core.print("[IO.load]Load "+fname+" failed: "+ e.getMessage() );
			return null;
		}
	}

	public static InputStream aLoad(Context cont, String aname)
	{
	    AssetManager assetManager = cont.getAssets();
	    try 
	    {
	        return assetManager.open(aname);
	    } 
	    catch (IOException e) 
	    {
	    	Core.print("Bad asset: " + aname);
	        return null;
	    }
	    
	}
	
	public static boolean save(String fname , Object o , Context cont)
	{
		try
		{
			if(o == null) Core.print("WARNING: saving null");
			FileOutputStream fos = cont.openFileOutput(fname , Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(o);
			oos.close();
			fos.close(); 
			return true;
		}
		catch(Exception e)
		{
			Core.printe("[IO.save]Error saving object: ", e );
			return false;
		}
	}
	
	public static File saveToFile(String fname, Object o)
	{
		try
		{
			File file = new File(fname);
			byte[] buffer = serialize( o );
			FileOutputStream fos = new FileOutputStream( file );
			fos.write( buffer );
			fos.close();
			Core.print("SUCCESS saving to path: "+ fname );
			return file;
		}
		catch(Exception e)
		{
			Core.printe("[IO.saveToFile]Error saving file: ", e );
			return null;
		}
	}

	public static byte[] serialize(Object o)
	{
		try
		{
			ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
			ObjectOutputStream objOut = new ObjectOutputStream( byteOut );
			objOut.writeObject(o);

			byte [] result = byteOut.toByteArray();
			Core.print("Serialize success");
			
			objOut.close();
			byteOut.close();

			return result;
		}
		catch(Exception e)
		{
			Core.print("Error serializing: "+e.getMessage() );
			return null;
		}
		
	}

    public static CharSequence readEula(Context cont) 
    {
        BufferedReader in = null;
        try 
        {
            in = new BufferedReader(new InputStreamReader(cont.getAssets().open("EULA")));
            String line;
            StringBuilder buffer = new StringBuilder();
            while ((line = in.readLine()) != null) buffer.append(line).append('\n');
            Core.print("Success reading eula");
            return buffer;
            
        } 
        catch (IOException e) 
        {
        	Core.printe(e);
            return "";
        } 
        catch (Exception e) 
        {
        	Core.printe(e);
            return "";
        } 

        finally 
        {
            closeStream(in);
        }
    }
    
    private static void closeStream(Closeable stream) {
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // Ignore
            }
        }
    }


}
