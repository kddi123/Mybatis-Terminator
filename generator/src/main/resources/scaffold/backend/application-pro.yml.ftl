# 生产环境：密改环境变量注入，最小日志输出
spring:
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
    url: ${MYSQL_URL:jdbc:mysql://localhost:3306/hr_management?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true}
    username: ${MYSQL_USERNAME:root}
    password: ${MYSQL_PASSWORD:root}
  data:
    redis:
      host: ${REDIS_HOST:localhost}
      port: ${REDIS_PORT:6379}
      database: 0
      timeout: 5s

logging:
  level:
    ${basePackage}: warn
    org.apache.ibatis: warn
