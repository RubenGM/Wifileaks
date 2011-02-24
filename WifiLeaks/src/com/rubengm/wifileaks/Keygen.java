package com.rubengm.wifileaks;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import android.net.wifi.ScanResult;

public class Keygen {
	/*// THIS CODE IS UNDER THE PUBLIC DOMAIN

function right($string, $chars) 
{ 
	return substr($string, strlen($string) - $chars, $chars); 
}

if (isset($_POST['essid']) && isset($_POST['bssid']))
{
	$bssid = preg_replace('/:/', '', strtoupper(trim($_POST['bssid'])));

	if (strlen($bssid) != 12)
	{
		die('Mete bien la MAC, buen hombre.');
	}

	$essid = strtoupper(trim($_POST['essid']));
	$_ssid = substr($bssid, 0, 8).right($essid, 4);

	die(substr(md5('bcgbghgg'.$_ssid.$bssid), 0, 20));
}

	Sacado de: http://kz.ath.cx/wlan/codigo.txt

	 */
	
	public static String INCOMPATIBLE = "Incompatible";
	private static String calc(String essid, String bssid) {
		if(validEssid(essid)) {
			bssid = cleanBssid(bssid);
			essid = essid.toUpperCase();
			String _ssid = mixSsids(essid, bssid);
			String md5 = md5(_ssid);
			return md5.substring(0, 20);
		} else {
			return INCOMPATIBLE;
		}
	}
	
	private static boolean validEssid(String essid) {
		if(essid.toUpperCase().contains("WLAN_") || essid.toUpperCase().contains("JAZZTEL_")) {
			essid = essid.replace("WLAN_", "").replace("JAZZTEL_", "");
			return essid.length() == 4;
		} else return false;
	}

	public static String calc(ScanResult sr) {
		return calc(sr.SSID, sr.BSSID);
	}

	private static String right(String string, int chars) {
		return string.substring(string.length() - chars);
	}

	private static String cleanBssid(String bssid) {
		return bssid.replace(":", "").trim().toUpperCase();
	}

	private static String mixSsids(String essid, String bssid) {
		return "bcgbghgg" + bssid.substring(0, 8) + right(essid, 4) + bssid;
	}

	private static String md5(String s) {
		try {
			MessageDigest digest = java.security.MessageDigest.getInstance("MD5");
			digest.update(s.getBytes());
			byte messageDigest[] = digest.digest();

			StringBuffer hexString = new StringBuffer();
			for (int i=0; i<messageDigest.length; i++) {
				String tmp = Integer.toHexString(0xFF & messageDigest[i]);
				if(tmp.length() == 0) {
					tmp = "0" + tmp;
				}
				hexString.append(tmp);
			}
			return hexString.toString();

		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return "";
	}
}