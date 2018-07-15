DIR=`pwd`
cd ..
gradle fatJar
cd $DIR

./runTest.sh 128 0.25 results
./runTest.sh 128 0.5 results
./runTest.sh 128 1 results
./runTest.sh 128 2 results
./runTest.sh 128 3 results

./runTest.sh 256 0.25 results
./runTest.sh 256 0.5 results
./runTest.sh 256 1 results
./runTest.sh 256 2 results
./runTest.sh 256 3 results

./runTest.sh 512 0.25 results
./runTest.sh 512 0.5 results
./runTest.sh 512 1 results
./runTest.sh 512 2 results
./runTest.sh 512 3 results

./runTest.sh 1024 0.25 results
./runTest.sh 1024 0.5 results
./runTest.sh 1024 1 results
./runTest.sh 1024 2 results
./runTest.sh 1024 3 results