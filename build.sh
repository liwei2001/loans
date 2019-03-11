docker build -f Dockerfile -t demo/oracle-java:8 .
docker build -f Dockerfile_mvn -t demo/maven:3.6-jdk-8 .
docker run -it --rm -v "$PWD":/app -w /app demo/maven:3.6-jdk-8 mvn package
docker run -it --rm -v "$PWD":/app -w /app demo/maven:3.6-jdk-8 java -cp target/loans-1.0.jar com.affirm.LoanApplication
