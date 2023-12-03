# StarsRus

Date created: December 2, 2023

## this is a project for:

University of California, Santa Barbara

CMPSC 174A - FUND DATABASE SYS

Fall 2023

## install & execute

1. install [Java Development Kit (JDK) 21](https://www.oracle.com/java/technologies/downloads/#java21)
2. clone the project
   1. `git clone https://github.com/ru1nw/starsrus.git`
3. extract the wallet for Oracle DB to the root of the project
4. fill in environment properties
   1. make a copy of `src/main/Util/TEMPLATE-AppProperties.java` in the same folder, name it `AppProperties.java`
   2. under `class AppProperties` in , fill in `PATH_TO_WALLET` and `DB_PASSWORD`
5. compile and execute: at the root of the project, run
   1. `chmod u+x compile run`
   2. `./compile src/main/App.java`
   3. `./run App`

## team member & contribution

- Jackson Cooley ([jacksonjude](https://github.com/jacksonjude))
- Ian Wen ([ru1nw](https://github.com/ru1nw))
  - [`src/main/java/edu/ucsb/cs174a/f23/UserInterface.java`](https://github.com/ru1nw/starsrus/blob/main/src/main/java/edu/ucsb/cs174a/f23/UserInterface.java)
  - [`src/main/java/edu/ucsb/cs174a/f23/Trader/TraderInterface.java`](https://github.com/ru1nw/starsrus/blob/376b45ccc38865508c19f365893ee06f396a2acc/src/main/java/edu/ucsb/cs174a/f23/Trader/TraderInterface.java)