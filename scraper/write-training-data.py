import os


drug-query = "rivastigmine"




def build-entry(title, abstract, pmid):

    entry = "{} {} {}".format(title, abstract, pmid)


def write-entry(entry):

    # first check that the file exists, and contains data
    # checking in current directory "./[file-name]"
    if os.path.isfile("./{}.txt").format(drug-query) and
            os.stat("./{}.txt").format(drug-query).st_size != 0:

        old_file = open("./{}.txt", "r+").format(drug-query)

    # if the file does not exit, create it
    else:
        old_file = open("./{}.txt", "w+").format(drug-query)
        print("No file found, creating new file for {}.txt".format(drug-query)

    newfile = open("{}.txt", "w+").format(drug-query) # writes in the current directory by default

    newfile.write(entry)
    newfile.close()
