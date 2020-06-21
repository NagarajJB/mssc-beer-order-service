# mssc-beer-order-service

[![CircleCI](https://circleci.com/gh/NagarajJB/mssc-beer-order-service.svg?style=svg)](https://circleci.com/gh/NagarajJB/mssc-beer-order-service)


# Notes
* Made to load "Tasting Room" customer in the beginning for creating orders using BeerOrderBootstrap
* TastingRoomService is scheduled to keep creating orders

* local-discovery profile -> Enables service registering in Eureka as configured in LocalDiscoveryConfig.java using @EnableDiscoveryClient.

* localmysql profile disables registration with Eureka and spring cloud config client

* if running with localmysql and local-discovery use them as localmysql,local-discovery (latter takes precedence)

* zipkin is disabled in application.properties, spring.zipkin.enabled=false, use 'local' profile(from spring cloud config/repo) to enable zipkin.