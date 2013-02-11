package de.sosd.mediaserver;

import java.util.ArrayList;

import de.sosd.mediaserver.dao.support.DidlDaoSupport;

public class TestSearchTranslation {

    private final static String[][] data = {
            {
            "upnp:genre = \"<unknown>\" and upnp:class derivedfrom \"object.container.person.musicArtist\"",
            " where didl.path like ?3 and (didl.genre = ?1 and (didl.classType = ?2))" },
            {
            "upnp:class derivedfrom \"object.item.imageItem.photo\" and (dc:date >= \"2001-10-01\" and dc:date <= \"2001-10-31\")",
            "" },
            { "dc:creator = \"Sting\"", "" },
            { "upnp:class derivedfrom \"object.container.person.musicArtist\"",
            "" }
                                         };

    @SuppressWarnings("unchecked")
    public static void main(final String[] args) {

        final DidlDaoSupport sh = new DidlDaoSupport();
        for (final String[] test : data) {
            final ArrayList list = new ArrayList();
            final String result = sh.translateSearchCriteria(test[0], list);

            System.out.println("result    : >" + result + "<");
            System.out.println("should be : >" + test[1] + "<");
        }
    }

}
