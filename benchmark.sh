#!/usr/bin/bash

main="-jar target/recsys2015-1.0.jar Benchmark"

datasets=("ml1M")
funs=("urv_1000")
ns=(4)
seeds=(28351)
Xmxs=("4G 2G 1G")

for p in 7
do
    for (( i=0; i<${#datasets[@]}; i++ ))
    do
    for m in ${Xmxs[$i]}
    do
        dataset=${datasets[$i]}
        path="${dataset}/"
        fun=${funs[$i]}
        n=${ns[$i]}
        n2=`echo "$n + 3" | bc`
        seed=${seeds[$i]}
        params="-Xmx${m} -Djava.util.concurrent.ForkJoinPool.common.parallelism=${p}"
        >&2 echo $seed
        # no compression
        paste <(yes $p | head -n $n2) <(yes $m | head -n $n2) <(java $params $main $path $dataset null null $n $fun $seed)
        # codecs
        for idxCodec in "fixed" "gamma" "rice" "ifor" "zeta_3" "ief" "gvbyte" "ivbyte"
        do
            paste <(yes $p | head -n $n2) <(yes $m | head -n $n2) <(java $params $main $path $dataset $idxCodec fixed $n $fun $seed)
        done
    done
    done
done
