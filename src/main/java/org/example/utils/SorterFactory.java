package org.example.utils;

/**
 * Created by heytitle on 11/28/16.
 */

import org.apache.flink.api.common.typeutils.TypeComparator;
import org.apache.flink.api.common.typeutils.TypeSerializer;
import org.apache.flink.api.common.typeutils.base.IntComparator;
import org.apache.flink.api.common.typeutils.base.IntSerializer;
import org.apache.flink.api.common.typeutils.base.LongComparator;
import org.apache.flink.api.common.typeutils.base.LongSerializer;
import org.apache.flink.api.java.tuple.Tuple2;
import org.apache.flink.api.java.typeutils.runtime.TupleComparator;
import org.apache.flink.api.java.typeutils.runtime.TupleSerializer;

import org.apache.flink.core.memory.MemorySegment;
import org.apache.flink.core.memory.MemorySegmentFactory;
import org.apache.flink.runtime.operators.sort.InMemorySorter;
import org.apache.flink.runtime.operators.sort.NormalizedKeySorter;
import org.example.Configuration;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SorterFactory {

	public static InMemorySorter createSorter() {

		TupleSerializer<Tuple2<Integer,Integer>> serializer = new TupleSerializer<Tuple2<Integer, Integer>>(
				(Class<Tuple2<Integer, Integer>>) (Class<?>) Tuple2.class,
				new TypeSerializer[] {
						IntSerializer.INSTANCE,
						IntSerializer.INSTANCE
				}
		);

		TupleComparator<Tuple2<Integer,Integer>> comparator = new TupleComparator<Tuple2<Integer, Integer>>(
				new int[]{0},
				new TypeComparator[]{
						new IntComparator(true)
				},
				new TypeSerializer[] {
						IntSerializer.INSTANCE
				}
		);

		MemorySegment memory = MemorySegmentFactory.allocateUnpooledSegment(Configuration.pageSize);

		NormalizedKeySorter<Tuple2<Integer,Integer>> sorter = new NormalizedKeySorter<Tuple2<Integer,Integer>>(
				serializer,
				comparator,
				getMemory( Configuration.numSegments, Configuration.pageSize )
		);

		return  sorter;
	}

	public static InMemorySorter getSorter( String sorterName ) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {

		TupleSerializer<Tuple2<Long,Integer>> serializer = new TupleSerializer<Tuple2<Long, Integer>>(
			(Class<Tuple2<Long, Integer>>) (Class<?>) Tuple2.class,
			new TypeSerializer[] {LongSerializer.INSTANCE,
				IntSerializer.INSTANCE
			}
		);

		TupleComparator<Tuple2<Long,Integer>> comparator = new TupleComparator<Tuple2<Long, Integer>>(
			new int[]{0},
			new TypeComparator[]{
				new LongComparator(true)
			},
			new TypeSerializer[] {
				IntSerializer.INSTANCE
			}
		);


		MemorySegment memory = MemorySegmentFactory.allocateUnpooledSegment(Configuration.pageSize);

		Class sorterClass = Class.forName(sorterName);

		Class[] types = {
			TypeSerializer.class, TypeComparator.class, List.class
		};

		Constructor constructor = sorterClass.getConstructor(types);

		Object[] parameters = {
			serializer, comparator, getMemory( Configuration.numSegments, Configuration.pageSize )
		};

		InMemorySorter<Tuple2<Long,Integer>> sorter = (InMemorySorter<Tuple2<Long,Integer>>) constructor.newInstance(parameters);

		return  sorter;
	}


	private static List<MemorySegment> getMemory(int numSegments, int segmentSize) {
		ArrayList<MemorySegment> list = new ArrayList<MemorySegment>(Configuration.numSegments);
		for (int i = 0; i < Configuration.numSegments; i++) {
			list.add(MemorySegmentFactory.allocateUnpooledSegment(segmentSize));
		}
		return list;
	}

}