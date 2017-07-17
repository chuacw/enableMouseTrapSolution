package cx.ath.chuacw.MouseTrapEnable;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import android.app.Activity;
import android.os.Bundle;
import android.os.FileObserver;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class MouseTrapEnableSolution extends Activity {
	private static final String TAG = "MouseTrap";
	FileObserver mfs;
	String sDirectory = "/data/data/com.magmamobile.game.mousetrap/shared_prefs/";
	String sFilename = "com.magmamobile.game.mousetrap_preferences.xml";

	@Override
	protected void onResume() {
		super.onResume();
		Log.v(TAG, "onResume");
		startObserver();
	}

	private void createObserver() {
        mfs = new FileObserver(sDirectory, FileObserver.CLOSE_WRITE){
			@Override
			public void onEvent(int event, String path) {
				if ((path!=null)&&(path.contains(sFilename))) {
					mfs.stopWatching();
					removeLastAccess();
					mfs.startWatching();
				}
			}
        };
	}
	
	private void startObserver() {
		if (mfs==null) {
			createObserver();
		}
		mfs.startWatching();
	}
	
	private void stopObserver() {
		mfs.stopWatching();
		mfs = null;
	}
	
	private final void checkFinishing() {
		if (isFinishing()) {
			Log.v(TAG, "stopping Observer");
			stopObserver();
		}
	}
	
	@Override
	protected void onPause() {
		Log.v(TAG, "onPause");
		checkFinishing();
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		Log.v(TAG, "onDestroy");
		checkFinishing();
		super.onDestroy();
	}
	
	@Override
	protected void onStop() {
		Log.v(TAG, "onStop");
		checkFinishing();
		super.onStop();
	}

	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
//        EnableObserver(); // Will be called in onResume
        Button btnEnableSolution = (Button)findViewById(R.id.btnEnableSolution);
        btnEnableSolution.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				removeLastAccess();
			}
		});
    }
    
    private void removeLastAccess() {
		ShellCommand sc = new ShellCommand();
		String sCommand = String.format("chmod 777 %s%s", new Object[] {sDirectory, sFilename});
		sc.su.runWaitFor(sCommand);
		String sFullFilename = String.format("%s%s", new Object[] {sDirectory, sFilename});
		String sTempFilename = String.format("%stemp.xml", new Object[] {sDirectory});
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(sFullFilename));
			String Line = null;
			BufferedWriter bw = new BufferedWriter(new FileWriter(sTempFilename));
			while ((Line=br.readLine())!=null) {
				if (!Line.contains("prefLastAskedSolution")) {
				  bw.write(Line);
				  bw.write("\n");
				}
			}
			bw.close();
			br.close();
			br = null;
			bw = null;
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sGiveFullAccess = String.format("chmod 777 %s", sTempFilename);
		sc.su.runWaitFor(sGiveFullAccess);
		File f = new File(sFullFilename);
		f.delete();
		File f2 = new File(sTempFilename);
		f2.renameTo(f);
		f = null;
		f2 = null;
		sc = null;
    }
}