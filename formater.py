 # cuylar
# for formating scraped pubmed abstracts and titles.

punctuation = [". ", "! ", "? "]

def Formater(title, abstract, pmid):

        # title
	#print fmtTitle
	

	title = title.encode('utf-8')
	abstract = abstract.encode('utf-8')
	pmid = pmid.encode('utf-8')

	fmtTitle = ""
        fmtTitle = "abstract=b'<d> <p> <s> %s </s> </p> </d>'" % title

        # add sentance flags, and remouve null sentance artifacts
        # replace ". " to avoid ruining decimal points
        # loop the abstract for each of [". ", "! ", "? "]
	print "!!!!!!\r\nAbstract:%s\r\n!!!!!!!\r\n" % abstract

        for i in punctuation:
		print "Replacing %s" % i
		t = "%s</s> <s>" % i
		fmtAbstract = abstract.replace(". ", t)

        #self.fmt-period = self.abstract.replace(". ", ". </s> <s>")
        #self.fmt-period = fmt-period.replace("</s> <s>", "</s>")

        # abstract
        fmtAbstract = 'article=b"<d> <p> <s> %s </s> </p> </d>"' % abstract

        # pmid
        fmtPmid = "publisher=b%s" % pmid

        #self.entry = "{} {} {}".format(self.fmt-title, self.fmt-abstract,
        #                               self.fmt-pmid)

        # return self.entry
	returnval = fmtTitle
	returnval = returnval + fmtAbstract
	returnval = returnval + fmtPmid
        return returnval
