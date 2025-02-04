
all: Report.pdf README.md

Report.pdf: Report.tex
	pdflatex -output-directory=./ Report.tex

clean:
	rm -f *.aux *.log *.out *.toc *.xml *.bcf
