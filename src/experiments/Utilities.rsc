module experiments::Utilities

import Set;
import List;
import IO;
import resource::versions::Timer;

public list[int] calcAvg(set[list[int]] input) {
    map[int column, int total] totals = ();
    map[int column, int amount] amounts = ();
    
    for(list[int] rslt <- input) {
	int c = 0;
	
	for(v <- rslt) {
	    if (c in totals) {
		totals[c] += v;
		amounts[c] += 1;
	    } else {
		totals[c] = v;
		amounts[c] = 1;
	    }
	    c += 1;
	}
    }
    
    return [ totals[c]/amounts[c] | c <- quickSort(totals.column)];
}

//Timer functions
public datetime printStartTimer(str msg) {
    sTime = startTimer();
    print("started at <sTime> \t[<msg>]");
    return sTime;
}
public int printStopTimer(str msg) {
    dur = stopTimer();
    print("duration <dur> ms \t[<msg>]");
    return dur;
} 
public int printRestartTimer(str msg) {
    dur = stopTimer();
    print("duration <dur> ms \t[<msg>], restarted <startTimer()>");
    return dur;
}

//Utility functions
public list[&T] quickSort(set[&T] st) {
    return quickSort(toList(st));
}
public list[&T] quickSort(list[&T] lst) {
    if(size(lst) <= 1){
	return lst;
    }
  
    list[&T] less = [];
    list[&T] greater = [];
    &T pivot = lst[0];

    <pivot, lst> = takeOneFrom(lst);

    for(&T elm <- lst){
	if(elm <= pivot){
	    less = [elm] + less;
	} else {
	    greater = [elm] + greater;
	}
    }

    return quickSort(less) + pivot + quickSort(greater);
}
