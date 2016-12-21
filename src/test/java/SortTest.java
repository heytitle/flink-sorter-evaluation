/**
 * Created by heytitle on 12/21/16.
 */

import junit.framework.TestCase;
import org.apache.commons.collections.BufferOverflowException;
import org.apache.commons.math3.util.MultidimensionalCounter;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.runtime.operators.sort.InMemorySorter;
import org.apache.flink.runtime.operators.sort.QuickSort;
import org.apache.flink.util.MutableObjectIterator;
import org.example.utils.SorterFactory;
import org.example.utils.generator.RandomTuple2LongInt;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Random;

public class SortTest extends TestCase {




	QuickSort qs = new QuickSort();

	@Test
	public  void testSort() throws IOException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
		int[] noRecords = new int[]{ 1000, 1000000 };

		for( int i : noRecords ) {
			int seed = new Random().nextInt();
			InMemorySorter sorter = SorterFactory.getSorter("org.example.MySorter");

			System.out.println("Testing  sorting on " + i + " records and seed " + seed );

			fillRandomData(sorter, seed, i );

			qs.sort(sorter);

			Tuple2<Long,Integer> readTarget = new Tuple2<Long,Integer>();

			MutableObjectIterator<Tuple2<Long,Integer>> iter = sorter.getIterator();
			iter.next(readTarget);

			int num = 0;
			Long prev = null;
			Long current;

			boolean isCorrect = true;
			do {
				current = readTarget.getField(0);

				if( prev == null ){
					prev = current;
					continue;
				}


				final long cmp = prev - current;

				if (cmp > 0) {
					isCorrect = false;
					break;
				}

				prev = current;

				num++;

			} while( (readTarget = iter.next(readTarget)) != null ) ;

			assertTrue("Data is sorted property: seed " + seed + " , no. records " + i, isCorrect);

		}


	}

	public static void fillRandomData(InMemorySorter sorter, int seed, int noRecords) throws IOException {
		RandomTuple2LongInt generator = new RandomTuple2LongInt(seed);

		Tuple2<Long,Integer> record = new Tuple2<Long,Integer>();
		int num = 0;

		while (num < noRecords){
			generator.next(record);
			boolean succeed = sorter.write(record);
			if( !succeed ) {
				throw new BufferOverflowException(" We have enough space for " + num  + "records only.");
			}
			num++;
		}
	}

}