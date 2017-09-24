""""# -*- coding: utf-8 -*-"""
from selenium import webdriver
from selenium.webdriver.common.keys import Keys
import requests
from bs4 import BeautifulSoup

query = "erythromycin" # "rivastigmine"
nextBtnTitle = "Next page of results"
url = "https://www.ncbi.nlm.nih.gov/pubmed/?term=" + query
#each page has 20
n = 1673
numRef = n * 20
urllist = []
title = ""
article_body = ""
pmid = 0
punctuation = [". ", "! ", "? "]


def formater(title, abstract, pmid):

	# do the utf-8 encoding at the time of file-write
	title = title#.encode('utf-8')
	abstract = abstract#.encode('utf-8')
	pmid = pmid#.encode('utf-8')

	# title
	fmtTitle = ""
	fmtTitle = "abstract=b'<d> <p> <s> {} </s> </p> </d>'".format(title)

	# abstract
		# replace ". " to avoid ruining decimal points

	for i in punctuation:
		# print("Replacing {}".format(i))
		# t = "{} </s> <s> ".format(i)
		abstract = abstract.replace(i, i + " </s> <s> ") # .format(i, i)

	abstract = abstract.replace("=", " equals ")
	fmtAbstract = 'article=b"<d> <p> <s> {} </s> </p> </d>"'.format(abstract)

	fmtPmid = "	publisher=b'{}'".format(pmid)

	return fmtTitle + fmtAbstract + fmtPmid

def parse_url(url):
	page = requests.get(url)
	soup = BeautifulSoup(page.content, "html.parser")
	
	article_title = soup.find("div", { "class" : "rprt abstract" }).h1.get_text()
	
	try:
		article_body = soup.find("div", {"class" : "abstr" }).div.get_text()
	except:
		article_body = ""

	article_pmid = soup.find("dl", {"class" : "rprtid" })
	pmid = ""
	for i in article_pmid.get_text().split(" ")[0:2]:
		pmid = pmid + i

	return article_title, article_body, pmid


#open browser
driver = webdriver.Firefox()
#driver = webdriver.Chrome()
#go to webpage
driver.get(url)

for i in range(n):
	#find all document links
	elems = driver.find_elements_by_xpath("//p[@class='title']/a")
	
	for elem in elems:
		#print each link url
		urllist.append(elem.get_attribute("href"))
	
	#go to next page
	elem = driver.find_element_by_xpath("//*[@title='" + nextBtnTitle + "']").click()
driver.close()


#open file

# use open( blah, blah, errors = 'replace') to replace unicode char with '?'
# use open( blah, blah, errors = 'ignore') to ignore errors associated with unicode.

# file name format [query]-[entry-count]_db.txt
with open("./" + query + "-" + str(numRef) + "_db.txt", mode = "w", encoding = "utf-8") as f:
	for url in urllist:
		title, body, pmid = parse_url(url)
		entry = formater(title, body, pmid)
		print(entry, file = f)

"""
f = open(query + '-db.html', 'w', encoding="utf-8")
for url in urllist:
	title, body, pmid = parse_url(url)
	f.write(title + body + pmid + '\n')
f.close()	
"""