/* 
 * Copyright (C) 2015 RankSys http://ranksys.github.io
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package es.saulvargas.recsys2015;

import org.ranksys.compression.codecs.CODEC;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import java.io.IOException;
import static es.saulvargas.recsys2015.Conventions.getCodec;
import static es.saulvargas.recsys2015.Conventions.getFixedLength;
import static es.saulvargas.recsys2015.Conventions.getPath;
import es.uam.eps.ir.ranksys.fast.index.FastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.FastUserIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastItemIndex;
import es.uam.eps.ir.ranksys.fast.index.SimpleFastUserIndex;
import es.uam.eps.ir.ranksys.fast.preference.SimpleFastPreferenceData;
import java.io.File;
import org.ranksys.compression.preferences.BinaryCODECPreferenceData;
import java.util.function.Function;
import java.util.stream.Stream;
import org.jooq.lambda.Unchecked;
import org.ranksys.compression.preferences.RatingCODECPreferenceData;
import org.ranksys.core.util.tuples.Tuple2io;
import org.ranksys.formats.index.ItemsReader;
import org.ranksys.formats.index.UsersReader;
import org.ranksys.formats.parsing.Parser;
import static org.ranksys.formats.parsing.Parsers.ip;
import static org.ranksys.formats.parsing.Parsers.sp;
import org.ranksys.formats.preference.CompressibleBinaryPreferencesFormat;
import org.ranksys.formats.preference.SimpleBinaryPreferencesReader;
import org.ranksys.formats.preference.SimpleRatingPreferencesReader;
import org.ranksys.formats.preference.CompressibleRatingPreferencesFormat;

/**
 * Program to create compressed preference data and save to compressed binary file.
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
public class Generate {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        String path = args[0];
        String dataset = args[1];
        String idxCodec = args[2];
        String vCodec = args[3];

        if (dataset.equals("msd") && !vCodec.equals("null")) {
            System.err.println("does not apply here: implicit data");
            return;
        }
        if (vCodec.startsWith("i")) {
            System.err.println("integrated codec only for ids");
            return;
        }

        switch (dataset) {
            case "ml1M":
            case "ml10M":
            case "ml20M":
            case "netflix":
            case "ymusic":
                store(path, dataset, idxCodec, vCodec, ip, ip);
                break;
            case "msd":
            default:
                store(path, dataset, idxCodec, vCodec, sp, sp);
                break;
        }
    }

    private static <U, I> void store(String path, String dataset, String idxCodec, String vCodec, Parser<U> up, Parser<I> ip) throws IOException {

        FastUserIndex<U> users = SimpleFastUserIndex.load(UsersReader.read(path + "/users.txt", up));
        FastItemIndex<I> items = SimpleFastItemIndex.load(ItemsReader.read(path + "/items.txt", ip));

        String dataPath = path + "/ratings.data";
        String uDataPath = path + "/ratings.u";
        String iDataPath = path + "/ratings.i";

        Function<CODEC<?>[], FastPreferenceData<U, I>> cdf = Unchecked.function(cds -> {
            switch (dataset) {
                case "msd":
                    CompressibleBinaryPreferencesFormat binaryFormat = CompressibleBinaryPreferencesFormat.get();
                    
                    if (!new File(uDataPath).exists() || !new File(iDataPath).exists()) {
                        SimpleFastPreferenceData<U, I> data = SimpleFastPreferenceData.load(SimpleBinaryPreferencesReader.get().read(dataPath, up, ip), users, items);
                        
                        binaryFormat.write(data, uDataPath, iDataPath);
                    }

                    Stream<Tuple2io<int[]>> ulb = binaryFormat.read(uDataPath);
                    Stream<Tuple2io<int[]>> ilb = binaryFormat.read(iDataPath);

                    return new BinaryCODECPreferenceData(ulb, ilb, users, items, cds[0], cds[1]);
                case "ml1M":
                case "ml10M":
                case "ml20M":
                case "netflix":
                case "ymusic":
                default:
                    CompressibleRatingPreferencesFormat ratingFormat = CompressibleRatingPreferencesFormat.get();

                    if (!new File(uDataPath).exists() || !new File(iDataPath).exists()) {
                        SimpleFastPreferenceData<U, I> data = SimpleFastPreferenceData.load(SimpleRatingPreferencesReader.get().read(dataPath, up, ip), users, items);
                        
                        ratingFormat.write(data, uDataPath, iDataPath);
                    }

                    Stream<Tuple2io<int[][]>> ulr = ratingFormat.read(uDataPath);
                    Stream<Tuple2io<int[][]>> ilr = ratingFormat.read(iDataPath);

                    return new RatingCODECPreferenceData(ulr, ilr, users, items, cds[0], cds[1], cds[2]);
            }
        });

        long time0 = System.nanoTime();
        int[] lens = getFixedLength(path, dataset);
        CODEC cd_uidxs = getCodec(idxCodec, lens[0]);
        CODEC cd_iidxs = getCodec(idxCodec, lens[1]);
        CODEC cd_vs = getCodec(vCodec, lens[2]);
        FastPreferenceData<U, I> preferences = cdf.apply(new CODEC[]{cd_uidxs, cd_iidxs, cd_vs});
        double loadingTime = (System.nanoTime() - time0) / 1_000_000_000.0;
        System.err.println("loaded " + dataset + " with " + idxCodec + "+" + vCodec + ": " + loadingTime);

        String fields = dataset + "\t" + idxCodec + "\t" + vCodec;
        System.out.println(fields + "\tus\t" + cd_uidxs.stats()[1]);
        System.out.println(fields + "\tis\t" + cd_iidxs.stats()[1]);
        System.out.println(fields + "\tvs\t" + cd_vs.stats()[1]);

        Utils.serialize(preferences, getPath(path, dataset, idxCodec, vCodec));
    }

}
