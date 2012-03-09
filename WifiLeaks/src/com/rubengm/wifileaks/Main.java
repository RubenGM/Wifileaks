package com.rubengm.wifileaks;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockListActivity;
import com.farproc.wifi.connecter.Wifi;

public class Main extends SherlockListActivity {
	public static final String TAG = "Wifileaks";
	List<ScanResult> results = new ArrayList<ScanResult>();
	WifiAdapter wa = new WifiAdapter();
	Refrescame refrescame;
	private boolean manualReload = true;
	private long timeLastNag = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setListAdapter(wa);
		comienzaRefresco();
		getListView().setDividerHeight(0);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		String clave = Keygen.calc(wa.getItem(position));
		if(clave.equals(Keygen.INCOMPATIBLE)) {
			//TODO: Hacer algo?
		} else if(clave.equals(Keygen.OPEN)) {
			startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		} else {
			conecta(results.get(position));
		}
		super.onListItemClick(l, v, position, id);
	}

	private void conecta(ScanResult r) {
		new Connect().execute(r);
	}

	private class Connect extends AsyncTask<ScanResult, Void, Boolean> {
		ProgressDialog pd;
		@Override
		protected Boolean doInBackground(ScanResult... params) {
			ScanResult r = params[0];

			setPdMessage("Reading wifi config");

			WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		    wifiManager.setWifiEnabled(true);
		    
		    return Wifi.connectToNewNetwork(getApplicationContext(), wifiManager, r, Keygen.calc(r), 10);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if(pd != null) {
				pd.dismiss();
			}
			if(result) {
				Toast.makeText(getApplicationContext(), getString(R.string.conectando), Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(getApplicationContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
			}
			super.onPostExecute(result);
		}

		protected void setPdMessage(final String msg) {
			runOnUiThread(new Runnable() {
				@Override
				public void run() {
					if(pd != null) {
						pd.setMessage(msg);
					}
				}});
			Log.i("WifiConnect", msg);
		}

		@Override
		protected void onPreExecute() {
			pd = new ProgressDialog(Main.this);
			pd.setTitle("Connecting");
			pd.setMessage("Getting ready...");
			pd.show();
			super.onPreExecute();
		}

	}

	private void paraRefresco() {
		if(refrescame != null)
			refrescame.continua = false;
		refrescame = null;
	}

	/**
	 * Lo he dejado de llamar temporalmente hasta que lo hagamos desactivable ;P
	 */
	private void comienzaRefresco() {
		if(refrescame == null) {
			refrescame = new Refrescame();
			refrescame.execute();
		}
		if (refrescame.continua == false) {
			refrescame = new Refrescame();
			refrescame.execute();
		}
	}

	@Override
	protected void onDestroy() {
		paraRefresco();
		super.onDestroy();
	}

	@Override
	protected void onPause() {
		paraRefresco();
		super.onPause();
	}

	@Override
	protected void onResume() {
		//comienzaRefresco();
		super.onResume();
	}

	@Override
	public boolean onCreateOptionsMenu(com.actionbarsherlock.view.Menu menu) {
		menu.add(0, 0, 0, getString(R.string.reload)).setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_WITH_TEXT);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(com.actionbarsherlock.view.MenuItem item) {
		switch(item.getItemId()) {
		case 0:
			refresca();
			manualReload = true;
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	Runnable doRefresca = new Runnable() {
		@Override
		public void run() {
			refresca();
		}
	};

	private class ScanResultComparator implements Comparator<ScanResult>{

		@Override
		public int compare(ScanResult r1, ScanResult r2) {
			String r1calc = Keygen.calc(r1);
			String r2calc = Keygen.calc(r2);

			boolean r1av = !r1calc.equals(Keygen.INCOMPATIBLE);
			boolean r2av = !r2calc.equals(Keygen.INCOMPATIBLE);

			if(r1av && !r2av) return -1;
			else if(!r1av && r2av) return +1;
			else {
				int rank1 = r1.level;
				int rank2 = r2.level;
				if (rank1 > rank2){
					return -1;
				}else if (rank1 < rank2){
					return +1;
				}else{
					return 0;
				}
			}
		}
	}

	private void refresca() {
		WifiManager wifiMgr = (WifiManager) getSystemService(Context.WIFI_SERVICE);
		wifiMgr.startScan();
		List<ScanResult> tmpresults = wifiMgr.getScanResults();
		if(tmpresults == null) tmpresults = new ArrayList<ScanResult>();
		if(manualReload) {
			Collections.sort(tmpresults, new ScanResultComparator());
			results = tmpresults;
			manualReload = false;
		} else {
			for(ScanResult s : tmpresults) {
				boolean found = false;
				int i = 0;
				while(!found && i < results.size()) {
					if(results.get(i).BSSID.equals(s.BSSID)) {
						found = true;
						results.get(i).level = s.level;
					}
					i++;
				}
			}
		}
		((WifiAdapter)getListAdapter()).notifyDataSetChanged();
		if(results.size() == 0) {
			if(System.currentTimeMillis() - timeLastNag > 10000) {
				Toast.makeText(Main.this, "Sin resultados. �Tienes la wifi activa?", Toast.LENGTH_LONG).show();
				timeLastNag = System.currentTimeMillis();
			}
		} 
	}

	private class WifiAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return results.size();
		}

		@Override
		public ScanResult getItem(int position) {
			return results.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ScanResult item = getItem(position);
			View v = null;
			if(convertView != null) {
				v = convertView;
			} else {
				v = View.inflate(Main.this, R.layout.row_wifi, null);
			}
			String key = Keygen.calc(item);
			if(Keygen.INCOMPATIBLE.equals(key)) {
				getBackground(v).setBackgroundResource(R.drawable.background_row_ko);
			} else {
				getBackground(v).setBackgroundResource(R.drawable.background_row);
			}
			getSsid(v).setText(item.SSID + " :: " + item.level + " dBm");
			getBssid(v).setText(item.BSSID);
			getKey(v).setText(key);
			getCapabilities(v).setText(item.capabilities);
			getVal(v).setProgress(calcProgress(v, item.level));
			return v;
		}

		private int calcProgress(View v, int level) {
			int max = getVal(v).getMax();
			int progress = max + level;
			if(progress < 0) progress = 0;
			if(progress > max) progress = max;
			return progress;
		}

		private LinearLayout getBackground(View v) {
			return (LinearLayout)v.findViewById(R.id.linearRow);
		}

		private ProgressBar getVal(View v) {
			return (ProgressBar)v.findViewById(R.id.val);
		}

		private TextView getSsid(View v) {
			return (TextView)v.findViewById(R.id.tvSsid);
		}

		private TextView getCapabilities(View v) {
			return (TextView)v.findViewById(R.id.tvCapabilities);
		}

		private TextView getBssid(View v) {
			return (TextView)v.findViewById(R.id.tvBssid);
		}

		private TextView getKey(View v) {
			return (TextView)v.findViewById(R.id.tvKey);
		}
	}

	private class Refrescame extends AsyncTask<Void, Void, Void> {
		public boolean continua = true;
		@Override
		protected Void doInBackground(Void... params) {
			while(continua) {
				runOnUiThread(doRefresca);
				try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					continua = false;
				}
			}
			return null;
		}

	}
}