package datamining.machinelearningprojectsminer.miner.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class GHUtil {

    public static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");	

	public static Date parseDateTime(String dateTimeStr) {
			try {
					return simpleDateFormat.parse(dateTimeStr);
			} catch (ParseException e) {
					throw new RuntimeException("Failed to parse date/time string: " + dateTimeStr, e);
			}
	}	

	public static enum BestTokenOption {
		REMAING, DATE
	}
	
	public static GHLimitRate getLimitRate(String token){
		GHLimitRate limitRate = null;
		try {
			String entryPoint = String.format("https://api.github.com/rate_limit");
			URL url = new URL(entryPoint);
			
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();															
			conn.addRequestProperty("Authorization", "Bearer " + token);

			if(conn.getResponseCode() != 404) {							
				BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
				Gson gson = new Gson();
				JsonObject json = gson.fromJson(reader, JsonObject.class);
				
				int remaining = json.getAsJsonObject("rate")									
									.get("remaining").getAsInt();
				int used = json.getAsJsonObject("rate")									
									.get("used").getAsInt();
				int limit = json.getAsJsonObject("rate")									
									.get("limit").getAsInt();
				long reset = json.getAsJsonObject("rate")									
									.get("reset").getAsLong();

				limitRate = new GHLimitRate(limit, used, remaining, reset * 1000);				
			}			
		} catch (IOException e) {			
			e.printStackTrace();
		}		
		return limitRate;
	}

    public static GHToken bestToken(List<GHToken> tokens, BestTokenOption option) {		
		updateTokens(tokens);
		switch (option) {
		case REMAING:
			Collections.sort(tokens, (t1, t2) -> t2.getRemaining().compareTo(t1.getRemaining()));			
			break;
		case DATE:
			Collections.sort(tokens, (t1, t2) -> t1.getResetDate().compareTo(t2.getResetDate()));
			break;
		}		
		return tokens.get(0);
	}

	public static void updateTokens(List<GHToken> tokens) {		
		for (GHToken token : tokens) {
			try {
				GHLimitRate limitRate = getLimitRate(token.getToken());
				token.setRemaining(limitRate.getRemaining());
				token.setResetDate(limitRate.getResetDate());
				System.out.println(token);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}
		}
	}	
}