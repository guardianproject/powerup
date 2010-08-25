package info.guardianproject.installer;

import java.io.File;


import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class GuardianInstaller extends Activity {
	
	TextView log;
	StringBuilder logBuffer = new StringBuilder();
	APKInstaller apkInstaller = null;
	
		  // Need handler for callbacks to the UI thread
	    final Handler mHandler = new Handler();

	    // Create runnable for posting
	    final Runnable mUpdateLog = new Runnable() {
	        public void run() {
	        	
	        	synchronized (logBuffer)
	        	{
	        		log.append(logBuffer.toString());
	        	
	        		logBuffer.delete(0,logBuffer.length());
	        	}
	        }
	    };

	    
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        
    }

    public void logMessage (String msg)
    {
    	
    	
    	logBuffer.append(msg);
    	logBuffer.append("\n");
    	
    	mHandler.post(mUpdateLog);
    	
    	
    }
    
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        
        MenuItem mItem = null;
       
        mItem = menu.add(0, 4, Menu.NONE, "INSTALL");
        mItem.setIcon(R.drawable.icon);
       
        
       
        return true;
    }
    
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		
		super.onMenuItemSelected(featureId, item);
	
		apkInstaller.installAPKs();
	
		return true;
		
		
	}
    
	@Override
	protected void onResume() {
		
		super.onResume();
		 setContentView(R.layout.main);
		 log = (TextView)findViewById(R.id.messageLog);
		 
		 Thread t = new Thread() {
	            public void run() {
	                startInstall();
	                
	                
	            }
	        };
	        t.start();
		    
		
		 
	}
	
	private void startInstall ()
	{
		
		File sdDir = Environment.getExternalStorageDirectory();

		String apkPath = "/data/app/info.guardianproject.installer.apk";
		int i = 1;
		
		while (!new File(apkPath).exists())
		{
			apkPath = "/data/app/info.guardianproject.installer-" + i++ + ".apk";
		}
		
		apkInstaller = new APKInstaller(sdDir,apkPath, this);
		
		apkInstaller.start(true);
		
	}
    
    
}