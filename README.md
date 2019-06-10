# ScoreTracker King

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