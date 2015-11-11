#!/bin/sh

m=2G
params="-Xmx${m}"
main="-jar target/recsys2015-1.0.jar Generate"

#datasets=("netflix" "ymusic")

datasets=("ml1M")

for (( i=0; i<${#datasets[@]}; i++ ))
do
    dataset=${datasets[$i]}
    path="${dataset}/"
    # no compression
    java $params $main $path $dataset "null" "null"
    # codecs
    for idxCodec in "fixed" "gamma" "rice" "ifor" "zeta_3" "ief" "ivbyte"
    do
        java $params $main $path $dataset $idxCodec "fixed"
    done
done
