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

import static es.saulvargas.recsys2015.Conventions.getPath;
import es.uam.eps.ir.ranksys.core.util.parsing.Parser;
import static es.uam.eps.ir.ranksys.core.util.parsing.Parsers.ip;
import static es.uam.eps.ir.ranksys.core.util.parsing.Parsers.sp;
import es.uam.eps.ir.ranksys.fast.preference.FastPreferenceData;
import es.uam.eps.ir.ranksys.nn.item.ItemNeighborhoodRecommender;
import es.uam.eps.ir.ranksys.nn.item.neighborhood.CachedItemNeighborhood;
import es.uam.eps.ir.ranksys.nn.item.neighborhood.ItemNeighborhood;
import es.uam.eps.ir.ranksys.nn.item.neighborhood.TopKItemNeighborhood;
import es.uam.eps.ir.ranksys.nn.item.sim.ItemSimilarity;
import es.uam.eps.ir.ranksys.nn.item.sim.SetCosineItemSimilarity;
import es.uam.eps.ir.ranksys.nn.item.sim.VectorCosineItemSimilarity;
import es.uam.eps.ir.ranksys.nn.user.UserNeighborhoodRecommender;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.TopKUserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.neighborhood.UserNeighborhood;
import es.uam.eps.ir.ranksys.nn.user.sim.SetCosineUserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.UserSimilarity;
import es.uam.eps.ir.ranksys.nn.user.sim.VectorCosineUserSimilarity;
import it.unimi.dsi.fastutil.ints.IntArrays;
import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import static java.util.stream.DoubleStream.of;

/**
 * Program that loads a compressed preference data and evaluates by
 * running a user or item-based knn algorithm.
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
public class Benchmark {

    public static void main(String[] args) throws Exception {

        String path = args[0];
        String dataset = args[1];
        String idxCodec = args[2];
        String vCodec = args[3];
        int n = parseInt(args[4]);
        String funName = args[5];
        long seed = parseLong(args[6]);

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
                test(path, dataset, idxCodec, vCodec, n, funName, seed, ip, ip);
                break;
            case "msd":
            default:
                test(path, dataset, idxCodec, vCodec, n, funName, seed, sp, sp);
                break;
        }
    }

    public static <U, I> void test(String path, String dataset, String idxCodec, String vCodec, int n, String funName, long seed, Parser<U> up, Parser<I> ip) throws Exception {

        long time0 = System.nanoTime();
        FastPreferenceData<U, I> preferences = Utils.deserialize(getPath(path, dataset, idxCodec, vCodec));
        double loadingTime = (System.nanoTime() - time0) / 1_000_000_000.0;
        System.err.println("loaded " + dataset + " with " + idxCodec + "+" + vCodec + ": " + loadingTime);

        Random rnd = new Random(seed);
        int[] targetUsers;
        if (funName.contains("_")) {
            int N = parseInt(funName.split("_")[1]);
            funName = funName.split("_")[0];
            targetUsers = rnd.ints(0, preferences.numUsers()).distinct().limit(N).toArray();
        } else {
            targetUsers = preferences.getAllUidx().toArray();
        }
        IntArrays.shuffle(targetUsers, rnd);

        Consumer<FastPreferenceData<U, I>> fun;
        switch (funName) {
            case "urv":
                fun = prefs -> {
                    UserSimilarity<U> us = new VectorCosineUserSimilarity<>(prefs, 0.5, true);
                    UserNeighborhood<U> un = new TopKUserNeighborhood<>(us, 100);
                    UserNeighborhoodRecommender<U, I> rec = new UserNeighborhoodRecommender<>(prefs, un, 1);
                    IntStream.of(targetUsers).parallel().forEach(user -> rec.getRecommendation(user, 100));
                };
                break;
            case "urs":
                fun = prefs -> {
                    UserSimilarity<U> us = new SetCosineUserSimilarity<>(prefs, 0.5, true);
                    UserNeighborhood<U> un = new TopKUserNeighborhood<>(us, 100);
                    UserNeighborhoodRecommender<U, I> rec = new UserNeighborhoodRecommender<>(prefs, un, 1);
                    IntStream.of(targetUsers).parallel().forEach(user -> rec.getRecommendation(user, 100));
                };
                break;
            case "irv":
                fun = prefs -> {
                    ItemSimilarity<I> is = new VectorCosineItemSimilarity<>(prefs, 0.5, true);
                    ItemNeighborhood<I> in = new CachedItemNeighborhood<>(new TopKItemNeighborhood<>(is, 100));
                    ItemNeighborhoodRecommender<U, I> rec = new ItemNeighborhoodRecommender<>(prefs, in, 1);
                    IntStream.of(targetUsers).parallel().forEach(user -> rec.getRecommendation(user, 100));
                };
                break;
            case "irs":
                fun = prefs -> {
                    ItemSimilarity<I> is = new SetCosineItemSimilarity<>(prefs, 0.5, true);
                    ItemNeighborhood<I> in = new CachedItemNeighborhood<>(new TopKItemNeighborhood<>(is, 100));
                    ItemNeighborhoodRecommender<U, I> rec = new ItemNeighborhoodRecommender<>(prefs, in, 1);
                    IntStream.of(targetUsers).parallel().forEach(user -> rec.getRecommendation(user, 100));
                };
                break;
            default:
                fun = null;
                break;
        }

        double[] times = tiktok(fun, preferences, n);

        String fields = dataset + "\t" + idxCodec + "\t" + vCodec + "\t" + funName;
        for (double t : times) {
            System.out.println(fields + "\tt\t" + t);
        }
        System.out.println(fields + "\tlt\t" + times[times.length - 1]);
        System.out.println(fields + "\tat\t" + of(times).average().getAsDouble());
        System.out.println(fields + "\tmt\t" + of(times).min().getAsDouble());
    }

    private static <T> double[] tiktok(Consumer<T> fun, T t, int n) {
        double[] times = new double[n];

        for (int i = 0; i < n; i++) {
            long time0 = System.nanoTime();
            fun.accept(t);
            times[i] = (System.nanoTime() - time0) / 1_000_000_000.0;
        }

        return times;
    }

}
