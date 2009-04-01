mvn clean install
cp target/*.war ${JBOSS_HOME}/server/port-1/deploy
cp target/*.war ${JBOSS_HOME}/server/port-2/deploy
