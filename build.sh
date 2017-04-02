rm ./dist/*.jar
mvn clean package -DskipTests
cp ./matebot-core/target/matebot-core-*-jar-with-dependencies.jar ./dist/matebot.jar

