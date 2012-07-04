package de.sosd.mediaserver.service.db;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;

import de.sosd.mediaserver.domain.db.ClassNameWcType;

@Service
public class StorageHelper {

	private final static Log logger = LogFactory.getLog(StorageHelper.class);
	
	private final static Map<String, String> didlFieldSortMap = new HashMap<String, String>();
	private final static Map<String, String> didlFieldSearchMap = new HashMap<String, String>();
	
	private final static String sortCapabilities = "upnp:class,dc:title,dc:date,upnp:artist,upnp:album,upnp:genre,upnp:originalTrackNumber";
	private final static String filterCapabilities = "upnp:class,dc:title,dc:date,upnp:artist,upnp:album,upnp:genre,upnp:originalTrackNumber";
	
	private final static String[][] didlFieldHqlBinding = {
		{"dc:title", "didl.title"},
		{"dc:date", "didl.date"},
		{"upnp:album", "didl.album"},
		{"upnp:originalTrackNumber", "didl.track"},
//		{"dc:creator"},
//		{"upnp:actor"},
		{"upnp:artist", "didl.artist"},
		{"upnp:genre", "didl.genre"},
//		{"@refID"},
		{"upnp:class", "didl.classType"}
	};
	
    static {
    	for (final String [] content : didlFieldHqlBinding) {
    		didlFieldSortMap.put(content[0], content[1]);
    		didlFieldSortMap.put("+" + content[0], content[1] + " asc");
    		didlFieldSortMap.put("-" + content[0], content[1] + " desc");  		
    		didlFieldSearchMap.put(content[0], content[1]);
    	}
    }	

    private enum ParserState {
    	
    	NAME,OPERATOR,VALUE
    	
    }
    
    public String translateSearchCriteria(final String search, final List<Object> parameters) {
    	// upnp:class derivedfrom "object.item.imageItem.photo" and (dc:date >= "2001-10-01" and dc:date <= "2001-10-31")
    	// dc:creator = "Sting"
    	final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
    	
    	final String[] words = search.replaceAll("\\(" , "\\( ").replaceAll("\\)", " \\)").split(" ");
    	final StringBuffer hql = new StringBuffer();
    	ParserState ps = ParserState.NAME;
    	StringBuffer stmt = new StringBuffer();
    	boolean valid = true;
//    	boolean complete = false;
    	
    	boolean isDate = false;
    	boolean isDerivedClass = false;
    	
    	for (final String word : words) {
    		if (word.length() > 0) {
    			
    			if ("(".equals(word) || ")".equals(word)) {
    				stmt.append(word);
    			} else {
    				if ("and".equalsIgnoreCase(word) || "or".equalsIgnoreCase(word)) {
    					stmt.append(" ");
    					stmt.append(word);
    					stmt.append(" ");
    					ps = ParserState.NAME;
    				} else {
    					switch (ps) {
						case NAME: {
							stmt = new StringBuffer();
							valid = didlFieldSearchMap.containsKey(word);
							stmt.append(didlFieldSearchMap.get(word));
							isDate = (word.equals("dc:date"));
							ps = ParserState.OPERATOR;
							break;
							}
						case OPERATOR: {
							if ("=".equals(word) || ">=".equals(word) || "<=".equals(word)) {
								stmt.append(" ");
								stmt.append(word);
								stmt.append(" ");
							} else {
								isDerivedClass = ("derivedfrom".equalsIgnoreCase(word));
								if (isDerivedClass) {
									stmt.append(" = ");
								} else {
									valid = false;
								}
							}
							
							ps = ParserState.VALUE;
							break;
							}
						case VALUE: {
							if (valid) {

								final String cword = word.replaceAll("\"", "");
								if (isDate) {
									// parse as Date
									try {
										final Date date = sdf.parse(cword);
										parameters.add(date);	
										stmt.append("?" + parameters.size());
										hql.append(stmt);
										
									} catch (final ParseException pe) {
										logger.error("can't parse date [" + cword + "] in search " + search);
									}
								} else {
									if (isDerivedClass) {
										boolean first = true;
										hql.append("(");
										for (final ClassNameWcType ct : ClassNameWcType.values()) {
											if (ct.value().toLowerCase().startsWith(cword.toLowerCase())) {
												final StringBuffer nstmt = new StringBuffer(stmt);
												if (! first) {
													hql.append(" or ");
												} else {
													first = false;
												}
												parameters.add(ct);
												nstmt.append("?" + parameters.size());
												hql.append(nstmt);
												
											}
										}
										hql.append(")");
									} else {
										// parse as string
										parameters.add(cword);
										stmt.append("?"+ parameters.size());
										hql.append(stmt);		
									}
								
								}
								isDate = false;
								isDerivedClass = false;
								ps = ParserState.NAME;
								break;
								}
							}
	    				}
	    			}
	    		}
    		} else {
    			// ignore whitespaces
    		}
    	}
    	
    	
    	String result = "";
    	final String whereStmts = hql.toString() ;
    	if (whereStmts.length() > 0) {
    		result = " where didl.path like ?" + (parameters.size() + 1) + " and ("+ whereStmts+ ")";
    	} else {
    		result = " where didl.path like ?" + (parameters.size() + 1);
    	}
    	
    	logger.info("requested search [" +search+ "] was transformed to [" + result + "]");
    	return result;
    }
    
	/**
	 * has to translate the didl filter into hql ' and a=b' or something
	 * 
	 * @param filter
	 * @return
	 */
	public String translateFilter(final String filter) {
		// @id,upnp:class,res,res@protocolInfo,res@av:authenticationUri,res@size,dc:title,upnp:albumArtURI,res@dlna:ifoFileURI,res@protection,res@bitrate,res@duration,res@sampleFrequency,res@bitsPerSample,res@nrAudioChannels,res@resolution,res@colorDepth,dc:date,av:dateTime,upnp:artist,upnp:album,upnp:genre,dc:contributer,upnp:storageFree,upnp:storageUsed,upnp:originalTrackNumber,dc:publisher,dc:language,dc:region,dc:description,upnp:toc,@childCount,upnp:albumArtURI@dlna:profileID,res@dlna:cleartextSize
		logger.info("requested filter was not transformed : " + filter);
		return "";
	}

	/**
	 * has to translate the didl sortcriteria into hql ' order by didl.title' or something
	 * 
	 * @param sortCriteria
	 * @return
	 */
	public String translateSortCriteria(final String sortCriteria) {
		// " order by didl.classType asc, didl.title asc, didl.date desc"
		final StringBuffer result = new StringBuffer();
		boolean first = true;
		// default sort by container before items, title ascending, latest last 
		String[] sorts = {"-upnp:class","+dc:title","+dc:date"};
		// if another criteria is requested, use it
		if ((sortCriteria!= null) && (sortCriteria.length() > 0)) {
			sorts = sortCriteria.split(",");
		}
		for (final String sort : sorts) {
			if (didlFieldSortMap.containsKey(sort)) {
				final String hql = didlFieldSortMap.get(sort);
				
				if (first) {
					result.append(" order by ");
					first = false;
				} else {	
					result.append(", ");
				}
				result.append(hql);
			} else {
				logger.info("requested sort-criteria was not transformed : " + sort);				
			}
		}
		logger.info("requested sort-criteria " + sortCriteria + " was transformed to " + result.toString());		
		return result.toString();
	}

	public String getGetSortCriterias() {
		return sortCapabilities;
	}

	public String getGetFilters() {
		return 	filterCapabilities;// getAsDidlString(didlFieldFilterMap);
	}

}
