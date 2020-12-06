FROM openjdk
COPY target/sark-1.0.0-standalone.jar /sark.jar
CMD java -Xmx256m -Xms256m  -XX:-PrintGC -jar /sark.jar
