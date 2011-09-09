module experiments::scm::Timer

@doc{Starts a timer}
@javaClass{experiments.scm.Timer}
public java datetime startTimer();

@doc{Stops the last timer}
@javaClass{experiments.scm.Timer}
public java int stopTimer();

@doc{Gets the current datetime as an int}
@javaClass{experiments.scm.Timer}
public java int currentDate();

@doc{Return the difference between two dates and/or datetimes in hours.} 
@javaClass{experiments.scm.Timer}
public java int hoursDiff(datetime startMeUp, datetime end);

@doc{Return the difference between two dates and/or datetimes in minutes.} 
@javaClass{experiments.scm.Timer}
public java int minutesDiff(datetime startMeUp, datetime end);

@doc{Return the difference between two dates and/or datetimes in seconds.} 
@javaClass{experiments.scm.Timer}
public java int secondsDiff(datetime startMeUp, datetime end);

@doc{Return the difference between two dates and/or datetimes in milliseconds.} 
@javaClass{experiments.scm.Timer}
public java int millisDiff(datetime startMeUp, datetime end);