package pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher;


import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.view.View.OnClickListener;


public class SokobanSetting extends Activity {
	private static MediaPlayer soundHome;

	public static void startPlayingSound(Context con)
	{
		soundHome= MediaPlayer.create(con, R.raw.soundhome);
		soundHome.start();
	}




	public static void stopPlayingSound(OnClickListener onClickListener)
	{
		if(soundHome.isPlaying())
		{
			soundHome.release();
			soundHome = null;
		}else{
			soundHome.start();
		}
	}

	
}