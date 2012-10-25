package de.sosd.mediaserver;

import java.util.ArrayList;

import de.sosd.mediaserver.service.db.StorageHelper;

public class TestSearchTranslation {

	
	private final static String[][] data = {
		{"upnp:genre = \"<unknown>\" and upnp:class derivedfrom \"object.container.person.musicArtist\""," where didl.path like ?3 and (didl.genre = ?1 and (didl.classType = ?2))"},
		{"upnp:class derivedfrom \"object.item.imageItem.photo\" and (dc:date >= \"2001-10-01\" and dc:date <= \"2001-10-31\")",""},
		{"dc:creator = \"Sting\"",""},
		{"upnp:class derivedfrom \"object.container.person.musicArtist\"",""}
		};
	
	
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {
		
		StorageHelper sh = new StorageHelper();
		for (int i = 0; i < data.length; ++i) {
			String [] test = data[i];
			
			ArrayList list = new ArrayList();
			String result = sh.translateSearchCriteria(test[0], list);
			
			System.out.println("result    : >" + result + "<");
			System.out.println("should be : >" + test[1] + "<");
		}
	}
	
}
