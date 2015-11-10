# Analysing Compression Techniques for In-Memory Collaborative Filtering

This project contains the code that, together with the RankSys framework, reproduces the experiments of our RecSys 2015 paper ("Analysing Compression Techniques for In-Memory Collaborative Filtering")[http://ceur-ws.org/Vol-1441/recsys2015_poster2.pdf] written in collaboration with (Dr Craig Macdonald)[http://www.dcs.gla.ac.uk/~craigm/] and (Dr Iadh Ounis)[http://www.dcs.gla.ac.uk/~ounis/] from the (University of Glasgow)[http://www.gla.ac.uk/].

If you use this code, please consider citing not only the RecSys 2015 poster but also the following paper:
* Catena, M., Macdonald, C., Ounis, I. (2014). On Inverted Index Compression for Search Engine Efficiency. In ECIR (pp. 359–371). doi:10.1007/978-3-319-06028-6_30

The search index compression technologies of the ECIR paper by Catena et al. is part of the (Terrier IR Platform)[http://terrier.org/docs/v4.0/compression.html">http://terrier.org/docs/v4.0/compression.html].

## Instructions

1. Execute the example.sh script to run the code with a small example using the Movielens 1M dataset.
2. Execute the generate.sh script to generate serialized representations of compressed PreferenceData objects.
3. Execute the benchmark.sh script to run the speed test for the different compression techniques.

If you want to test this code with the same datasets as in the paper, i.e. the Netflix Prize dataset and the Yahoo Music dataset, prepare both dataset with the structure of the MovieLens 1M example and uncomment the corresponding lines in the generate.sh and benchmark.sh scripts.
