package com.twister.cineworld.model.json;

import java.io.IOException;
import java.net.*;
import java.util.*;

import android.util.Log;

import com.google.gson.*;
import com.twister.cineworld.model.json.data.*;
import com.twister.cineworld.model.json.request.*;
import com.twister.cineworld.model.json.response.*;
import com.twister.cineworld.ui.Tools;

public class CineworldAccessor {
	public static final URL	URL_FILMS_ALL			= BaseRequest.makeUrl("quickbook/films", "full=true");
	public static final URL	URL_DATES_ALL			= BaseRequest.makeUrl("quickbook/dates");
	public static final URL	URL_PERFORMANCES		= BaseRequest.makeUrl("quickbook/performances", "cinema=%s", "film=%s", "date=%s");
	public static final URL	URL_CATEGORIES_ALL		= BaseRequest.makeUrl("categories");
	public static final URL	URL_EVENTS_ALL			= BaseRequest.makeUrl("events");
	public static final URL	URL_DISTRIBUTORS_ALL	= BaseRequest.makeUrl("distributors");

	private final Gson		m_gson;

	public CineworldAccessor() {
		m_gson = new GsonBuilder()
				.registerTypeAdapter(CineworldDate.class, new CineworldDateTypeConverter())
				.create();
	}

	public List<CineworldCinema> getAllCinemas() {
		CinemasRequest request = new CinemasRequest();
		request.setFull(true);
		return get(request.getURL(), "cinemas", CinemasResponse.class);
	}

	public List<CineworldFilm> getAllFilms() {
		return get(CineworldAccessor.URL_FILMS_ALL, "films", FilmsResponse.class);
	}

	public List<CineworldDate> getAllDates() {
		return get(CineworldAccessor.URL_DATES_ALL, "dates", DatesResponse.class);
	}

	public List<CineworldCategory> getAllCategories() {
		return get(CineworldAccessor.URL_CATEGORIES_ALL, "films", CategoriesResponse.class);
	}

	public List<CineworldEvent> getAllEvents() {
		return get(CineworldAccessor.URL_EVENTS_ALL, "events", EventsResponse.class);
	}

	public List<CineworldDistributor> getAllDistributors() {
		return get(CineworldAccessor.URL_DISTRIBUTORS_ALL, "distributors", DistributorsResponse.class);
	}

	public List<CineworldPerformance> getPeformances(final String cinema, final String film, final String date) {
		try {
			URL url = new URL(String.format(CineworldAccessor.URL_PERFORMANCES.toString(), cinema, film, date));
			return get(url, "performances", PerformancesResponse.class);
		} catch (MalformedURLException ex) {
			Log.e("ACCESS", String.format("Invalid URL getPerformances: cinema='%s', film='%s', date='%s'", cinema, film, date), ex);
			return Collections.emptyList();
		}
	}

	@SuppressWarnings("unchecked")
	private <T extends CineworldBase, Response extends BaseResponse<? extends T>>
			List<T> get(final URL url, final String objectType, final Class<Response> clazz) {
		List<? extends T> result = Collections.emptyList();
		try {
			result = new JsonClient(m_gson).get(url, clazz).getList();
		} catch (IOException ex) {
			Log.d("ACCESS", "Unable to get all " + objectType, ex);
			Tools.toast(ex.getMessage()); // TODO
		} catch (URISyntaxException ex) {
			Log.d("ACCESS", "Unable to get all " + objectType, ex);
			Tools.toast(ex.getMessage()); // TODO
		}
		return (List<T>) result; // TODO review generic bounds
	}

	private <T extends CineworldBase> List<T> getList(final BaseRequest<T> request) {
		return get(request.getURL(), request.getRequestType(), request.getResponseClass());
	}

	// ////////////////////////////////////////////////////////////////////////////////////
	// More specific methods
	// ////////////////////////////////////////////////////////////////////////////////////

	public CineworldCinema getCinema(final int cinemaId) {
		CinemasRequest request = new CinemasRequest();
		request.setCinema(cinemaId);
		List<CineworldCinema> cinemas = getList(request);
		if (cinemas.isEmpty()) {
			return null;
		} else if (cinemas.size() == 1) {
			return cinemas.get(0);
		} else {
			Log.w("ACCESS", String.format("Multiple cinemas returned for id=%d", cinemaId));
			return null;
		}
	}
}
