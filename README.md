Westview App

The main goal of this project is to provide a single app to view grades, schedules, and school 
information with real-time updates as to what the current period is and the bell schedule of the 
day. In other words, the app's purpose is to add to Saturn, StudentVUE, and the current Westview HS 
app.

Grades and class schedules are pulled on a per-student basis using an API written by Synergy. This 
data is then parsed and used within the app to display grades and schedules to the student. Data 
cannot be accessed by anyone other than the student, and the student must provide the login 
information. All Synergy information is cleared upon logout or closing the app.

Calendars relating to the school are pulled using Google's calendar APIs, then parsed into a more 
readable format. Like student information, this data is not stored and is cleared upon logout.

Newsletters are displayed by embedding the web page into the app. Link clicks are sent to the 
phone's default browser. Counseling calendars can be accessed by clicking on the calendar from the 
counseling home page.

Grades, attendance, and information from Synergy cannot be accessed without having the student's 
username and password. The username and password are communicated to Synergy in the same manner as 
accessing StudentVUE in a web browser. All information related to the student is never saved to the 
phone's file system. The "Stay logged in" functionality is provided by Apple and Android's 
implementation of secure, encrypted storage. Upon selecting "log out" or fully closing the app, all 
data is cleared from memory and theoretically cannot be accessed again.