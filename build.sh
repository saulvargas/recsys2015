#!/bin/sh

wget http://files.grouplens.org/datasets/movielens/ml-1m.zip

unzip ml-1m.zip

mkdir -p ml1M/preference-data
cut -f1 -d: ml-1m/ratings.dat | sort -u > ml1M/users.txt
cut -f3 -d: ml-1m/ratings.dat | sort -u > ml1M/items.txt
sed 's/::/\t/g' ml-1m/ratings.dat | cut -f1-3 > ml1M/ratings.data

rm -r ml-1m.zip ml-1m
