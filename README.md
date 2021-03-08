# Overall
Core-processor is a Spring Statemachine configuration project. However, since it's not a standalone Spring Boot application, it can't be tested directly.

Core-processor-test is a sub-module inside, which is a stand-alone Spring Boot application, to test and verify the configuration.

# Develop, Build, Install and Test
- The project is developed using IntelliJ
- Maven install core-processor as a local artifactory, since it's a dependency for core-processor-test
- Run test inside core-processor-test

# Distributed SM with Redis Distributed Lock
- Docker run local Redis server
  ```
  cd core-processor-test
  docker-compose up -d
  ```
- Run Junit test