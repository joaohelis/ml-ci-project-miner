package datamining.machinelearningprojectsminer.miner.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.MalformedJsonException;

import datamining.machinelearningprojectsminer.config.Config;
import datamining.machinelearningprojectsminer.miner.helper.GHUtil.BestTokenOption;

public class GHAPIProcessor {

    private static GHToken token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);

	public static GHToken getToken() {
		return token;
	}

	public static String getReponseFromAPIQueryConnection(HttpURLConnection conn){
		BufferedReader bufferedReader = null;
		String reponseString = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String inputLine;
			StringBuilder response = new StringBuilder();
			while ((inputLine = bufferedReader.readLine()) != null) {
				response.append(inputLine);
			}
			reponseString = response.toString();
			bufferedReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return reponseString;
	}

	public static boolean isValidJson(String json) {
        try {
            JsonElement jsonElement = JsonParser.parseString(json);
            return true; // JSON is valid
        } catch (JsonSyntaxException e) {
            return false; // JSON is invalid
        }
    }

	public static Map<String, Object> ghGraphQlAPIQueryProcessor(String query, Integer maxConnectionTries) {

		String connectionResponse = null;

		int connectionTryCount = 0;
		HttpURLConnection conn = null;
		boolean estabilishedConnection = false;
		
		while(!estabilishedConnection && connectionTryCount < maxConnectionTries){

			estabilishedConnection = false;
			conn = null;
			
			if(token.getRemaining() <= 50){							
				token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
				if(token.getRemaining() <= 50){							
					long sleepTime = token.getResetDate().getTime() - new Date().getTime();
					try {
						System.out.println("All " + Config.gitHubTokens.size() + " tokens has no Remaining Rate - RESET DATE -> " + token.getResetDate());
						System.out.println("Sleeping " + sleepTime/1000/60 + " minute(s) to reset the rate limit...");
						Thread.sleep(sleepTime + 1000);
						token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
						continue;
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			}
			
			String graphqlUrl = "https://api.github.com/graphql";
			
			System.out.println("url: " + graphqlUrl + " - " + new Date());
									
			try {
				connectionTryCount++;

				ExecutorService executorService = Executors.newSingleThreadExecutor();

				int flushTimeOutInSeconds = 35;
				int connectionTimeOutInSeconds = 15;

				Callable<Map<String, Object>> task = () -> {
				    URL url = null;
		            try {
		                url = new URL(graphqlUrl);
		            } catch (MalformedURLException e) {
		                e.printStackTrace();
		            }
				    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
				    connection.setRequestMethod("POST");
				    connection.setRequestProperty("Authorization", "Bearer <TOKEN>".replace("<TOKEN>", token.getToken())); // Replace with your GitHub access token
				    connection.setRequestProperty("Content-Type", "application/json");
				    connection.setDoOutput(true);
					connection.setConnectTimeout(connectionTimeOutInSeconds * 1000);
					OutputStream outputStream = connection.getOutputStream();
					outputStream.write(query.getBytes());
					outputStream.flush();
					outputStream.close();
					
					int connectionResponseCode = connection.getResponseCode();
					
					Map<String, Object> connectionResultMap = new HashMap<>();
					connectionResultMap.put("connection", connection);
					connectionResultMap.put("connectionResponseCode", connectionResponseCode);
					return connectionResultMap;
				};

				Future<Map<String, Object>> future = executorService.submit(task);				

				// Wait for the flush operation to complete or timeout
				boolean flushCompleted = false;
				Integer connectionResponseCode = null;

				try {
				    Map<String, Object> connectionResultMap = future.get(flushTimeOutInSeconds, TimeUnit.SECONDS); // Timeout set
				    conn = (HttpURLConnection) connectionResultMap.get("connection");
					connectionResponseCode = (int) connectionResultMap.get("connectionResponseCode");
					flushCompleted = true;
				}catch (TimeoutException e) {
					System.out.printf("Flush operation timed out (%s sec). ", flushTimeOutInSeconds);
					future.cancel(true); // Cancel the task if it exceeds the timeout										
				}catch (Exception e) {
					// Handle any other exceptions that may occur during the flush operation
					e.printStackTrace();
				}
				
				// Shutdown the executor service
				executorService.shutdown();
				
				if(!flushCompleted){	
					if(connectionTryCount < maxConnectionTries){
						System.out.printf("Try number %d of %d - Retrying...\n", connectionTryCount, maxConnectionTries);
					}else{
						System.out.println("Max number of tries reached. Aborting...");
					}				
					continue;
				}
				    						
				if(conn == null || connectionResponseCode != HttpURLConnection.HTTP_OK){		
					System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
					throw new RuntimeException("Failed: HTTP error code : " + conn.getResponseCode());
				}

				int remaining = Integer.parseInt(conn.getHeaderField("X-RateLimit-Remaining"));
				token.setRemaining(remaining);  

				System.out.println("Connection established with token " + token.getToken() + " - Remaining: " + token.getRemaining());
				estabilishedConnection = true;

				
				connectionResponse = getReponseFromAPIQueryConnection(conn);

				// trying to parse the reponse to json

				JsonObject responseJson = new Gson().fromJson(connectionResponse, JsonObject.class);
				
				if(connectionResponse == null || responseJson.isJsonNull() || !isValidJson(connectionResponse)){								
					throw new MalformedJsonException("Invalid JSON response");					
				}
				
			}catch(MalformedJsonException jsonException){
				System.out.println("Invalid JSON response.");
				if(connectionTryCount < maxConnectionTries){
					System.out.printf("Try number %d of %d - Retrying...\n", connectionTryCount, maxConnectionTries);
				}else{
					System.out.println("Max number of tries reached. Aborting...");
				}
			} catch (Exception e) {							
				System.out.println(e);
				if(maxConnectionTries > 1){
					int sleepTimeInSeconds = 3;
					System.out.printf("Connection failed! Try number %d of %d\n", connectionTryCount, maxConnectionTries);
					System.out.println("Retrying connection in " + sleepTimeInSeconds + " seconds...");
					try {
						Thread.sleep(sleepTimeInSeconds * 100); // 1/2 second sleep
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}						
		}	

		Map<String, Object> result = new HashMap<>();
		result.put("connection", conn);
		result.put("connectionResponse", connectionResponse);		
		return result;
	}

	public static Map<String, Object> ghAPIEntryPointProcessor(String entryPoint, Integer maxConnectionTries, Integer timeout) {

		String connectionResponse = null;

		int connectionTryCount = 0;
		HttpURLConnection conn = null;
		boolean estabilishedConnection = false;
		
		while(!estabilishedConnection && connectionTryCount < maxConnectionTries){

			estabilishedConnection = false;
			conn = null;
			
			if(token.getRemaining() <= 50){							
				token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
				if(token.getRemaining() <= 50){							
					long sleepTime = token.getResetDate().getTime() - new Date().getTime();
					try {
						System.out.println("All " + Config.gitHubTokens.size() + " tokens has no Remaining Rate - RESET DATE -> " + token.getResetDate());
						System.out.println("Sleeping " + sleepTime/1000/60 + " minute(s) to reset the rate limit...");
						Thread.sleep(sleepTime + 1000);
						token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
						continue;
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			}
						
			System.out.println("url: " + entryPoint + " - " + new Date());
									
			try {
				connectionTryCount++;

				ExecutorService executorService = Executors.newSingleThreadExecutor();

				int connectionTimeOutInSeconds = 15;

				if(timeout != null)
					connectionTimeOutInSeconds = timeout;				

				Callable<Map<String, Object>> task = () -> {
				    URL url = null;
		            try {
		                url = new URL(entryPoint);
		            } catch (MalformedURLException e) {
		                e.printStackTrace();
		            }

					HttpURLConnection connection = (HttpURLConnection) url.openConnection();
					connection.setRequestMethod("GET");
					connection.setRequestProperty("Authorization", "Bearer " + token.getToken());
					connection.setRequestProperty("Accept", "application/vnd.github+json");					
														
					int connectionResponseCode = connection.getResponseCode();
					
					Map<String, Object> connectionResultMap = new HashMap<>();
					connectionResultMap.put("connection", connection);
					connectionResultMap.put("connectionResponseCode", connectionResponseCode);
					return connectionResultMap;
				};

				Future<Map<String, Object>> future = executorService.submit(task);				

				// Wait for the flush operation to complete or timeout
				boolean connectionCompleted = false;
				Integer connectionResponseCode = null;

				try {
				    Map<String, Object> connectionResultMap = future.get(connectionTimeOutInSeconds, TimeUnit.SECONDS); // Timeout set
				    conn = (HttpURLConnection) connectionResultMap.get("connection");
					connectionResponseCode = (int) connectionResultMap.get("connectionResponseCode");
					connectionCompleted = true;
				}catch (TimeoutException e) {
					System.out.printf("Connection operation timed out (%s sec). ", connectionTimeOutInSeconds);
					future.cancel(true); // Cancel the task if it exceeds the timeout										
				}catch (Exception e) {
					// Handle any other exceptions that may occur during the flush operation
					e.printStackTrace();
				}
				
				// Shutdown the executor service
				executorService.shutdown();
				
				if(!connectionCompleted){	
					if(connectionTryCount < maxConnectionTries){
						System.out.printf("Try number %d of %d - Retrying...\n", connectionTryCount, maxConnectionTries);
					}else{
						System.out.println("Max number of tries reached. Aborting...");
					}				
					continue;
				}
				    						
				if(conn == null || connectionResponseCode != HttpURLConnection.HTTP_OK){		
					System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
					throw new RuntimeException("Failed: HTTP error code : " + conn.getResponseCode());
				}

				int remaining = Integer.parseInt(conn.getHeaderField("X-RateLimit-Remaining"));
				token.setRemaining(remaining);  

				System.out.println("Connection established with token " + token.getToken() + " - Remaining: " + token.getRemaining());
				estabilishedConnection = true;
				
				connectionResponse = getReponseFromAPIQueryConnection(conn);

				// trying to parse the reponse to json

				JsonObject responseJson = new Gson().fromJson(connectionResponse, JsonObject.class);
				
				if(connectionResponse == null || responseJson.isJsonNull() || !isValidJson(connectionResponse)){								
					throw new MalformedJsonException("Invalid JSON response");					
				}
				
			}catch(MalformedJsonException jsonException){
				System.out.println("Invalid JSON response.");
				if(connectionTryCount < maxConnectionTries){
					System.out.printf("Try number %d of %d - Retrying...\n", connectionTryCount, maxConnectionTries);
				}else{
					System.out.println("Max number of tries reached. Aborting...");
				}
			} catch (Exception e) {							
				System.out.println(e);
				if(maxConnectionTries > 1){
					int sleepTimeInSeconds = 3;
					System.out.printf("Connection failed! Try number %d of %d\n", connectionTryCount, maxConnectionTries);
					System.out.println("Retrying connection in " + sleepTimeInSeconds + " seconds...");
					try {
						Thread.sleep(sleepTimeInSeconds * 100); // 1/2 second sleep
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}						
		}	

		Map<String, Object> result = new HashMap<>();
		result.put("connection", conn);
		result.put("connectionResponse", connectionResponse);		
		return result;
	}	

	public static HttpURLConnection ghAPIEntryPointProcessor(String entryPoint, Integer maxConnectionTries) {
		
		int connectionTryCount = 0;
		HttpURLConnection conn = null;
		boolean stabilishedConnection = false;
		int connectionTimeOutInSeconds = 15;
		
		while(!stabilishedConnection && connectionTryCount < maxConnectionTries){
			
			if(token.getRemaining() <= 50){							
				token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
				if(token.getRemaining() <= 50){							
					long sleepTime = token.getResetDate().getTime() - new Date().getTime();
					try {
						System.out.println("All " + Config.gitHubTokens.size() + " tokens has no Remaining Rate - RESET DATE -> " + token.getResetDate());
						System.out.println("Sleeping " + sleepTime/1000/60 + " minute(s) to reset the rate limit...");
						Thread.sleep(sleepTime + 1000);
						token = GHUtil.bestToken(Config.gitHubTokens, BestTokenOption.REMAING);
						continue;
					} catch (InterruptedException e) {					
						e.printStackTrace();
					}
				}
			}

			URL url = null;
			try {
				url = new URL(entryPoint);
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}					
									
			try {
				connectionTryCount++;
				System.out.println(entryPoint);

				conn = (HttpURLConnection) url.openConnection();
				conn.setRequestMethod("GET");
				conn.setRequestProperty("Authorization", "Bearer " + token.getToken());
				conn.setRequestProperty("Accept", "application/vnd.github+json");
				conn.setConnectTimeout(connectionTimeOutInSeconds * 1000);
				
				int remaining = Integer.parseInt(conn.getHeaderField("X-RateLimit-Remaining"));
				token.setRemaining(remaining);   

				if(conn.getResponseCode() != 200){					
					System.out.println("Failed: HTTP error code : " + conn.getResponseCode());
					throw new RuntimeException("Failed: HTTP error code : " + conn.getResponseCode());
				}

				System.out.println("Connection established with token " + token.getToken() + " - Remaining: " + token.getRemaining());
				stabilishedConnection = true;
			} catch (Exception e) {							
				System.out.println(e);
				if(maxConnectionTries > 1){
					System.out.printf("Connection failed! Try number %d of %d\n", connectionTryCount, maxConnectionTries);
					System.out.println("Retrying connection in 5 seconds...");
					try {
						Thread.sleep(5000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
			}						
		}	
		
		return conn;
	}
    
}