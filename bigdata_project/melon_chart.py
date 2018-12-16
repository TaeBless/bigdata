from selenium import webdriver
from bs4 import BeautifulSoup
from time import sleep
import requests
import re
import csv

header = {'User-Agent': ''}
d = webdriver.Chrome('./chromedriver')
d.implicitly_wait(3)
d.get('http://www.melon.com/chart/index.htm')
d.get("http://www.melon.com/chart/search/index.htm")
d.find_element_by_xpath('//*[@id="d_chart_search"]/div/h4[1]/a').click()

#연대선택
for i in range(1, 2):
    age_xpath = '//*[@id="d_chart_search"]/div/div/div[1]/div[1]/ul/li[' + str(i) + ']/span/label'
    age = d.find_element_by_xpath(age_xpath)
    age.click()

    #연도선택
    for i in range(1, 10):
        result = list()
        try:
            year_xpath = '//*[@id="d_chart_search"]/div/div/div[2]/div[1]/ul/li[' + str(i) + ']/span/label'
            year = d.find_element_by_xpath(year_xpath)
            year.click()
            print(year.text)
        except:
            print("year_xpath not found")
            break

            #월간선택
        for i in range(1,13):
            try:
                month_xpath = '//*[@id="d_chart_search"]/div/div/div[3]/div[1]/ul/li[' + str(i) + ']/span/label'
                month = d.find_element_by_xpath(month_xpath)
                month.click()
                print(month.text)
            except:
                print("month_xpath not found")
                break

            #주간선택
            for i in range(1,6):
                try:
                    week_xpath = '//*[@id="d_chart_search"]/div/div/div[4]/div[1]/ul/li[' + str(i) + ']/span/label'
                    week = d.find_element_by_xpath(week_xpath)
                    week.click()
                    print(week.text)
                except:
                    print("week_xpath not found")
                    break

                #장르/스타일선택
                classCd = d.find_element_by_xpath('//label[@for = "gnr_1"]')
                if '가요' not in classCd.text:
                    classCd = d.find_element_by_xpath('//label[@for = "gnr_2"]')

                classCd.click()
                d.find_element_by_xpath('//*[@id="d_srch_form"]/div[2]/button/span/span').click()
                sleep(10)
                song_ids = d.find_elements_by_xpath('//*[@id="lst50"]/td[4]/div/a')
                song_ids = [re.sub('[^0-9]', '', song_id.get_attribute("href")) for song_id in song_ids]
                ranks = d.find_elements_by_xpath('//*[@id="lst50"]/td[2]/div/span[1]')

                for rank, song_id in zip(ranks, song_ids):
                    sleep(5)
                    print(song_id)

                    req = requests.get('http://www.melon.com/song/detail.htm?songId=' + song_id, headers = header)
                    html = req.text
                    soup = BeautifulSoup(html, "html.parser")

                    title = soup.find(attrs={"class": "song_name"}).text.replace('곡명', '').strip()
                    if '19금' in title:
                        title = title.replace('19금', '')
                        title = re.sub('^\s*|\s+$','', title)

                    artist = soup.find(attrs={"class": "artist_name"}).text

                    for pagenum in range(1):
                        print(pagenum)
                        d.get('https://www.melon.com/song/detail.htm?songId=' + song_id + '#cmtpgn=&pageNo=' + str(pagenum+1) + '&sortType=0&srchType=2&srchWord=')
                        element1 = d.find_element_by_class_name('list_cmt')
                        reviews = element1.find_element_by_class_name('cmt_text d_cmtpgn_cmt_full_contents').text

                        print(reviews)

                    result.append({
                        'title': title,
                        'artist': artist
                    })
                    print("제목:", title)
                    print("*_*_*_*_*_*_*_*_*_*_*__*_*_*")
        with open('./data/melon_chart' + year.text + '.csv', 'w', encoding='utf-8', newline='') as f:
            c = csv.writer(f)
            for i in result:
                c.writerow([i['title'],
                            i['artist']
                            ])
