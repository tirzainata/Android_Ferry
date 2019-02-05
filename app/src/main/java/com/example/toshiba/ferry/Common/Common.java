package com.example.toshiba.ferry.Common;

import com.example.toshiba.ferry.Remote.IGoogleAPI;
import com.example.toshiba.ferry.Remote.RetrofitClient;

public class Common {
	public static final String baseURL = "https://maps.googleapis.com";
	public static IGoogleAPI getGoogleAPI()
	{
		return RetrofitClient.getRetrofit(baseURL).create(IGoogleAPI.class);
	}

}
