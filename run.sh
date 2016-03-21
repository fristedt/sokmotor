#!/bin/sh
java -Xmx4096m -cp .:/info/DD2476/ir16/lab/pdfbox ir.SearchGUI "$@" -d /info/DD2476/ir16/lab/davisWiki
