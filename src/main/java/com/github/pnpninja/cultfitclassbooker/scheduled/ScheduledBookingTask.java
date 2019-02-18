package com.github.pnpninja.cultfitclassbooker.scheduled;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pnpninja.cultfitclassbooker.CultFitClassBookerApplication;
import com.github.pnpninja.cultfitclassbooker.config.ConfigUtils;
import com.github.pnpninja.cultfitclassbooker.config.CultFitConstants;
import com.github.pnpninja.cultfitclassbooker.config.CultFitURLs;
import com.google.gson.Gson;
import com.jayway.jsonpath.InvalidPathException;
import com.jayway.jsonpath.JsonPath;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

@Component
public class ScheduledBookingTask {

	private static final Logger logger = LogManager.getLogger(ScheduledBookingTask.class);

	private static OkHttpClient client = new OkHttpClient();
	private static ObjectMapper objectMapper = new ObjectMapper();

	@Scheduled(cron = "1 0 22 * * ?")
	public void bookClassesInAdvance() {

		Request request = buildRequestGet(String.format(CultFitURLs.URL_CLASSES_FOR_CENTER, ConfigUtils.getKey()), ConfigUtils.getKey(),
				ConfigUtils.getCookie());

		Response response = null;
		try {
			response = client.newCall(request).execute();
			// logger.info(response.body().string());
			Map<String, Object> cultClassesData = convertJsonStringToHashMap(response.body().string());
			List<String> classIdsOrdered = getIDsOfPreferredClassAndTiming(ConfigUtils.getPreferredClasses(),
					ConfigUtils.getPreferredTimings(), cultClassesData, ConfigUtils.getBookingDays());
			logger.info("Calculated Order of ClassIDs - " + classIdsOrdered.toString());
			if (classIdsOrdered!=null && classIdsOrdered != Collections.EMPTY_LIST) {
				for (String classId : classIdsOrdered) {
					Request bookRequest = buildRequestPost(String.format(CultFitURLs.URL_BOOK_CLASS, classId),
							ConfigUtils.getKey(), ConfigUtils.getCookie(), "{}");
					try {
						response = client.newCall(bookRequest).execute();
						if (response.code() == 200) {
							break;
						}
					} catch (IOException e) {
						logger.info("Unable to book class - " + classId);
					}
				}
			}
		} catch (IOException e) {
			logger.error("Unable to fetch Class IDs to book - " + e.getMessage());
		} finally {
			response.close();
		}
	}

	private Request buildRequestGet(String URL, String apiKey, String cookie) {
		Request request = new Request.Builder().url(URL).get().addHeader("cookie", cookie).addHeader("apikey", apiKey)
				.build();
		return request;
	}

	private Request buildRequestPost(String URL, String apiKey, String cookie, String jsonData) {
		MediaType mediaType = MediaType.parse("application/octet-stream");
		RequestBody body = RequestBody.create(mediaType, jsonData);
		Request request = new Request.Builder().url(URL).post(body).addHeader("cookie", cookie)
				.addHeader("apikey", apiKey).build();
		return request;
	}

	private Map<String, Object> convertJsonStringToHashMap(String json)
			throws JsonParseException, JsonMappingException, IOException {
		return objectMapper.readValue(json, HashMap.class);
	}

