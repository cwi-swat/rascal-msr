module experiments::scm::Timer

@doc{Starts a timer}
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java datetime startTimer();

@doc{Stops the last timer}
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int stopTimer();

@doc{Gets the current datetime as an int}
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int currentDate();

@doc{Return the difference between two dates and/or datetimes in hours.} 
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int hoursDiff(datetime start, datetime end);

@doc{Return the difference between two dates and/or datetimes in minutes.} 
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int minutesDiff(datetime start, datetime end);

@doc{Return the difference between two dates and/or datetimes in seconds.} 
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int secondsDiff(datetime start, datetime end);

@doc{Return the difference between two dates and/or datetimes in milliseconds.} 
@javaClass{org.rascalmpl.library.experiments.scm.Timer}
public java int millisDiff(datetime start, datetime end);