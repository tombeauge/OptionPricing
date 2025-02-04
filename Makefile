
all: Report.pdf README.md

Report.pdf: Report.tex
	pdflatex -output-directory=./ Report.tex

README.md: Report.tex
	pandoc Report.tex -o README.md --from=latex --to=markdown

clean:
	rm -f *.aux *.log *.out *.toc *.xml *.bcf
