Alice (The Program)
=====

Built: Aug-Sept 2013, Maintained: Sept-Current. 

Alice is essentially a web-crawler/bot that runs a business on the social networking site, DeviantArt. Visit Alice's page at http://datrade.deviantart.com/.

Function:

This program facilitates trading equivalent to the sale of Like's on Facebook, or views on Youtube, except the transactions are wholly legitimate. On DeviantArt, there are counts for similar social currencies, and Alice manages both sides of the transaction, accepting payments from those who wish to buy them, and paying those who give them to the purchasers. This maintainence is far too tedious to perform by hand, and thus is only viable on a large scale by automation. Since Alice's construction, she has been extended to encompass numerous features of DeviantArt, from transacting followers (Watches), to reading critiques on artworks.

The program:

Although the program is not particularly long, it is quite dense. Virtually all of Alice's operations are multithreaded, with more than a dozen threads running at all times, peaking at several dozen at high loads. Alice has to also remember records of more than ten thousand users in its current state, and uses a MySQL database to maintain this scalably. Web interactions are done either via Java's URL object, for quick scraping of various pages, or by the Selenium Webdriver, a web-testing framework turned into a bot.

What's uploaded on the repository is only the source code: Alice requires many data files, external libraries, and a database to function properly.

Challenges:

Multithreading dozens of tasks to run concurrently was a daunting challenge, and it took some time to resolve all the deadlocks and race conditions across the numerous lines of code.
On a different front, maintaining a flexible design was also a challenge. The Alice of August, when it was first built, was about a fifth of the length of the one today, and was not too brilliantly. Some legacy code remains in the messier classes, but otherwise Alice is now fairly well-structured and easy to maintain.

Successes:

To date, Alice's services have netted revenues of more than $2,000 USD equivalent of DeviantArt's virtual currency. Profit margins are approximately 50%. She continues to run on 24/7 on a small computer and is maintained occasionally as bugs appear, the site updates, or users request new features.
