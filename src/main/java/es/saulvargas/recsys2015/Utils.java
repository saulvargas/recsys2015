package es.saulvargas.recsys2015;

import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.fast.preference.TransposedPreferenceData;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import static java.util.stream.Collectors.joining;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Utilities for serializing data and converting between formats.
 * <br>
 * If you use this code, please cite the following papers:
 * <ul>
 * <li>Vargas, S., Macdonald, C., Ounis, I. (2015). Analysing Compression Techniques for In-Memory Collaborative Filtering. In Poster Proceedings of the 9th ACM Conference on Recommender Systems. <a href="http://ceur-ws.org/Vol-1441/recsys2015_poster2.pdf">http://ceur-ws.org/Vol-1441/recsys2015_poster2.pdf</a>.</li>
 * <li>Catena, M., Macdonald, C., Ounis, I. (2014). On Inverted Index Compression for Search Engine Efficiency. In ECIR (pp. 359–371). doi:10.1007/978-3-319-06028-6_30</li>
 * </ul>
 * The code that reproduces the results of the RecSys 2015 poster by Vargas et al. in a separated project: <a href="http://github.com/saulvargas/recsys2015">http://github.com/saulvargas/recsys2015</a>
 * <br>
 * The search index compression technologies of the ECIR paper by Catena et al. is part of the Terrier IR Platform: <a href="http://terrier.org/docs/v4.0/compression.html">http://terrier.org/docs/v4.0/compression.html</a>.
 *
 * @author Saúl Vargas (Saul.Vargas@glasgow.ac.uk)
 */
public class Utils {

    /**
     * Serialize object.
     *
     * @param <T> type of object
     * @param t object
     * @param path path of the file
     * @throws IOException when IO error
     */
    public static <T> void serialize(T t, String path) throws IOException {
        serialize(t, new FileOutputStream(path));
    }

    /**
     * Serialize object.
     *
     * @param <T> type of object
     * @param t object
     * @param out output stream
     * @throws IOException when IO error
     */
    public static <T> void serialize(T t, OutputStream out) throws IOException {
        try (ObjectOutputStream oos = new ObjectOutputStream(new GZIPOutputStream(out))) {
            oos.writeObject(t);
        }
    }
    
    /**
     * Deserialize object.
     *
     * @param <T> type of object
     * @param path path of file
     * @return object
     * @throws IOException when IO error
     * @throws ClassNotFoundException when deserializing wrong class
     */
    public static <T> T deserialize(String path) throws IOException, ClassNotFoundException {
        return deserialize(new FileInputStream(path));
    }

    /**
     * Deserialize object.
     *
     * @param <T> type of object
     * @param in input stream
     * @return object
     * @throws IOException when IO error
     * @throws ClassNotFoundException when deserializing wrong classe
     */
    @SuppressWarnings("unchecked")
    public static <T> T deserialize(InputStream in) throws IOException, ClassNotFoundException {
        try (ObjectInputStream ois = new ObjectInputStream(new GZIPInputStream(in))) {
            return (T) ois.readObject();
        }
    }

    /**
     * Saves a PreferenceData instance in two files for user and item preferences, respectively. The format of the user preferences file consists on one list per line, starting with the identifier of the user followed by the identifier-rating pairs of the items related to that. The item preferences file follows the same format by swapping the roles of users and items.
     *
     * @param prefData preferences
     * @param up path to user preferences file
     * @param ip path to item preferences file
     * @throws FileNotFoundException one of the files could not be created
     * @throws IOException other IO error
     */
    public static void saveRatingData(FastPreferenceData<?, ?> prefData, String up, String ip) throws FileNotFoundException, IOException {
        saveRatingData(prefData, new FileOutputStream(up), new FileOutputStream(ip));
    }

    /**
     * Saves a PreferenceData instance in two files for user and item preferences, respectively. The format of the user preferences stream consists on one list per line, starting with the identifier of the user followed by the identifier-rating pairs of the items related to that. The item preferences stream follows the same format by swapping the roles of users and items.
     *
     * @param prefData preferences
     * @param uo stream of user preferences
     * @param io stream of user preferences
     * @throws IOException when IO error
     */
    public static void saveRatingData(FastPreferenceData<?, ?> prefData, OutputStream uo, OutputStream io) throws IOException {
        BiConsumer<FastPreferenceData<?, ?>, OutputStream> saver = (prefs, os) -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                prefs.getUidxWithPreferences().forEach(uidx -> {
                    String a = prefs.getUidxPreferences(uidx)
                            .sorted((p1, p2) -> Integer.compare(p1.idx, p2.idx))
                            .map(p -> p.idx + "\t" + (int) p.v)
                            .collect(joining("\t"));
                    try {
                        writer.write(uidx + "\t" + a);
                        writer.newLine();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };

        saver.accept(prefData, uo);
        saver.accept(new TransposedPreferenceData<>(prefData), io);
    }

    /**
     * Saves a PreferenceData instance in two files for user and item preferences, respectively. The format of the user preferences file consists on one list per line, starting with the identifier of the user followed by the identifiers of the items related to that. The item preferences file follows the same format by swapping the roles of users and items.
     *
     * @param prefData preferences
     * @param up path to user preferences file
     * @param ip path to item preferences file
     * @throws FileNotFoundException one of the files could not be created
     * @throws IOException other IO error
     */
    public static void saveBinaryData(FastPreferenceData<?, ?> prefData, String up, String ip) throws FileNotFoundException, IOException {
        saveBinaryData(prefData, new FileOutputStream(up), new FileOutputStream(ip));
    }

    /**
     * Saves a PreferenceData instance in two files for user and item preferences, respectively. The format of the user preferences stream consists on one list per line, starting with the identifier of the user followed by the identifiers of the items related to that. The item preferences stream follows the same format by swapping the roles of users and items.
     *
     * @param prefData preferences
     * @param uo stream of user preferences
     * @param io stream of user preferences
     * @throws IOException when IO error
     */
    public static void saveBinaryData(FastPreferenceData<?, ?> prefData, OutputStream uo, OutputStream io) throws IOException {
        BiConsumer<FastPreferenceData<?, ?>, OutputStream> saver = (prefs, os) -> {
            try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(os))) {
                prefs.getUidxWithPreferences().forEach(uidx -> {
                    String a = prefs.getUidxPreferences(uidx)
                            .sorted((p1, p2) -> Integer.compare(p1.idx, p2.idx))
                            .map(p -> Integer.toString(p.idx))
                            .collect(joining("\t"));
                    try {
                        writer.write(uidx + "\t" + a);
                        writer.newLine();
                    } catch (IOException ex) {
                        throw new UncheckedIOException(ex);
                    }
                });
            } catch (IOException ex) {
                throw new UncheckedIOException(ex);
            }
        };

        saver.accept(prefData, uo);
        saver.accept(new TransposedPreferenceData<>(prefData), io);
    }

}
