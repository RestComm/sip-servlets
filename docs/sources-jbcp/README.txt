when mvn clean install has been performed in the parent directory, to validate against publican do :

cd ./src/main/resources
chmod 777 mkbk Makefile
./mkbk sss jbcp html-single-en-US

that's it
