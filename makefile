all:
	javac -Xlint:none -encoding ISO-8859-1 -cp .:/info/DD2476/ir16/lab/pdfbox ir/*.java

clean:
	rm *.class
