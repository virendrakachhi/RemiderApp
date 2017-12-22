package com.attendanceapp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class MobileConnectivity {
	public boolean BlueToothConnectionStatus = false;
	public boolean DummyConnectionStatus = false;
	public boolean EthernetConnectionStatus = false;
	public boolean MobileConnectionStatus = false;
	public boolean MobileDUNConnectionStatus = false;
	public boolean MobileHIPRIConnectionStatus = false;
	public boolean MobileMMSConnectionStatus = false;
	public boolean MobileSUPLConnectionStatus = false;
	public boolean WimaxConnectionStatus = false;
	public boolean WiFiConnectionStatus = false;
	public boolean isIntenetConnectionactive = false;

	public boolean isIntenetConnectionactive() {
		return isIntenetConnectionactive;
	}

	public void setIntenetConnectionactive(boolean isIntenetConnectionactive) {
		this.isIntenetConnectionactive = isIntenetConnectionactive;
	}

	public boolean isBlueToothConnectionStatus() {
		return BlueToothConnectionStatus;
	}

	public void setBlueToothConnectionStatus(boolean blueToothConnectionStatus) {
		BlueToothConnectionStatus = blueToothConnectionStatus;
	}

	public boolean isDummyConnectionStatus() {
		return DummyConnectionStatus;
	}

	public void setDummyConnectionStatus(boolean dummyConnectionStatus) {
		DummyConnectionStatus = dummyConnectionStatus;
	}

	public boolean isEthernetConnectionStatus() {
		return EthernetConnectionStatus;
	}

	public void setEthernetConnectionStatus(boolean ethernetConnectionStatus) {
		EthernetConnectionStatus = ethernetConnectionStatus;
	}

	public boolean isMobileConnectionStatus() {
		return MobileConnectionStatus;
	}

	public void setMobileConnectionStatus(boolean mobileConnectionStatus) {
		MobileConnectionStatus = mobileConnectionStatus;
	}

	public boolean isMobileDUNConnectionStatus() {
		return MobileDUNConnectionStatus;
	}

	public void setMobileDUNConnectionStatus(boolean mobileDUNConnectionStatus) {
		MobileDUNConnectionStatus = mobileDUNConnectionStatus;
	}

	public boolean isMobileHIPRIConnectionStatus() {
		return MobileHIPRIConnectionStatus;
	}

	public void setMobileHIPRIConnectionStatus(
			boolean mobileHIPRIConnectionStatus) {
		MobileHIPRIConnectionStatus = mobileHIPRIConnectionStatus;
	}

	public boolean isMobileMMSConnectionStatus() {
		return MobileMMSConnectionStatus;
	}

	public void setMobileMMSConnectionStatus(boolean mobileMMSConnectionStatus) {
		MobileMMSConnectionStatus = mobileMMSConnectionStatus;
	}

	public boolean isMobileSUPLConnectionStatus() {
		return MobileSUPLConnectionStatus;
	}

	public void setMobileSUPLConnectionStatus(boolean mobileSUPLConnectionStatus) {
		MobileSUPLConnectionStatus = mobileSUPLConnectionStatus;
	}

	public boolean isWimaxConnectionStatus() {
		return WimaxConnectionStatus;
	}

	public void setWimaxConnectionStatus(boolean wimaxConnectionStatus) {
		WimaxConnectionStatus = wimaxConnectionStatus;
	}

	public boolean isWiFiConnectionStatus() {
		return WiFiConnectionStatus;
	}

	public void setWiFiConnectionStatus(boolean wiFiConnectionStatus) {
		WiFiConnectionStatus = wiFiConnectionStatus;
	}

	public static MobileConnectivity checkNetworkConnections(Context c) {

		MobileConnectivity info = new MobileConnectivity();

		info.BlueToothConnectionStatus = false;
		info.DummyConnectionStatus = false;
		info.EthernetConnectionStatus = false;
		info.MobileConnectionStatus = false;
		info.MobileDUNConnectionStatus = false;
		info.MobileHIPRIConnectionStatus = false;
		info.MobileMMSConnectionStatus = false;
		info.MobileSUPLConnectionStatus = false;
		info.WimaxConnectionStatus = false;
		info.WiFiConnectionStatus = false;

		ConnectivityManager connec = (ConnectivityManager) c
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		try {

			NetworkInfo network = connec
					.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
			if (network != null) {
				if (network.isConnected()) {
					info.setWiFiConnectionStatus(true);
				}
			}

			network = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
			if (network != null) {
				if (network.isConnected()) {
					info.setMobileConnectionStatus(true);
				}
			}

			network = connec.getNetworkInfo(ConnectivityManager.TYPE_BLUETOOTH);
			if (network != null) {
				if (network.isConnected()) {
					info.setBlueToothConnectionStatus(true);
				}
			}

			network = connec.getNetworkInfo(ConnectivityManager.TYPE_DUMMY);
			if (network != null) {
				if (network.isConnected()) {
					info.setDummyConnectionStatus(true);
				}
			}

			network = connec.getNetworkInfo(ConnectivityManager.TYPE_ETHERNET);
			if (network != null) {
				if (network.isConnected()) {
					info.setEthernetConnectionStatus(true);
				}
			}

			network = connec
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_DUN);
			if (network != null) {
				if (network.isConnected()) {
					info.setMobileDUNConnectionStatus(true);
				}
			}
			network = connec
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_HIPRI);
			if (network != null) {
				if (network.isConnected()) {
					info.setMobileHIPRIConnectionStatus(true);
				}
			}

			network = connec
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_MMS);
			if (network != null) {
				if (network.isConnected()) {
					info.setMobileMMSConnectionStatus(true);
				}
			}
			network = connec
					.getNetworkInfo(ConnectivityManager.TYPE_MOBILE_SUPL);
			if (network != null) {
				if (network.isConnected()) {
					info.setMobileSUPLConnectionStatus(true);
				}
			}
			network = connec.getNetworkInfo(ConnectivityManager.TYPE_WIMAX);
			if (network != null) {
				if (network.isConnected()) {
					info.setWimaxConnectionStatus(true);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		if (info.BlueToothConnectionStatus || info.DummyConnectionStatus
				|| info.EthernetConnectionStatus || info.MobileConnectionStatus
				|| info.MobileDUNConnectionStatus
				|| info.MobileHIPRIConnectionStatus
				|| info.MobileMMSConnectionStatus
				|| info.MobileSUPLConnectionStatus
				|| info.WimaxConnectionStatus || info.WiFiConnectionStatus) {
			info.setIntenetConnectionactive(true);
		} else {
			info.setIntenetConnectionactive(false);
		}

		return info;

	}

}