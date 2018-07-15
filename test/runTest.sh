# <memory> <cpu> <dir>

if [ "$#" -ne 3 ]; then
    echo "Usage: $0 <memory_MB> <cpus> <output_dir>"
    exit -1
fi

echo "Output directory: $3"

exec 3< <(docker run \
-m ${1}m \
--memory-swap ${1}m \
--cpus=${2} \
-p 2181:2181 \
-p 9092:9092 \
-p 9999:9999 \
--env ADVERTISED_HOST=localhost \
--env ADVERTISED_PORT=9092 \
--env KAFKA_JMX_OPTS="-Dcom.sun.management.jmxremote=true -Dcom.sun.management.jmxremote.authenticate=false -Dcom.sun.management.jmxremote.ssl=false -Djava.net.preferIPv4Stack=true" \
--env JMX_PORT=9999 \
spotify/kafka)

line=""
while [[ true ]]; do
  read <&3 line; echo "$line";

  if [[ -z $line ]]; then
    echo "Kafka did not start"
    exit -4
  fi

  if [[ $line = *"port is already allocated"* ]]; then
    echo "Kafka port already in use"
    exit -3
  fi

  if [[ $line = *"kafka entered RUNNING state"* ]]; then
    break
  fi
done

echo "Running configuration:"
echo "  Memory: $1 MB"
echo "  CPU: $2"

echo "Running first run"
java -jar ../build/libs/kafkametrics-executable-1.0.jar ${3}/${1}_${2}_firstrun

echo "Running second run"
java -jar ../build/libs/kafkametrics-executable-1.0.jar ${3}/${1}_${2}_secondrun

echo "Cleaning the Kafka Docker container"
docker container rm -f -v $(docker ps -q --filter ancestor=spotify/kafka)