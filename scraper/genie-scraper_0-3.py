# genie-abstracts
# this scraper takes a standard pubmed query and a page count as inputs
# the output is a a csv file. col-1 = title, col-2 = abstract, col-3 = pmid
# The scraper ignores PMID's where no abstract is available.
# Several steps are taken to ensure that the text is formatted appropriately for the 'text-sum' NLP algorithm
# these include changing '=' to 'equals',
# and ensuring that punctuation is appropriately followed by a single space at the end of each sentence
# but punctuation for decimals and abbreviations should remain intact.
# the scraper is designed to write each entry to the csv file as it is queried,
# therefore in the event of a crash or loss of connection data to that point is retained.

import os
import re
import csv
import time
import requests
from selenium import webdriver
from bs4 import BeautifulSoup


query = '"data sharing" AND "genomics"'#"erythromycin" # "rivastigmine"
fPrefix = query.replace(" ", "_").replace('"', '') # compensate for file naming convention
nextBtnTitle = "Next page of results"
url = "https://www.ncbi.nlm.nih.gov/pubmed/?term=" + query
#each page has 20
pages = 2
numRef = pages * 20
title = ""
article_body = ""
pmid = 0
count = 1


def csv_formater(title, abstract, pmid):
    print("Formating CSV entry from url number", count)
    title = BeautifulSoup(title).text # remove html tags
    abstract = BeautifulSoup(abstract).text
    pmid = BeautifulSoup(pmid).text

    # handle characters that cause errors for ML-reader
    title = title.replace("[", "").replace("]", "")
    abstract = abstract.replace("=", " equals ")
    #abstract = abstract.replace("\n", " ") # new-line in the paragraph is problematic

    # This regex statement (?<=[A-Za-z0-9()[\]%])\.(?=[A-Za-z()[\]]{2})|(?<=[A-Za-z()[\]%]{2})\.(?=[A-Za-z0-9()[\]])
    # find all periods without a space after them, as in: (---best of times.It was the worst---),
    # but ignore decimals(12.3, 0.21), abreviations (N.Y., D.C.), and titles (Dr., Mr.)
    # the replacement value is ". "

    # build the regex as a string in-order to loop the list of punctuations.
    # find all punctuations without a space, but not numbers, and abreviations.
    punctuation = [".", "!", "?"]
    for punk in punctuation:
        repunk = re.escape(punk)
        punkregex = r"(?<=[A-Za-z0-9()[\]%])" + repunk + r"(?=[A-Za-z()[\]]{2})|(?<=[A-Za-z()[\]%]{2})" + repunk + r"(?=[A-Za-z0-9()[\]])"
        title = re.sub(punkregex, punk + " ", title)
        abstract = re.sub(punkregex, punk + " ", abstract)

    # join-split combo reduces all white space to single space, and eliminates trailing/leading space and \escape-char
    title = ' '.join(title.split())
    abstract = ' '.join(abstract.split())

    # remouve escape markers from '"Error: Failed to Scrape " + esc_url'; added in 'def parse_url(url):'
    title = title.replace("\\", "")
    abstract = abstract.replace("\\", "")
    pmid = pmid.replace("\\", "")

    entry = [[title, abstract, pmid]]
    return entry


def parse_url(url):
    print("Calling url number", count)
    page = requests.get(url)
    soup = BeautifulSoup(page.content, "html.parser")
    esc_url = re.escape(url)
    # if the information is not there, record Failure
    try:
        article_title = soup.find("div", {"class": "rprt abstract"}).h1.get_text()
    except:
        article_title = "Error: Failed to Scrape " + esc_url
    try:
        try: # find english abstr
            article_body = soup.find_all("div", {"class": "abstr_eng"})[0].get_text()
        except: # find any abstr
            article_body = soup.find("div", {"class": "abstr"}).div.get_text()
    except:
        article_body = "Error: Failed to Scrape " + esc_url
    try:
        article_pmid = soup.find("dl", {"class": "rprtid"})
        pmid = ""
        for i in article_pmid.get_text().split(" ")[0:2]:
            pmid = pmid + i
    except:
        pmid = "Error: Failed to Scrape " + esc_url

    return article_title, article_body, pmid


def write_entry(url):
    global count
    # make request to each url, parse format, and append
    title, body, pmid = parse_url(url)
    entry = csv_formater(title, body, pmid)

    # check for error, and only add to csv if error free
    if any("Error: Failed to Scrape " + url not in i for i in entry):
        # file name format [query]-[entry-count]_db.csv
        with open("./" + fPrefix + "-temp_db.csv", mode="a", encoding="utf-8") as f:

            writer = csv.writer(f, lineterminator = '\n')
            writer.writerows(entry)

        print("Writing CSV entry", count)
        count += 1
    else:
        pass # write to file only if there is no error. FUTURE: record and fix entries with errors


def main():
    global url
    start = time.time()
    # open browser
    driver = webdriver.Firefox()
    # go to webpage
    driver.get(url)

    for page in range(pages):
        # find all document links
        elems = driver.find_elements_by_xpath("//p[@class='title']/a")

        for elem in elems:
            # assign url
            url = elem.get_attribute("href")
            write_entry(url)  ## make request to each url, parse, format, and append
            checkTime = time.time()
            print("Elapsed time:", checkTime - start)

        # go to next page
        elem = driver.find_element_by_xpath("//*[@title='" + nextBtnTitle + "']").click()

    driver.close()
    print("Renaming temp_db file.")
    os.rename("./" + fPrefix + "-temp_db.csv", "./" + fPrefix + "-" + str((count - 1)) + "_db.csv")
    end = time.time()
    print("Total time:", end - start)


if __name__ == "__main__":
    main()
