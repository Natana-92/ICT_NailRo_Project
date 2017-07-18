package com.javalec.gapi;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class GooglePlace {

	private static final String API_KEY = "AIzaSyDC2VbtwngPu8iC0mla2B2EM3MonDYSeFQ";
	private static final String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
	private static final String TYPE_SEARCH = "/textsearch";
	private static final String OUT_JSON = "/json";

	public GooglePlace() {

	}
	
	/*
	 * Make Url for Google Place API
	 */
	public StringBuilder urlMake(String[] str) {
		HttpURLConnection conn = null;
		StringBuilder jsonResults = new StringBuilder();
		try {
			StringBuilder sb = new StringBuilder(PLACES_API_BASE);
			for (int i = 0; i < str.length; i++) {
				sb.append(str[i]);
			}
			URL url = new URL(sb.toString());
			conn = (HttpURLConnection) url.openConnection();
			InputStreamReader in = new InputStreamReader(conn.getInputStream());
			int read;
			char[] buff = new char[1024];
			while ((read = in.read(buff)) != -1) {
				jsonResults.append(buff, 0, read);
			}
		} catch (Exception e) {
			System.out.println("Url Make Error : " + e);
		} finally {
			if (conn != null) {
				conn.disconnect();
			}
		}
		return jsonResults;
	}

	/*
	 * GoogleAPI Place Search str = 지역정보.
	 */
	public ArrayList<JPlace> search(String str) {
		ArrayList<JPlace> resultList = null;
		String[] url = {TYPE_SEARCH,OUT_JSON,"?query=" + str + "관광명소","&language=" + "ko","&key=" + API_KEY};
		StringBuilder jsonResults = urlMake(url);
		JSONObject jsonObj = new JSONObject(jsonResults.toString());
		JSONArray predsJsonArray = jsonObj.getJSONArray("results");
		resultList = parsingPlaceSearch(predsJsonArray, resultList);

		return resultList;
	};

	/*
	 * place ID 로 reviews, rating 알아내는 메소드.
	 */
	public JPlace detailSearch(String place_id) {
		String[] url = new String[]{"/details",OUT_JSON,"?place_id=" + place_id,"&key=" + API_KEY,"&language=" + "ko"};
		StringBuilder jsonResults = urlMake(url);
		JPlace place = null;

		JSONObject jsonObj = new JSONObject(jsonResults.toString()).getJSONObject("result");
		JSONArray reviews = new JSONArray();
		Double rating = 0.0;

		try {
			place = new JPlace();
			if (jsonObj.has("rating")) {
				rating = jsonObj.getDouble("rating");
			}
			if (jsonObj.has("reviews")) {
				reviews = jsonObj.getJSONArray("reviews");
			}

			place.setPlace_rating(rating);
			place.setReviews(reviews);
		} catch (Exception e) {
			System.out.println("detail : " + e);
		}
		return place;
	}

	/*
	 * GoogleAPI Place Search pasing
	 */
	private ArrayList<JPlace> parsingPlaceSearch(JSONArray predsJsonArray, ArrayList<JPlace> resultList) {
		JSONArray photos = new JSONArray();
		JSONObject photo_reference = new JSONObject();
		resultList = new ArrayList<JPlace>(predsJsonArray.length());
		try {
			for (int i = 0; i < predsJsonArray.length(); i++) {
				JPlace place = new JPlace();
				JPlace place_detail = new JPlace();
				place.setFormatted_address(predsJsonArray.getJSONObject(i).getString("formatted_address"));
				place.setName(predsJsonArray.getJSONObject(i).getString("name"));
				place.setPlace_id(predsJsonArray.getJSONObject(i).getString("place_id"));
				place_detail = detailSearch(place.getPlace_id());
				place.setPlace_rating(place_detail.getPlace_rating());
				place.setReviews(place_detail.getReviews());

				if (!predsJsonArray.getJSONObject(i).isNull("photos")) {
					photos = (JSONArray) predsJsonArray.getJSONObject(i).get("photos");
					photo_reference = (JSONObject) photos.get(0);
					place.setPhoto_reference(photo_reference.get("photo_reference").toString());
				}
				resultList.add(place);
			}
		} catch (JSONException e) {
			System.out.println("error processing JSON parsingPlaceSearch results : " + e);
		}

		return resultList;
	}

}
