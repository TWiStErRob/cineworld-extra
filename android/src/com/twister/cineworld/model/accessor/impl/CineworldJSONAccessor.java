package com.twister.cineworld.model.accessor.impl;

import java.util.*;

import com.google.gson.*;
import com.twister.cineworld.exception.*;
import com.twister.cineworld.model.accessor.Accessor;
import com.twister.cineworld.model.accessor.impl.util.GeoCache;
import com.twister.cineworld.model.generic.*;
import com.twister.cineworld.model.generic.Date;
import com.twister.cineworld.model.json.*;
import com.twister.cineworld.model.json.data.*;
import com.twister.cineworld.model.json.request.*;
import com.twister.cineworld.model.json.response.BaseListResponse;

public class CineworldJSONAccessor implements Accessor {
	private static final String	GENERIC_SOURCE			= "CineworldJSON";
	private static final int	CINEWORLD_COMPANY_ID	= 1;
	private final JsonClient	m_jsonClient;

	public CineworldJSONAccessor() {
		Gson gson = new GsonBuilder()
				.registerTypeAdapter(CineworldDate.class, new CineworldDateTypeConverter())
				.create();
		m_jsonClient = new ApacheHttpJsonClient(gson);
	}

	public List<Cinema> getAllCinemas() throws ApplicationException {
		CinemasRequest request = new CinemasRequest();
		request.setFull(true);
		List<CineworldCinema> list = getList(request);
		List<Cinema> result = convert(list);
		for (Cinema cinema : result) {
			cinema.setTerritory(request.getTerritory());
		}
		return result;
	}

	public Cinema getCinema(final int cinemaId) throws ApplicationException {
		CinemasRequest request = new CinemasRequest();
		request.setCinemas(Arrays.asList(cinemaId));
		request.setFull(true);
		CineworldCinema single = getSingular(request, cinemaId);
		Cinema result = convert(single);
		result.setTerritory(request.getTerritory());
		return result;
	}

	public List<Cinema> getCinemas(final int filmEdi) throws ApplicationException {
		CinemasRequest request = new CinemasRequest();
		request.setFull(true);
		request.setFilms(Arrays.asList(filmEdi));
		List<CineworldCinema> list = getList(request);
		List<Cinema> result = convert(list);
		for (Cinema cinema : result) {
			cinema.setTerritory(request.getTerritory());
		}
		return result;
	}

	public List<Film> getAllFilms() throws ApplicationException {
		FilmsRequest request = new FilmsRequest();
		request.setFull(true);
		List<CineworldFilm> list = getList(request);
		List<Film> result = convert(list);
		return result;
	}

	public Film getFilm(final int filmEdi) throws ApplicationException {
		FilmsRequest request = new FilmsRequest();
		request.setFilms(Arrays.asList(filmEdi));
		request.setFull(true);
		CineworldFilm single = getSingular(request, filmEdi);
		Film result = convert(single);
		return result;
	}

	public List<Film> getFilms(final int cinemaId) throws ApplicationException {
		FilmsRequest request = new FilmsRequest();
		request.setFull(true);
		request.setCinemas(Arrays.asList(cinemaId));
		List<CineworldFilm> list = getList(request);
		List<Film> result = convert(list);
		return result;
	}

	public List<Film> getFilms(final int cinemaId, final TimeSpan span) throws ApplicationException {
		FilmsRequest request = new FilmsRequest();
		request.setFull(true);
		request.setCinemas(Arrays.asList(cinemaId));
		if (span == TimeSpan.Tomorrow) {
			Calendar now = Calendar.getInstance();
			now.add(Calendar.DAY_OF_MONTH, 1); // 1 day later
			request.setDates(RequestTools.convertDates(now));
		}
		List<CineworldFilm> list = getList(request);
		List<Film> result = convert(list);
		return result;
	}

	public List<Date> getAllDates() throws ApplicationException {
		DatesRequest request = new DatesRequest();
		List<CineworldDate> list = getList(request);
		List<Date> result = convert(list);
		return result;
	}

	public List<Date> getDates(final int filmEdi) throws ApplicationException {
		DatesRequest request = new DatesRequest();
		request.setFilms(Arrays.asList(filmEdi));
		List<CineworldDate> list = getList(request);
		List<Date> result = convert(list);
		return result;
	}

	public List<Category> getAllCategories() throws ApplicationException {
		CategoriesRequest request = new CategoriesRequest();
		List<CineworldCategory> list = getList(request);
		List<Category> result = convert(list);
		return result;
	}

	public List<Event> getAllEvents() throws ApplicationException {
		EventsRequest request = new EventsRequest();
		List<CineworldEvent> list = getList(request);
		List<Event> result = convert(list);
		return result;
	}

	public List<Distributor> getAllDistributors() throws ApplicationException {
		DistributorsRequest request = new DistributorsRequest();
		List<CineworldDistributor> list = getList(request);
		List<Distributor> result = convert(list);
		return result;
	}

