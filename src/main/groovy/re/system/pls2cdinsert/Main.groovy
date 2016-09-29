package re.system.pls2cdinsert

import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

class Main {
    static Logger log = LoggerFactory.getLogger(Main.class)
    static String ITUNES_LIBRARY_PATH = "${System.properties['user.home']}/Music/iTunes/iTunes Library.xml"

    public static void main(String[] args) {
        log.info('iTunes Playlist To CD Insert, Version 1.0')
        log.info("Using iTunes library at: ${ITUNES_LIBRARY_PATH}")
        log.info("Processing playlist #${args[0]}")

        File file = new File(ITUNES_LIBRARY_PATH)
        NSDictionary library = PropertyListParser.parse(file);

        for (NSObject playlist : library.objectForKey('Playlists').array) {
            if (playlist.'Playlist ID'.intValue() == Integer.parseInt(args[0])) {
                processPlaylist(playlist)
            }
        }
    }

    static void processPlaylist(playlist) {
        log.info("Found playlist \"${playlist.Name}\"")
    }
}