	private List<String> getIDsOfPreferredClassAndTiming(List<String> preferredClassType,
			List<String> preferredClassTimings, Map<String, Object> cultClassesData,
			List<String> preferredBookingDays) {

		// TODO find out ID of first preferred class
		Date latestDate = getLatestDate(cultClassesData, preferredBookingDays);
		if (latestDate == null) {
			return null;
		}
		List<Object> bestClasses = getClassesAtDateAndPreferredTimes(cultClassesData, latestDate, preferredClassTimings,
				preferredClassType);
		Map<String, List<Object>> bestClassesMappedByClassType = new HashMap<String, List<Object>>();
		for (Object bestClass : bestClasses) {
			Map<String, Object> bestClassMap = (Map<String, Object>) bestClass;
			if (bestClassesMappedByClassType.get(bestClassMap.get("workoutName")) == null) {
				List<Object> tempList = new ArrayList<Object>();
				((Map<String, Object>) bestClass).put("timingPreference",
						preferredClassTimings.indexOf(bestClassMap.get("startTime")));
				tempList.add(bestClass);
				bestClassesMappedByClassType.put((String) bestClassMap.get("workoutName"), tempList);
			} else {
				((Map<String, Object>) bestClass).put("timingPreference",
						preferredClassTimings.indexOf(bestClassMap.get("startTime")));
				bestClassesMappedByClassType.get(bestClassMap.get("workoutName")).add(bestClass);
			}
		}
		List<String> classIdsOrdered = new ArrayList<String>();
		for (String classType : preferredClassType) {
			List<Object> classListUnordered = bestClassesMappedByClassType.get(classType);
			if (classListUnordered != null) {
				classListUnordered.sort(new Comparator<Object>() {
					@Override
					public int compare(Object o1, Object o2) {
						Map<String, Object> o11 = (Map<String, Object>) o1;
						Map<String, Object> o22 = (Map<String, Object>) o2;
						int a1 = (int) o11.get("timingPreference");
						int a2 = (int) o22.get("timingPreference");
						return a1 - a2;
					}
				});
				classListUnordered.forEach(clazz -> {
					Map<String, Object> clazzMap = (Map<String, Object>) clazz;
					classIdsOrdered.add((String) clazzMap.get("id"));
				});
			}

		}
		return classIdsOrdered;
	}

	private Date getLatestDate(Map<String, Object> cultClassesData, List<String> preferredDays) {
		Date latestDate = null;
		List<Object> classByDateList = (List<Object>) cultClassesData.get("days");
		List<Date> dates = classByDateList.parallelStream().map(classByDate -> {
			String dateString = (String) ((Map<String, Object>) classByDate).get("id");
			Date date;
			try {
				date = new SimpleDateFormat("yyyy-MM-dd").parse(dateString);
				return date;
			} catch (ParseException e) {
				logger.error(e.getMessage());
				return null;
			}
		}).filter(Objects::nonNull).collect(Collectors.toList());
		Collections.sort(dates, Collections.reverseOrder());
		if (dates == Collections.EMPTY_LIST) {
			return null;
		} else {
			latestDate = dates.get(0);
			if (CultFitConstants.DAY_MAP.get(latestDate.getDay()) != null
					&& preferredDays.contains(CultFitConstants.DAY_MAP.get(latestDate.getDay()))) {
				return latestDate;
			} else {
				return null;
			}
		}
	}

	private List<Object> getClassesAtDateAndPreferredTimes(Map<String, Object> cultClassesData, Date date,
			List<String> preferredTimes, List<String> preferredClassTypes) {
		if (date == null) {
			logger.info("Date is null. No booking can be conducted");
			return null;
		} else {
			String dateStr = new SimpleDateFormat("yyyy-MM-dd").format(date);
			try {
				List<Object> bookableClasses = JsonPath.read(new Gson().toJson(cultClassesData),
						prepareFullCondition(dateStr, preferredClassTypes, preferredTimes));
				return bookableClasses;
			} catch (InvalidPathException ipe) {
				logger.error("Error in prepared condition - "
						+ prepareFullCondition(dateStr, preferredClassTypes, preferredTimes));
				return null;
			} catch (ClassCastException cce) {
				logger.error("Error in casting object!");
				return null;
			}
		}
	}

	private String preparePreferredClassesCondition(List<String> preferredClassTypes) {
		return preparePreferredCondition("@.workoutName==", preferredClassTypes);
	}

	private String preparePreferredCondition(String filter, List<String> preferredList) {
		if (preferredList == null) {
			return null;
		} else {
			String classFilter = preferredList.parallelStream().map(preferredClass -> {
				return new String(filter + "'" + preferredClass + "'");
			}).collect(Collectors.joining(" || "));
			return "(" + classFilter + ")";
		}
	}

	private String preparePreferredTimeCondition(List<String> preferredTimes) {
		return preparePreferredCondition("@.startTime==", preferredTimes);
	}

	private String prepareFullCondition(String dateStr, List<String> preferredClassTypes, List<String> preferredTimes) {
		return "$.classByDateList[*].classByTimeList[*].classes[?(@.date=='" + dateStr + "' &&"
				+ preparePreferredClassesCondition(preferredClassTypes) + "&& "
				+ preparePreferredTimeCondition(preferredTimes) + " && (@.state=='AVAILABLE'))]";
	}

}
