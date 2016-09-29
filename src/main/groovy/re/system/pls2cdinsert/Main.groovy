package re.system.pls2cdinsert

import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.audio.mp3.MP3File
import org.jaudiotagger.tag.id3.AbstractID3v2Tag
import org.jaudiotagger.tag.id3.ID3v24Frames
import org.odftoolkit.odfdom.doc.OdfTextDocument
import org.odftoolkit.odfdom.dom.element.style.StyleMasterPageElement
import org.odftoolkit.odfdom.dom.style.props.OdfPageLayoutProperties
import org.odftoolkit.odfdom.incubator.doc.style.OdfStyle
import org.odftoolkit.odfdom.incubator.doc.style.OdfStylePageLayout
import org.odftoolkit.odfdom.incubator.doc.text.OdfTextParagraph
import org.slf4j.LoggerFactory;
import org.slf4j.Logger
import java.nio.file.Paths;

class Main {
    static Logger log = LoggerFactory.getLogger(Main.class)
    static String ITUNES_LIBRARY_PATH = "${System.properties['user.home']}/Music/iTunes/iTunes Library.xml"
    static NSDictionary library
    static String insertText = ''
    static String savePath = "${System.properties['user.home']}/Desktop"

    public static void main(String[] args) {
        System.setProperty('sun.java2d.cmm', 'sun.java2d.cmm.kcms.KcmsServiceProvider')

        log.info('iTunes Playlist To CD Insert, Version 1.0')
        log.info("Using iTunes library at: ${ITUNES_LIBRARY_PATH}")
        log.info("Processing playlist #${args[0]}")
        File file = new File(ITUNES_LIBRARY_PATH)
        library = PropertyListParser.parse(file);

        library.objectForKey('Playlists').array.each { playlist ->
            if (playlist.'Playlist ID'.intValue() == Integer.parseInt(args[0])) {
                processPlaylist(playlist)
            }
        }

        createDoc()
    }

    static createDoc() {
        OdfTextDocument odt = OdfTextDocument.newTextDocument()

        StyleMasterPageElement defaultPage = odt.officeMasterStyles.getMasterPage('Standard')
        String pageLayoutName = defaultPage.stylePageLayoutNameAttribute
        OdfStylePageLayout pageLayoutStyle = defaultPage.automaticStyles.getPageLayout(pageLayoutName)
        pageLayoutStyle.setProperty(OdfPageLayoutProperties.MarginRight, '5cm')
        pageLayoutStyle.setProperty(OdfPageLayoutProperties.MarginLeft, '5cm')

        insertText.eachLine() {
            OdfTextParagraph p = odt.newParagraph()
            p.addContent(it)
        }

        OdfTextParagraph br = odt.newParagraph()
        br.addContent('')

        insertText.eachLine() {
            OdfTextParagraph p = odt.newParagraph()
            p.addContent(it)
        }

        odt.save(savePath)

    }

    static processPlaylist(playlist) {
        log.info("Found playlist \"${playlist.Name}\"")

        insertText += "${playlist.Name}\n\n"
        savePath += "/${playlist.Name.content.replace(':', '').replace('/', '-')}.odt"

        playlist.'Playlist Items'.array.eachWithIndex { item,i  ->
            getTrack(item.'Track ID', i)
        }
    }

    static getTrack(trackId, i) {
        NSDictionary tracks = library.objectForKey('Tracks')
        NSObject track = tracks.get((String)trackId)
        MP3File mp3File = AudioFileIO.read(Paths.get(new URL(track.Location.content).toURI()).toFile())
        AbstractID3v2Tag tag = mp3File.getID3v2Tag()

        insertText += "${String.format('%02d', i + 1)} " +
                      "[${tag.getFirst(ID3v24Frames.FRAME_ID_BPM)}, " +
                      "${tag.getFirst(ID3v24Frames.FRAME_ID_INITIAL_KEY)}] " +
                      "${tag.getFirst(ID3v24Frames.FRAME_ID_TITLE)} - " +
                      "${tag.getFirst(ID3v24Frames.FRAME_ID_ARTIST)}\n"

    }

}

