#
# Makefile for the latex documentation of this project
# @author Pieter van den Hombergh number 879417
# $Id: Makefile,v 1.7 2005/10/17 13:16:29 hom Exp $
# latex command
LATEX=pdflatex
BIBTEX=bibtex
# define some flags
LATEXFLAGS=--interaction=batchmode
# How to produce the pdf from .tex files: 
# run pdfelatex to 
# get the references right.
# to process a Latex doc into a PDF document

.PRECIOUS : .eps .pdf
%.pdf: %.tex
	$(LATEX) $(LATEXFLAGS) $<
	$(BIBTEX) $*
	$(LATEX) $(LATEXFLAGS) $<
	$(LATEX) $(LATEXFLAGS) $<

%.eps: %.dia
	dia -n --export-to-format=eps $<

%.pdf: %.eps
	epstopdf $<


# first some definitions
BASENAME=main
TARGET=$(BASENAME).pdf
BARCODE=900451
PARTS=$(INCLUDES) $(FIGS) $(BIBFILES)

# things that may be thrown away 
DISPOSABLES= *~ *.aux *.log *.o hello *.log *.log *.bbl *.out *.toc *.url *.fls *.lol *.lof *.blg *.lot

# define the default target
all: $(TARGET) 

# pick up dependencies 
# to make this work, run make depend once in a while
include .depend


$(TARGET): $(BASENAME).tex .depend $(PARTS)

#define a phony target
.PHONY: clean distclean

clean:
	rm -f $(DISPOSABLES)

distclean: clean
	rm -f $(TARGET)

# barcode for diktaat
figures/barcode.pdf : 
	barcode -Ec -e 39 -b"$(BARCODE)" -o figures/barcode.eps
	epstopdf figures/barcode.eps -o $@

barcode: figures/barcode.pdf

depend:
	texdepdf $(BASENAME) > .depend

.depend: depend


# DO NOT DELETE
