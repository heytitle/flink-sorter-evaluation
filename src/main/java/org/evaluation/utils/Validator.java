package org.evaluation.utils;

import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.util.MutableObjectIterator;

import java.io.IOException;

/**
 * Created by heytitle on 12/21/16.
 */
public class Validator {

	public static boolean isSorted(MutableObjectIterator<Tuple2<Integer,Integer>> iter) throws IOException {

		Tuple2<Integer,Integer> readTarget = new Tuple2<Integer,Integer>();

		iter.next(readTarget);

		int num = 0;
		Integer prev = null;
		Integer current;

		boolean isSorted = true;

		do {
			current = readTarget.getField(0);

			if( prev == null ){
				prev = current;
				continue;
			}


			final long cmp = prev.compareTo(current);

			if ( cmp > 0 ) {
				isSorted = false;
				break;
			}

			prev = current;

			num++;

		} while( (readTarget = iter.next(readTarget)) != null ) ;

		return isSorted;
	}
}
