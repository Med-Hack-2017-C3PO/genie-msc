# cuylar
# for formating scraped pubmed abstracts and titles.

punctuation = [".", "!", "?"]

def Formater(title, abstract, pmid):
	
	title = title.encode('utf-8')
	abstract = abstract.encode('utf-8')
	pmid = pmid.encode('utf-8')

	# title
	fmtTitle = ""
        fmtTitle = "abstract=b'<d> <p> <s> {} </s> </p> </d>'".format(title)
	
	# abstract
        # replace ". " to avoid ruining decimal points
	counter = 0;
	while(counter < len(abstract)-1):
		try:
			indexo = punctuation.index(abstract[counter])
			if not abstract[counter+1].isdigit() or abstract[counter+1] is " ": 
				abstract = abstract[:counter] + "{}</s> <s> ".format(punctuation[indexo]) + abstract[counter+2:]
				counter = counter + 9

		except:
			dummyvar = 0;
		counter = counter + 1				


        fmtAbstract = 'article=b"<d> <p> <s> %s </s> </p> </d>"' % abstract

        fmtPmid = "	publisher=b'{}'".format(pmid)

        return fmtTitle + fmtAbstract + fmtPmid
