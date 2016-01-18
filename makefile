all:
	javac -Xlint:none -encoding ISO-8859-1 -cp .:pdfbox ir/*.java

clean:
	rm *.class
