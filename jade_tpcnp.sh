cd src/java

javac -cp /home/cleber/jason-2.1/libs/jade-4.3.jar:/home/cleber/Projetos/tp_cnp/src/java/jadeagents:. jadeapp/StartJade.java jadeagents/Initiator.java jadeagents/Participant.java jadeagents/Rejector.java 


% valgrind --tool=memcheck java -cp /home/cleber/jason-2.1/libs/jade-4.3.jar:/home/cleber/Projetos/tp_cnp/src/java/jadeagents:. jadeapp.StartJade
time java -cp /home/cleber/jason-2.1/libs/jade-4.3.jar:/home/cleber/Projetos/tp_cnp/src/java/jadeagents:. jadeapp.StartJade

cd ../..
