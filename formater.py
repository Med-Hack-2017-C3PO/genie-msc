# cuylar
# for formating scraped pubmed abstracts and titles.

punctuation = [". ", "! ", "? "]

class Formater(self, title, abstract, pmid):

    def __init_(self):
        self.title = title
        self.abstract = abstract
        self.pmid = pmid

        # title
        self.fmt-title = "abstract=b'<d> <p> <s> {} </s> </p> </d>'".format(self.title)

        # add sentance flags, and remouve null sentance artifacts
        # replace ". " to avoid ruining decimal points

        # loop the abstract for each of [". ", "! ", "? "]
        for i in punctuation:
            self.fmt-period = self.abstract.replace("{}", "{}</s> <s>").format(i, i)

        #self.fmt-period = self.abstract.replace(". ", ". </s> <s>")
        self.fmt-period = fmt-period.replace("</s> <s>", "</s>")

        # abstract
        self.fmt-abstract = 'article=b"<d> <p> <s> {} </s> </p> </d>"'.format(fmt-period)

        # pmid
        self.fmt-pmid = "publisher=b'{}'".format(self.pmid)

        #self.entry = "{} {} {}".format(self.fmt-title, self.fmt-abstract,
        #                               self.fmt-pmid)

        # return self.entry
        return fmt-title, fmt-abstract, fmt-pmid
