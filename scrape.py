#!/usr/bin/env python
import urllib2
from bs4 import BeautifulSoup
from selenium import webdriver

from formater import *

url1 = "https://www.ncbi.nlm.nih.gov/pubmed/28938490"
url2 = "https://www.ncbi.nlm.nih.gov/pubmed/?term=Rivastigmine"
def parse_url(url):
	page = urllib2.urlopen(url)	
	soup = BeautifulSoup(page, "html.parser")
	
	article_title = soup.find("div", { "class" : "rprt abstract" }).h1.get_text()
	
	try:
		article_body = soup.find("div", {"class" : "abstr" }).div.get_text()
	except:
		article_body = ""

	article_pmid = soup.find("dl", {"class" : "rprtid" })
	pmid = ""
	for i in article_pmid.get_text().split(" ")[0:2]:
		pmid = pmid + i

	return article_title,article_body,pmid

def get_urls(url):
	page = urllib2.urlopen(url)
	soup = BeautifulSoup(page, "html.parser")

	titles = soup.find_all("p", { "class" : "title" })

	for link in titles:
		link = link.a
		link = 'https://www.ncbi.nlm.nih.gov' + link.get('href')
		t = []
		counter = 1
		for i in parse_url(link):
#			print i
			print counter
			t.append(i)
			counter = counter + 1#		
		print Formater(t[0],t[1],t[2])
			
def get_urls2(url):
	options = webdriver.SafariOptions()
	options.add_argument('--ignore-certificate-errors')
	options.add_argument("--test-type")
	options.binary_location = "/usr/bin/chromium"
	driver = webdriver.Chrome(chrome_options=options)
	driver.get('http://codepad.org')

	t = driver.find_element_by_xpath("//p[@class='title']")
	t.find_element_by_xpath("/a")
	print t.text


#t = parse_url(url1)
#for i in t:
#	print i


#search_term = raw_input("what would you like to search for?")

get_urls(url2);