	public List<Performance> getPeformances(final int cinemaId, final int filmEdi, final int date)
			throws ApplicationException {
		PerformancesRequest request = new PerformancesRequest();
		request.setCinema(cinemaId);
		request.setFilm(filmEdi);
		request.setDate(date);
		List<CineworldPerformance> list = getList(request);
		List<Performance> result = convert(list);
		return result;
	}

	private <T extends CineworldBase> List<T> getList(final BaseListRequest<T> request) throws ApplicationException {
		BaseListResponse<T> response = this.m_jsonClient.get(request.getURL(), request.getResponseClass());
		if (response.getErrors() != null && !response.getErrors().isEmpty()) {
			throw new InternalException("Errors in JSON response: %s", response.getErrors());
		}
		return response.getList();
	}

	private <T extends CineworldBase> T getSingular(final BaseListRequest<T> request, final Object parameter)
			throws ApplicationException {
		List<T> list = getList(request);
		if (list.isEmpty()) {
			throw new InternalException("No results for request");
		} else if (list.size() == 1) {
			return list.get(0);
		} else {
			throw new InternalException("Multiple %s returned for parameter=%s", request.getRequestType(), parameter);
		}
	}

	private <TOut extends GenericBase, TIn extends CineworldBase> List<TOut> convert(final List<TIn> responseItems) {
		List<TOut> cinemas = new ArrayList<TOut>(responseItems.size());
		for (TIn cineworldObject : responseItems) {
			TOut object = convert(cineworldObject);
			cinemas.add(object);
		}
		return cinemas;
	}

	// TODO Dozer
	@SuppressWarnings("unchecked")
	private <TOut extends GenericBase, TIn extends CineworldBase> TOut convert(final TIn cineworldObject) {
		GenericBase result = null;
		if (cineworldObject instanceof CineworldCinema) {
			CineworldCinema cineworld = (CineworldCinema) cineworldObject;
			Cinema generic = new Cinema();
			generic.setCompanyId(CineworldJSONAccessor.CINEWORLD_COMPANY_ID);
			generic.setId(cineworld.getId());
			generic.setName(cineworld.getName());
			generic.setDetailsUrl(cineworld.getCinemaUrl());
			generic.setAddress(cineworld.getAddress());
			generic.setPostcode(cineworld.getPostcode());
			generic.setTelephone(cineworld.getTelephone());
			generic.setLocation(GeoCache.getGeoPoint(cineworld.getPostcode()));
			result = generic;
		} else if (cineworldObject instanceof CineworldCategory) {
			CineworldCategory cineworld = (CineworldCategory) cineworldObject;
			Category generic = new Category();
			generic.setCode(cineworld.getCode());
			generic.setName(cineworld.getName());
			result = generic;
		} else if (cineworldObject instanceof CineworldEvent) {
			CineworldEvent cineworld = (CineworldEvent) cineworldObject;
			Event generic = new Event();
			generic.setCode(cineworld.getCode());
			generic.setName(cineworld.getName());
			result = generic;
		} else if (cineworldObject instanceof CineworldDistributor) {
			CineworldDistributor cineworld = (CineworldDistributor) cineworldObject;
			Distributor generic = new Distributor();
			generic.setId(cineworld.getId());
			generic.setName(cineworld.getName());
			result = generic;
		} else if (cineworldObject instanceof CineworldPerformance) {
			CineworldPerformance cineworld = (CineworldPerformance) cineworldObject;
			Performance generic = new Performance();
			generic.setTime(cineworld.getTime());
			generic.setType(cineworld.getType());
			generic.setBookingUrl(cineworld.getBookingUrl());
			generic.setAvailable(cineworld.isAvailable());
			generic.setSubtitled(cineworld.isSubtitled());
			generic.setAudioDescribed(cineworld.isAudioDescribed());
			result = generic;
		} else if (cineworldObject instanceof CineworldDate) {
			CineworldDate cineworld = (CineworldDate) cineworldObject;
			Date generic = new Date();
			generic.setDate(cineworld.getDate());
			result = generic;
		} else if (cineworldObject instanceof CineworldFilm) {
			CineworldFilm cineworld = (CineworldFilm) cineworldObject;
			Film generic = new Film();
			generic.setId(cineworld.getId());
			generic.setEdi(cineworld.getEdi());
			generic.setTitle(cineworld.getTitle());
			generic.setClassification(cineworld.getClassification());
			generic.setAdvisory(cineworld.getAdvisory());
			generic.setPosterUrl(cineworld.getPosterUrl());
			generic.setStillUrl(cineworld.getStillUrl());
			generic.setFilmUrl(cineworld.getFilmUrl());
			generic.set3D(cineworld.is3D());
			generic.setIMax(cineworld.isIMax());
			result = generic;
		}
		if (result != null) {
			result.setSource(CineworldJSONAccessor.GENERIC_SOURCE);
		}
		return (TOut) result;
	}
}
