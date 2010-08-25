/* Copyright (c) 2009, Nathan Freitas, Orbot / The Guardian Project - http://openideals.com/guardian */
/* See LICENSE for licensing information */

package info.guardianproject.installer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.util.Log;

public class APKInstaller {

	private final static int FILE_WRITE_BUFFER_SIZE = 40960;
	
	File installPathBase = null;
	String apkPath = null;
	
	private GuardianInstaller gi;
	
	private final static String TAG = "APKInstaller";
	private final static String APK_MIME_TYPE = "application/vnd.android.package-archive";
	//private final static String CONTENT_PATH = "content://com.android.htmlfileprovider";
	
	private final static String ASCII_DIV = "----------------------------------------";
	
	private ArrayList<File> extractedApks;
	
	public APKInstaller (File installPathBase, String apkPath, GuardianInstaller gi)
	{
		this.installPathBase = installPathBase;
		this.apkPath = apkPath;
		this.gi = gi;
		
		extractedApks = new ArrayList<File>();
	}
	
	/*
	 * Start the binary installation if the file doesn't exist or is forced
	 */
	public void start (boolean force)
	{
		
		installFromZip ();
		
	}
	
	/*
	 * Extract the Tor binary from the APK file using ZIP
	 */
	private void installFromZip ()
	{
		
		try
		{
		
			gi.logMessage("Extracting applications from: " + apkPath);
			gi.logMessage(ASCII_DIV);
			
			ZipFile zip = new ZipFile(apkPath);
	
			Enumeration<? extends ZipEntry> enumEntries = zip.entries();
			
			ZipEntry zipen = null;
			
			while (enumEntries.hasMoreElements())
			{
			
				zipen = enumEntries.nextElement();
				
				if (zipen.getName().endsWith("apk"))
				{
				
					String fileName = zipen.getName();
					
					if (fileName.indexOf("/")!=-1)
						fileName = fileName.substring(fileName.indexOf("/")+1);
				
					gi.logMessage("verifying app: " + fileName);
					
					File outFile = new File(installPathBase, fileName);
			        
					gi.logMessage("extracting file to: " + outFile.getAbsolutePath());
						
			        
					streamToFile(zip.getInputStream(zipen),outFile);
					
					
					if (outFile.exists())
					{
						gi.logMessage("Success!");
						extractedApks.add(outFile);
						
					}
					
					gi.logMessage(ASCII_DIV);
					
					
				}
			}
			
			gi.logMessage(extractedApks.size() + " applications extracted and ready to install.\n\n");
			gi.logMessage("**** MENU->'INSTALL' TO CONTINUE ****");
			gi.logMessage(ASCII_DIV);
			
			
			zip.close();
			
			Log.i(TAG,"SUCCESS: unzipped tor, privoxy binaries from apk");
	
		}
		catch (IOException ioe)
		{
			Log.i(TAG,"FAIL: unable to unzip binaries from apk",ioe);
		
		}
	}
	
	
	public void installAPKs ()
	{
		Iterator<File> it = extractedApks.iterator();
		
		while (it.hasNext())
		{
			openAPK(it.next());
		}
	}
	
	private void openAPK (File f)
	{
		Intent intent = new Intent();
	     intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	     intent.setAction(android.content.Intent.ACTION_VIEW);
	     intent.setDataAndType(Uri.fromFile(f), APK_MIME_TYPE);
	     gi.startActivity(intent);

	}
	
	/*
	 * Write the inputstream contents to the file
	 */
    private static void streamToFile(InputStream stm, File outFile)

    {

        FileOutputStream stmOut = null;

        byte[] buffer = new byte[FILE_WRITE_BUFFER_SIZE];

        int bytecount;

        
        try {
            outFile.createNewFile();

        	stmOut = new FileOutputStream(outFile);
        }

        catch (java.io.IOException e)

        {

        	Log.i(TAG,"Error opening output file " + outFile.getAbsolutePath(),e);

        	return;
        }

       

        try

        {

            while ((bytecount = stm.read(buffer)) > 0)

            {

                stmOut.write(buffer, 0, bytecount);

            }

            stmOut.close();
            
            
            stm.close();

        }

        catch (java.io.IOException e)

        {

            Log.i(TAG,"Error writing output file '" + outFile.getAbsolutePath() + "': " + e.toString());

            return;

        }

    }
	
    //copy the file from inputstream to File output - alternative impl
	public void copyFile (InputStream is, File outputFile)
	{
		
		try {
			outputFile.createNewFile();
			DataOutputStream out = new DataOutputStream(new FileOutputStream(outputFile));
			DataInputStream in = new DataInputStream(is);
			
			int b = -1;
			byte[] data = new byte[1024];
			
			while ((b = in.read(data)) != -1) {
				out.write(data);
			}
			
			if (b == -1); //rejoice
			
			//
			out.flush();
			out.close();
			in.close();
			// chmod?
			
			
			
		} catch (IOException ex) {
			Log.e(TAG, "error copying binary", ex);
		}

	}
	

	public ArrayList<File> getExtractedApks() {
		return extractedApks;
	}



}
