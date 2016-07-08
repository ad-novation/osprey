#!/usr/bin/env sh
if [ $# -eq 0 ]; then
    echo "Please provide JAR path"
    exit 1
fi

mkdir -p ./dist

rm ./target/osprey 2> /dev/null
cat ./deployment/base.sh > ./dist/osprey
cat $1 >> ./dist/osprey
chmod +x ./dist/osprey

echo "Built osprey in ./dist/osprey"
