# Pluto
A stock exchange monitoring application

<div dir="ltr" style="text-align: left;" trbidi="on">
<div style="text-align: justify;">
<b><br /></b></div>
<div style="text-align: justify;">
In 2009, I got engaged in the role of an architect for building a matching engine for a security exchange. While it was a worthy challenge from a non-functional standpoint, I was equally fascinated by the the roller coaster ride of stock trading.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
Unfortunately, every time I get enthused about something - my developer itch takes over. So in 2008, while I was preparing to work on the exchange solution, I built an open source application - Pluto.&nbsp;<b>Pluto</b>&nbsp;simple put, is a market tracking and visualization program</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
I am moving the codebase of Pluto to GitHub, in hope that it can be used/enhanced in a community. The integration modules of the component would need revamp since the exchange has changed the structure of data exchange.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="320" src="http://4.bp.blogspot.com/-dVsJWPEZteY/VjYL6V6TqNI/AAAAAAAABpI/432t2OstGH4/s320/pluto.png" width="238" /></div>
<div style="text-align: justify;">
<b>Pluto</b>&nbsp;was conceptualized more from need than from necessity. My jump into online market trading was motivated purely by the fact that my next professional engagement was for a stock exchange. Well, I started pretty green behind the ears and thought the only way to accelerate understanding of this new domain was to put some personal motivation at stake and what better motivation than putting some of my hard earned dough on the turbulent waters of the current day market.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
I entered right when the market was buckling and little webs of cracks were forming. This was some time before the October 2008 panic had hit the economy. I felt almost like an amateur astronomer who on his first night at the telescope found himself staring at a supernova in the sky. Everyone was talking about the markets, starting from my colleagues down to our local grocer. Not to mention that bleeding that my market funds were going through.&nbsp;</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
To start with, my investments were all random. I used to pick up tips about good and bad stocks at the lunch table and spent the rest of the time watching my money being swept away. The most frustrating part was that my fingers had started aching pressing the refresh button on the browser to track the last traded price. Then came one or two lucky investments and I recovered enough money after paying the brokerage charges. The excitement was almost like the time when I had earned my first pay cheque.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
Soon the realization set in that random investments and pressing the F5 button were not going to work. I needed to have a broader perspective of the market, have real-time and offline access to the market statistics and have mechanisms to visualize the market data from multiple perspectives. Online systems like yahoo finance, etc are OK, but they don't expose real real-time data (unless you pay) or high end visualization mechanisms. There were commercial tools and data feeds but I was in no mood for big investments.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
And then my itchy fingers beckoned. It has been months since I had ventured on one of my personal pet projects and this was too much of a seduction to refuse. So the journey began, two months ago when I started conceptualizing Pluto. It was clear from the beginning that Pluto is going to be big and the development is going to take months and thousands of lines of code. To add to the complication, were my green ears - I had no clue what I was trying to build :D. Fortunately, this was not the first time I was facing this scenario. As a software architect, every new engagement starts with exactly the same feelings. All it meant was that Pluto was not a quickie, I had to really focus on building it. All it meant was that Pluto needed thought, and lots of it. Since the requirements were to unfold during the journey, the base design was to be stable and enough decoupled as functional layers were slapped onto the existing core.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="242" src="http://4.bp.blogspot.com/-zjuwpA6zZMw/VjYL6I3T7EI/AAAAAAAABqQ/ZKerMqHWvvw/s320/pluto-screenshot.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<br /></div>
<br />
<div style="text-align: center;">
</div>
<div style="text-align: justify;">
The image above shows Pluto as it stands now. You can click on the image for a better resolution picture. In its current state pluto is already in production and tracking 178 index lined scrips on an intra-day basis and more than 2700 scrips on an end of day basis. Along with this, Pluto also tracks 7 indexes on a intra-day and end of day basis. The intra-day data resolution is around 30 seconds, with features of back filling high resolution intra day data both for indexes and index linked scrips. Pluto does not require commercial data feeds or any other paid licenses. It scourges on data freely available on the internet and sends spiders to leech them onto its private Postgres database.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
Pluto's features can be better explained from an user interface perspective. The significant parts of the user interface are as described below. The following list is not exhaustive, I will keep updating the list as and when newer functionality is implemented.</div>
<ol>
<li style="text-align: justify;">Status bar</li>
<li style="text-align: justify;">Workspace</li>
<ol type="i">
<li style="text-align: justify;">Desktop</li>
<li style="text-align: justify;">Quick launch toolbar</li>
<li style="text-align: justify;">Menu bar</li>
<li style="text-align: justify;">Status bar</li>
<ol type="a">
<li style="text-align: justify;">Window manager</li>
<li style="text-align: justify;">Network monitor</li>
<li style="text-align: justify;">Index watch</li>
</ol>
</ol>
<li style="text-align: justify;">Internal frames</li>
<ol type="i">
<li style="text-align: justify;">Configuration dialog</li>
<ol type="a">
<li style="text-align: justify;">Networking configuration</li>
<li style="text-align: justify;">Service console</li>
</ol>
<li style="text-align: justify;">Log window</li>
<li style="text-align: justify;">Scrip intraday table</li>
<li style="text-align: justify;">Scrip end of day table</li>
<li style="text-align: justify;">Index intraday table</li>
<li style="text-align: justify;">Chart window</li>
<ol type="a">
<li style="text-align: justify;">Single entity charts</li>
<li style="text-align: justify;">Multi entity comparision charts</li>
</ol>
</ol>
<li style="text-align: justify;">Integrated news feed reader</li>
<li style="text-align: justify;">Batch processes</li>
</ol>
<br />
<h5 style="text-align: justify;">
<span style="font-size: large;">Status bar&nbsp;&nbsp;</span></h5>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" src="http://1.bp.blogspot.com/-x3s3rqUzsYg/VjYL9mrHbfI/AAAAAAAABqI/xl53g_yMDoc/s1600/windows-statusbar.gif" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
Pluto lives in the computer as a service and makes its presence felt via a currency icon in the task bar. A red or green circle on the task bar icon identifies whether Pluto is online or offline at any instant. User can choose to right click on the task bar icon and force the application to go offline. Pluto is installed as a windows service to ensure that it works autonomously and without user intervention. Pluto has intelligence to auto detect network changes and restart its background services like fetching live data, end of day reports, market news etc without requiring any intervention. Similarly it can detect network outages and go into statis mode to be wakened upon the availability of the network.</div>
<br />
<h5 style="text-align: justify;">
<span style="font-size: large;">Workspace&nbsp;</span>&nbsp;</h5>
<div style="text-align: justify;">
Double clicking on the task bar icon brings forth the workspace for Pluto, which provides an avenue for the user to visualize the collected data and track values in a real time manner. The workspace by default opens in a maximized window. The screen shot below shows how an empty workspace looks like. The workspace has four fundamental regions, the desktop, menubar, quick launch bar and the status bar. Each of the components of a workspace is described below.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="193" src="http://3.bp.blogspot.com/-hbetw0IOLwc/VjYL5MualHI/AAAAAAAABok/ujhITfz_1_o/s320/pluto-desktop.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
</div>
<h5 style="text-align: justify;">
<span style="font-size: small;">[Workspace] Desktop</span>&nbsp;&nbsp;</h5>
<div style="text-align: justify;">
The desktop is a multi document interface, which manages multiple internal frames. Region (5) shown in the above diagram is the desktop for Pluto. The internal frames can be multiple instances of charts, tables, reporting or configuration dialogs. The different types of internal frames supported by Pluto will be explained later. The desktop is sensitive to mouse drops. Pluto supports extensive drag and drop capabilities, where the user can drag symbols and indexes from multiple windows and drop them on either pre existing charting windows or directly to the desktop. The desktop recognizes such drops and opens a intra day charting window for the dropped entity.</div>
<h5 style="text-align: justify;">
<span style="font-size: small;">[Workspace] Quick launch toolbar&nbsp;&nbsp;</span></h5>
<div style="text-align: justify;">
Quick launch toolbar (Region [1] shown in the workspace image), aggregates commonly used functionality as image icons on a side bar. The same features can be accessed via the menu system, but the quick launch toolbar is more usable for frequently used commands like show configuration dialog, log windows, intra day summary reports etc. Each hot spot on the quick launch toolbar provides rollover tooltips for command identification. The toolbar houses only a select subset of the commands supported by the menu system.&nbsp;<i>In the future, the functionality of letting the user configure the quick launch toolbar buttons would be provided.</i></div>
<h5 style="text-align: justify;">
<span style="font-size: small;">[Workspace] Menu bar&nbsp;&nbsp;</span></h5>
<div style="text-align: justify;">
Menu bar is a menu system which aggregates menu based classification of the user initiated actions supported by Pluto.</div>
<h5 style="text-align: justify;">
<span style="font-size: small;">[Workspace] Status bar&nbsp;</span>&nbsp;</h5>
<div style="text-align: justify;">
</div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="12" src="http://4.bp.blogspot.com/-8oci7C6C6bo/VjYL9TiLgcI/AAAAAAAABqA/CVPFeEuXfmk/s320/statusbar.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
The status bar is an important component of Pluto which holds components displaying important real time information. For now, the components of the status bar are statically configured, but in the future the user will be given a chance to configure the items important to him/her. Currently the status bar holds a digital clock, network monitor, intra day multi index ticker and a window manager.</div>
<div style="text-align: justify;">
<b>Network monitor</b>&nbsp;- The network monitor (Region 3) displays real time information on the network usage of Pluto. It shows a chart summarizing information regarding the last 30 network attempts by Pluto. The chart shows a bar for each invocation, the color of the bar provides information regarding the success or failure of the network attempt while the height of the bar provides a clue regarding the relative volume of the network traffic. The adjacent light, shows whether Pluto is currently working online or offline. The network monitor also provides information regarding the total volume of data downloaded in bytes, KB and MB, since Pluto was last started.</div>
<br />
<div style="text-align: justify;">
<b>Intra day multi index tracker</b>&nbsp;-&nbsp;</div>
<br />
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" src="http://4.bp.blogspot.com/-Bd4LHvOjxUE/VjYL4ErwHDI/AAAAAAAABoI/6og2X0Bzwi8/s1600/index-tracker.gif" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
The intra day, real time, multi index monitor (Region 4) displays real time information on the indexes being tracked by Pluto. Pluto tracks 9 realtime indexes exposed by NSE on a 30 second resolution. This status bar component can be used to provide a visual marker for the currently tracked index. The user can choose the index being tracked by selecting from the index name popup associated with the eject button to the left. If Pluto is working in an offline mode, this component displays the last index value of the specified index. Please note that depending upon whether the index is in the +ve or -ve range, the color of the points and percentage values change from green to red. This component shows the current value, the point difference since the last end of day closing value and the percentage of change for the currently tracked index.</div>
<br />
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<b>Window manager</b>&nbsp;- The user can open many internal frames. The window manager component on the status bar, provides management capabilities for the windows being managed by Pluto's desktop by providing categorized popup menus, where each menu item corresponds to an internal window. If a window has been minimized the window manager's menu item can be used to restore it. The window manager's menu items can also be used to bring a particular window to the front.</div>
<br />
<h5 style="text-align: justify;">
<span style="font-size: large;">Internal frames&nbsp;&nbsp;</span></h5>
<div style="text-align: justify;">
<b>Scrip intra day table</b>&nbsp;- Pluto supports tracking of index linked Scrips on an intra day basis. At present there are 147 index linked scrips that are being tracked by Pluto during business hours. The image below shows a screenshot of the scrip intra day panel.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" src="http://1.bp.blogspot.com/-o0g25HHu86g/VjYL3V7Ow_I/AAAAAAAABo4/xzta4Sv8Vy4/s1600/index-itd-panel.gif" /></div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="320" src="http://2.bp.blogspot.com/-YkYFqazGiR0/VjYL8xSkLUI/AAAAAAAABp8/EDrZrunL5Y4/s320/scrip-itd-panel.gif" width="264" /></div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="224" src="http://2.bp.blogspot.com/-Fqwd5k2n0WA/VjYL8ejoD_I/AAAAAAAABpw/Fe56GCzG9C8/s320/scrip-itd-panel-query.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
The table is updated every 30 seconds with the latest intra day values of the scrips. Each row displays the latest values of a scrip. The values displayed are the percentage change from last closing price, percentage change from today's opening price, the current price, total traded quantity. The table also has the option of showing the daily high and low values. As you can see, the cells in the table are color coded based on their current values in relation to the last closing value. The table also supports sorting on each column by clicking on the column header. The table supports a filter query language which can be used to select a subset of scrips. The query language is kept very flexible with multiple mappings for keywords for example to show all the scrips with a percentage EOD change of more than 5%, you can write&nbsp;<code><span style="color: blue;">%chg &gt; 5</span></code>. The image on the right shows how a query to filter all scrips with bank in their name and whose total traded quantity &gt; 200000 should be displayed.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
The intra day table also supports auxiliary actions associated with the selected scrips. The user can select one or more scrip rows in the table and click on the chart icon on the title bar to open a comparative chart for all the selected scrips. Pluto also stores the captured data into a persistent data store for offline working. By default Pluto fetches the intra day values at a resolution of 30 seconds, however the user can choose the enrich the past data with a resolution of 6 seconds by clicking on the "enrich" button to the right of the charting button.</div>
<div style="text-align: justify;">
The intra day panel also supports drag &amp; drop and double click charting. The user can drag any row onto another chart or the desktop to popup a chart summarizing all the intra day values (price and volume) for the particular scrip.</div>
<div style="text-align: justify;">
<b><br /></b></div>
<div style="text-align: justify;">
<b>Scrip end of day table</b>&nbsp;- While Pluto tracks all the index linked scrips for intra day values with a 30 second resolution, Pluto also tracks more than 1250 stocks for their end of day values. Pluto maintains a historical database of more than 10 years of historical end of day data. Unlike intra day data, the end of day values are not dynamic through the day and because of their sheer number, they pose challenges in visualization. Pluto, mitigates the problem by providing the End Of Day panel as shown in the screen shots below.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="309" src="http://3.bp.blogspot.com/-QgRMfBpaP6w/VjYL76giFMI/AAAAAAAABpo/wmrLTsjfhbI/s320/scrip-eod-panel.gif" width="320" /></div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="279" src="http://1.bp.blogspot.com/-wdfPTmDQtXY/VjYL7iqF7MI/AAAAAAAABpU/rhbm9cKX8nM/s320/scrip-eod-panel-graph.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
As you can see (left image), the EOD panel is much like the intra day panel, except that it provides some extra visualization tools. Primary of them being the last 10 day percentage change summary in a color coded grid (region 2). This small visualization tool suddenly transforms a row into a two dimensional conduit, relaying information about the behavior of the stock over the last 10 day. Of course, you can do all the common operations available for the intra day stocks, like double clicking a row and opening a chart, selecting multiple stocks and opening a comparative chart. This table also supports rollover tooltips which show the full name of the company, which the exchange symbol represents. You can also find the associated ICICI Direct code in the same row. This panel supports searching in pretty much the same way as it is supported in the intra day table with the additional advantage that a description can match against either the NSE scrip name, ICICI Direct scrip name or the actual company name. Most of these scrips are not index linked, but you can still select one or more stocks and fetch their intra day summary (needs connectivity).</div>
<div style="text-align: justify;">
The screenshot on the right show how the percentage change grid maps to the value on a chart.</div>
<div style="text-align: justify;">
<b><br /></b></div>
<div style="text-align: justify;">
<b>Index intra day table</b>&nbsp;- Pluto supports tracking of nine intra day indexes. The user can click on the "Visualization &gt;&gt; Index ITD Panel" menu item to display the index intra day panel as shown in the screen shot below. The index intra day panel supports all the features of the scrip intra day panel, sorting, charting, retrospective enrichment ,color coding and drag and drop charting. However, the support for a user defined query is not provided simply because there are only nine rows.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" src="http://1.bp.blogspot.com/-o0g25HHu86g/VjYL3V7Ow_I/AAAAAAAABo4/xzta4Sv8Vy4/s1600/index-itd-panel.gif" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<b>Chart window</b>&nbsp;- Pluto supports a very versatile charting environment, which adapts itself to multiple entities (scrips, indexes, technical indicators). The charting environment supports the following features:</div>
<ul>
<li style="text-align: justify;">Drag and drop support - Any scrip and/or index can be dragged from a scrip or index panel and dropped onto a chart</li>
<li style="text-align: justify;">Multi time range charting - The chart supports a time range of 1 day, 2 days, 3 days, 4 days, 5 days, 2 weeks, 1 month, 3 month, 6 month and 1 year time range</li>
<li style="text-align: justify;">Comparative mode - Once more than one entities have been dropped on the chart canvas, the chart switches itself to comparative mode instead of absolute mode</li>
<li style="text-align: justify;">Color coded entities - The chart displays each entity in a different color. With the color legends shown in the top left drop down.</li>
<li style="text-align: justify;">Cross hairs - Once a chart is selects, it converts the mouse pointer to a cross hair, the value of the cross hair pixel is translated into the chart relative values and shown in the lower left corner.</li>
<li style="text-align: justify;">Volume charts for single scrips - If the chart contains only one entity and the entity is a scrip. The chart shows the volume chart as a thin bar chart in the bottom of the chart.</li>
<li style="text-align: justify;">Visual area control - The chart has controls to switch off the control panel and volume chart to maximize on the charting area</li>
<li style="text-align: justify;">End markers - depending upon the time range, the chart highlights end of day, end of week and end of month values</li>
<li style="text-align: justify;">Technical indicators - Currently Pluto is being enhanced to support multiple technical indicators for each entity. A multitude of technical indicators like MA, EMA, SMA, Bollinger Bands etc will be added in the near future</li>
<li style="text-align: justify;">Dynamic intra day rendering - It should be noted that the chart auto refreshes itself with intra day values as and when Pluto catures them
</li>
</ul>
<div style="text-align: justify;">
The following screenshots show the charting windows of Pluto inaction.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="240" src="http://4.bp.blogspot.com/-bBwp5SglgeI/VjYL8Amsc3I/AAAAAAAABpk/yB8JO6A3YGE/s320/scrip-graph.gif" width="320" /></div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="241" src="http://2.bp.blogspot.com/-WrE3P-5kDsY/VjYL3X4Yi2I/AAAAAAAABoA/8lDQVRbY__k/s320/comparative-graph.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="178" src="http://2.bp.blogspot.com/-HNKawzoVs1k/VjYL3W8QtJI/AAAAAAAABog/c7tlP9huGsE/s320/10years.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<br />
<h5 style="text-align: justify;">
<span style="font-size: large;">Integrated news feed reader</span>&nbsp;&nbsp;</h5>
<div style="text-align: justify;">
Happenings around the world which translate themselves into news have a significant impact on the market dynamics. The faster the information reaches the investor, the better he or she is prepared to anticipate changes in the market. Most of us spend time pouring over newspaper over morning tea or catching up near the office pantry. Wouldn't it be nice if news could be delivered right on your screen. Nothing new about it, news feeds have been there for a long time. It was time, Pluto supported configurable news feeds riveted with it's background service mechanism. Pluto now supports a configurable number of news feeds, polling for fresh news at a configured interval and delivering them right amidst the charts and figures. Pluto's news feed and display also supports opening up of feed details in a system browser. Pluto also supports news notification via the system task bar icon, which pops up a message when any new news items arrive.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
</div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="213" src="http://3.bp.blogspot.com/-uyLUeYBvcqM/VjYL7btP8SI/AAAAAAAABqM/5A1q1IIoB0M/s320/rss-reader.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<b><span style="font-size: large;">Batch&nbsp;</span></b><span style="text-align: left;"><span style="font-size: large;"><b>Processes</b></span></span></div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="320" src="http://3.bp.blogspot.com/-6wOOMjrz8gg/VjYL4YZgXfI/AAAAAAAABoU/uYUtbDuCtbo/s320/job-management.gif" width="306" /></div>
<br />
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="252" src="http://2.bp.blogspot.com/-x4kAPmXtSYc/VjYL4vnDwnI/AAAAAAAABoQ/Ufz-ijvteus/s320/log-window.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<br /></div>
<br />
<div style="text-align: justify;">
<h4>Technology bla bla</h4>
<div style="text-align: justify;">
<span style="font-size: large;"><br /></span></div>
<div style="text-align: justify;">
The core of Pluto's design is based on two fundamental principles - loose coupling and usability. Of course all the other design related song and dance is present, but the above two factors are given the highest importance. This is because, for Pluto the future is unknown (<i>Pluto evolves and my understanding of the market evolves</i>) and the only way this can be mitigated is by designing it on the lego principle, I should be able to strip off blocks and rebuild them again with equal ease. The focus on usability is primarily on the user interface front. Since Pluto is centered around data visualization and analysis, usability is the only way to assimilate the data that Pluto gathers.</div>
<div style="text-align: justify;">
<br /></div>
<br />
<div style="text-align: justify;">
The diagram on the right shows a very high level structure of Pluto's internals. This is an over simplified diagram but proves the point that Pluto is based on well proven architecture comprising of layers and tiers and an unwavering focus on reuse.</div>
<div style="text-align: justify;">
<br /></div>
<div class="separator" style="clear: both; text-align: center;">
<img border="0" height="212" src="http://2.bp.blogspot.com/-mXEaRcJWFH0/VjYL48R6DoI/AAAAAAAABoc/SaSqCJwHXW0/s320/pluto-design.gif" width="320" /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
The design of Pluto has also evolved with the realization that within a subnet, Pluto is most economically operated in a client server architecture, where the server comprises of the persistent layer and data fetching logic. Each deployment of Pluto's server is going to require GBs of data space to store the end of day values of stocks for the last 20 years (or more), not to mention the network bandwidth requirement. It is estimated that on an average Pluto downloads around 70 MB of data. Keeping this in mind, the communication between the client and the server has been kept extremely loosely coupled which will facilitate deploying only the client as a separate application, applet or for that matter a WebStart application. Spring is used heavily to achieve the decoupling in terms of interface based dependency injection.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
Pluto's data capture algorithms heavily rely upon screen scraping technologies. At present the scaping is inbuilt using custom string manipulation code, which is not quite resilient towards HTML layout changes in the page. However, the data extraction logic from the scraped contents is well isolated. The intention is to replace the custom logic with WebHarvest and Solvent at a later date. WebHarvest and Solvent use XQuery extensively on the HTML DOM, making it easy to extract data and resilient to string changes in the HTML.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
An integral part of Pluto's internals is a job subsystem. As I have mentioned earlier, pluto is designed to be autonomous and functioning with minimal user intervention. It is for this reason that Pluto resides as a background daemon (in Windows as an operating system service). To function on it's own, Pluto relies on a barrage of cron triggered jobs, which get triggered at predefined intervals. The set of jobs include logic to fetch end of day data, intra day data, archival of old records, checking the network status etc. The cron triggering functionality is implemented leveraging Quartz.</div>
<div style="text-align: justify;">
<br /></div>
<div style="text-align: justify;">
<i>If you are interested to contribute, please leave a note.</i></div>
</div>
