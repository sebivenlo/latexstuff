all: main clean

main:
	pdflatex example.tex
	biber example
	pdflatex example.tex
	mv example.pdf example.pdf

clean:
	rm -f *.log *.lof *.lot *.toc *~ *.aux *.fls *.out 	*.bbl *.blg *.synctex.gz *.maf *.mtc *.mtc0 *.snm *.nav *.glo *.glsdefs 	*.ist *.vrb *.bcf *.run.xml

cleanall: clean
	rm -f example.pdf example.pdf
