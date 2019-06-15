# ScoreTracker

This project is a part of the assignment which I created. I covered the design choices I made in a blog post which can be found at the below URL.

http://www.kartikiyer.com/2019/06/16/synchronizing-multi-threaded-code-based-on-object-value/

The main class is RequestInterceptor. It creates a http webserver which caters to the http requests at port 8081.
The program can be given path to a property file <arg[0]> while launching it, but it will revert to default values if property files is not provided 

All the classes have javadocs attached which explain in detail their usages and the design decisions taken.

The couple of design decisions I wanted to highlight include.


1. Synchronization is done on levelId & levelId+userId
Since we are modifying the same collection, I took a "data parallel" approach, 
where I allow updates happening on different levels to happen concurrently without blocking them.
The class Mutex is responsible for giving locks to threads based on their levelId.
The threads use to ensure a lock on the core DS belonging to the same level.
Requests of the same level will have to pass through a sync block which ensures data integrity as well as performance,
by allowing parallel updates as long as they belong to different levels.


2. TreeSet of userscore is used where the score is a double.
Treeset with key on userscore was used to leverage the sort capability of this DS by using the compareTo in UserScore.class
The problem is treemap wont allow us to save same score twice as score is my key, & duplicate keys are not allowed.
My solution was to increment the score by a decimal of 0.01 to save it and later round is when retrieving the highscores 
 
 
3. Each login of the user will be given a new session
I thought about reusing the session, but then ultimately decided to skip it because,
to keep track of sessions, I'd need a second map to store Map<userid,session> and then check that before returning it.
And since the sessionId is converted to userId when storing score related info, user scores should not be impacted as they dont rely on session.
Also, most of the games use a unique sessionId as sessionIds can be used to track other metrics like ping/latency etc, 
which would not be possible for analytics if the same session is reused till it expires


4. EndToEndHttpTest 
This class present in the root of the tests package is responsible for testing the end to end fuctionality of the project.
It simulates all the operations which would happen normally over the project.  


5 Functional requirements
The functions are described in detail below and the notation <value> means a call parameter value or a return value. All calls shall result in the HTTP status code 200, unless when something goes wrong, where anything but 200 must be returned. Numbers parameters and return values are sent in decimal ASCII representation as expected (ie no binary format).
Users and levels are created “ad-hoc”, the first time they are referenced.

5.1 Login
This function returns a session key in the form of a string (without spaces or “strange” characters) which shall be valid for use with the other functions for 10 minutes. The session keys should be “reasonably unique”.
Request: GET /<userid>/login
Response: <sessionkey>
<userid> : 31 bit unsigned integer number
<sessionkey> : A string representing session (valid for 10 minutes).
Example: http://localhost:8081/4711/login --> UICSNDK

5.2 Post a user's score to a level
This method can be called several times per user and level and does not return anything. Only requests with valid session keys shall be processed.
Request: POST /<levelid>/score?sessionkey=<sessionkey>
Request body: <score>
Response: (nothing)
<levelid> : 31 bit unsigned integer number
<sessionkey> : A session key string retrieved from the login function.
<score> : 31 bit unsigned integer number
Example: POST http://localhost:8081/2/score?sessionkey=UICSNDK (with the post body: 1500)

5.3 Get a high score list for a level
Retrieves the high scores for a specific level. The result is a comma separated list in descending score order. Because of memory reasons no more than 15 scores are to be returned for each level. Only the highest score counts. ie: an user id can only appear at most once in the list. If a user hasn't submitted a score for the level, no score is present for that user. A request for a high score list of a level without any scores submitted shall be an empty string.
Request: GET /<levelid>/highscorelist
Response: CSV of <userid>=<score>
<levelid> : 31 bit unsigned integer number
<score> : 31 bit unsigned integer number
<userid> : 31 bit unsigned integer number
Example: http://localhost:8081/2/highscorelist - > 4711=1500,131=1220
