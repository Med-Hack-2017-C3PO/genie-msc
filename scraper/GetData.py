import urllib.request
from bs4 import BeautifulSoup
from selenium import webdriver

url = "https://www.ncbi.nlm.nih.gov/pubmed/?term=Rivastigmine"

def get_abstr(url):

	page = urllib.request.urlopen(url).read()
	soup = BeautifulSoup(page, 'lxml')

	abstr = soup.find('div', class_='abstr')

	if abstr:
		abstr_text = abstr.find('div', class_='').p.abstracttext.get_text()
	else:
		abstr_text = ''
	
	return abstr_text

def get_data(url):

	page = urllib.request.urlopen(url).read()
	soup = BeautifulSoup(page, 'lxml')
	articles = soup.find_all('div', class_='rprt')

	for article in articles:

		article = article.find('div', class_='rslt')
		title = article.p.a.get_text()
		abstr = get_abstr('https://www.ncbi.nlm.nih.gov' + article.p.a.get('href'))
		pmid = article.find('dl', class_='rprtid').dd.get_text()

		print(title, pmid)
		print(abstr)
		print()

get_data(url)
