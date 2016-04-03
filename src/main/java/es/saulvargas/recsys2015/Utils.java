package es.saulvargas.recsys2015;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
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

}
