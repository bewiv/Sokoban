package pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher;

import java.util.ArrayList;
import java.util.List;

import pfe.smi6.realisepar.marouaneadnane.alielouarma.encadrepar.aitabdelouahedabdelkaher.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class SokobanLevelMenuActivity extends Activity {
	
	private static final String MAX_LEVEL_NAME = "max_level";
	public static final String SHARED_PREFS_NAME = "game_prefs";
	static	int series=-1;
	public static String getMaxLevelPrefName(int levelSetIndex) {
		// historical compat: first level == no suffix
		return MAX_LEVEL_NAME + (levelSetIndex == 0 ? "" : ("_" + levelSetIndex));
	}

	public void onButtonClicked(View view) {
		int index = -1;
		switch (view.getId()) {
		case R.id.levelsTheMeltdownButton:
			index = 1;
			break;
		case R.id.levelsDownOfTheDinosaursButton:
			index = 2;
			break;
		case R.id.levelsContinentalDriftButton:
			index = 3;
			break;
		case R.id.levelsiceageButton:
			index = 0;
			break;
		}

		final int levelSetIndex = index;
		series = levelSetIndex;
		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		final String maxLevelNamePref = getMaxLevelPrefName(levelSetIndex);
		final int maxLevel = Math.min(prefs.getInt(maxLevelNamePref, 1),SokobanLevels.levelMaps.get(levelSetIndex).length);

		if (maxLevel == 1) {
			Intent intent = new Intent();
			intent.putExtra(SokobanGameActivity.GAME_LEVEL_INTENT_EXTRA, 0);
			intent.putExtra(SokobanGameActivity.GAME_LEVEL_SET_EXTRA, levelSetIndex);
			intent.putExtra(SokobanGameActivity.SHOW_HELP_INTENT_EXTRA, true);
			intent.setClass(this, SokobanGameActivity.class);
			startActivity(intent);
		} else {
			List<String> levelList = new ArrayList<String>(maxLevel);
			for (int i = maxLevel; i > 0; i--) {
				levelList.add("Level " + i);
			}
			final String[] items = levelList.toArray(new String[maxLevel]);
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Choose level");
			builder.setItems(items, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int item) {
					Intent intent = new Intent();
					int levelClicked = maxLevel - item - 1;
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_SET_EXTRA, levelSetIndex);
					intent.putExtra(SokobanGameActivity.GAME_LEVEL_INTENT_EXTRA, levelClicked);
					intent.setClass(SokobanLevelMenuActivity.this, SokobanGameActivity.class);
					startActivity(intent);
				}
			});
			builder.create().show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.levelsets);
	}

	@Override
	protected void onResume() {
		super.onResume();
		setButtonText(R.id.levelsTheMeltdownButton, 0);
		setButtonText(R.id.levelsDownOfTheDinosaursButton, 1);
		setButtonText(R.id.levelsContinentalDriftButton, 2);
		setButtonText(R.id.levelsiceageButton, 3);
	}

	private void setButtonText(int buttonId, int levelSetIndex) {
		Button button = (Button) findViewById(buttonId);
		String buttonText = button.getText().toString();
		if (buttonText.contains("-")) {
			buttonText = buttonText.split("-")[0].trim();
		}

		SharedPreferences prefs = getSharedPreferences(SHARED_PREFS_NAME, MODE_PRIVATE);
		final String maxLevelNamePref = getMaxLevelPrefName(levelSetIndex);
		final int maxLevel = Math.min(prefs.getInt(maxLevelNamePref, 1), SokobanLevels.levelMaps.get(levelSetIndex).length);
		int availableLevels = SokobanLevels.levelMaps.get(levelSetIndex).length;
		button.setText(buttonText + " - " + maxLevel + "/" + availableLevels);
	}

}
