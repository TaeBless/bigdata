#-*- coding: utf-8 -*-
from selenium import webdriver
from bs4 import BeautifulSoup
from time import sleep
import requests
import re
import csv


header = {'User-Agent': ''}
d = webdriver.Chrome('./chromedriver')
d.implicitly_wait(3)
result=list()
url = 'https://www.melon.com/search/dj/index.htm?q=%EC%BA%90%EB%A1%A4&section=title&searchGnbYn=Y&kkoSpl=N&kkoDpType=&ipath=srch_form#params%5Bq%5D=%25EC%25BA%2590%25EB%25A1%25A4&params%5Bsort%5D=date&params%5Bsection%5D=title&po=pageObj&startIndex='

for i in range(1,452,10):
    d.get(url+str(i))
    sleep(10)
    djlistID = []
    djlistName = []
    for i in range(1,11):
        djlistIDs = d.find_element_by_xpath('//*[@id="pageList"]/div/ul/li['+str(i)+']/div[1]/div/dl/dt/a')
        aID = djlistIDs.get_attribute("href")
        aName = str(djlistIDs.get_attribute("title")).split('-')[0].strip()
        djlistID.append(aID)
        djlistName.append(aName)

    for djlistID, djlistName in zip(djlistID, djlistName):
        d.get('https://www.melon.com/mymusic/dj/mymusicdjplaylistview_inform.htm?plylstSeq=' + str(djlistID).split('\'')[-2])
        sleep(5)

        for i in range(1, 51):
            try:
                title = d.find_element_by_xpath('//*[@id="frm"]/div/table/tbody/tr['+ str(i) +']/td[5]/div/div/div[1]/span/a').text
                print(title)
                if '19금' in title:
                    title = title.replace('19금', '')
                    title = re.sub('^\s*|\s+$', '', title)

                artist = d.find_element_by_xpath('//*[@id="frm"]/div/table/tbody/tr[' + str(i) + ']/td[5]/div/div/div[2]/a').text
                print(artist)
                print('*--------------------------------*')

                result.append({
                    'title':title,
                    'artist':artist
                })
            except:
                break

with open('./data/caroll.csv', 'w', encoding='utf-8', newline='') as f:
    c = csv.writer(f)
    c.writerow(['title','arits'])
    for i in result:
        c.writerow([i['title'],
                    i['artist']
                    ])
