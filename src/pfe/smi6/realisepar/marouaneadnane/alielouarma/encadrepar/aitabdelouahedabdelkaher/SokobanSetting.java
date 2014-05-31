package pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher;


import android.app.Activity;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class SokobanSetting extends Activity {
	private static MediaPlayer soundHome;

	public static void startPlayingSound(Context con)
	{
		soundHome= MediaPlayer.create(con, R.raw.soundhome);
		soundHome.start();
	}




	public static void stopPlayingSound(OnClickListener onClickListener)
	{
		if(!soundHome.isPlaying())
		{
			soundHome.stop();
			soundHome = null;
		}
	}

	
}