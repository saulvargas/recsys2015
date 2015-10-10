package es.saulvargas.recsys2015;

import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.fast.preference.TransposedPreferenceData;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UncheckedIOException;
import java.util.function.BiConsumer;
import static java.util.stream.Collectors.joining;

/**
 *
 * @author Saúl Vargas (Saul.Vargas@glasgow.ac.uk)
 */
public class Utils {

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
