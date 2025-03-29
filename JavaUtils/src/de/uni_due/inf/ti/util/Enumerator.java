/* 
 * Copyright (c) 2013-2014, H.J. Sander Bruggink, 
 * Copyright (c) 2013-2014, University of Duisburg-Essen
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright
 *     notice, this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright
 *     notice, this list of conditions and the following disclaimer in the
 *     documentation and/or other materials provided with the distribution.
 *   - Neither the names of its copyright holder(s) nor the names of its
 *     contributors may be used to endorse or promote products derived from
 *     this software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

// 2014-09-26 - Created
// 2014-10-21 - Extended to implement Spliterator interface

package de.uni_due.inf.ti.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.checkerframework.dataflow.qual.Pure;
import org.checkerframework.dataflow.qual.SideEffectFree;

import de.uni_due.inf.ti.annotation.NonNull;

/**
 * A class which enumerates (non-null) values. 
 * {@code Enumerator}s differ from {@code Iterator}s in that they typically
 * generate the elements on-the-fly.
 * <p>
 * The Enumerator class provides bridges between Java's Collection API and
 * Stream API (new in Java 8):
 * <ul>
 * <li>To obtain an iterator which iterates over the same elements as this
 *     Enumerator, one calls the {@link #iterator()} method.
 * <li>To obtain a list containing the elements enumerated over by this
 *     Enumerator, one calls the {@link #createList()} method.
 * <li>To obtain a stream, use {@link #stream()}. The returned stream is
 *     somewhat lightweight (common operations like filter, map, etc. are
 *     implemented directly, instead of falling back to a standard stream
 *     based on a spliterator).
 * </ul>
 * 
 * @author H.J. Sander Bruggink
 */
public abstract class Enumerator<T> implements Iterable<T>, Spliterator<T>
{
    private @Nullable Iterator<T> myIterator = null;
    
    /**
     * Returns the next element in the enumeration, or {@code null} if there
     * is no next element.
     * 
     * @return
     *      the next element of the enumeration
     */
    public abstract @Nullable T next();
    
    /**
     * Applies an action on the next element, if available.
     * 
     * @param action
     *      action which is applied to the next element of this Enumerator
     * @return 
     *      {@code true} if a next element was available and the action was
     *      applied to it, or {@code false} if a next element was not 
     *      available
     * @throws NullPointerException
     *      if {@code action} is {@code null}
     */
    @Override
    public boolean tryAdvance(Consumer<? super T> action)
    {
        if (action == null) throw new NullPointerException();
        
        @Nullable T ob = next();
        if (ob != null) {
            action.accept(ob);
            return true;
        }
        else return false;
    }
    
    /**
     * Tries to split this Spliterator. This default method returns 
     * {@code null}.
     */
    @Override
    public @Nullable Spliterator<T> trySplit()
    {
        return null;
    }
    
    /**
     * Returns the characteristics of this Enumerator. This default method
     * returns {@code IMMUTABLE | NONNULL}; subclasses with other
     * characteristics should override this method.
     */
    @Override@Pure
    public int characteristics()
    {
        return IMMUTABLE | NONNULL;
    }
    
    /**
     * Returns the estimated number of elements in this Enumerator.
     * This default implementation returns {@link Long#MAX_VALUE}.
     */
    @Override@Pure
    public long estimateSize()
    {
        return Long.MAX_VALUE;
    }
    
    /**
     * Returns an Enumerator which applies a function to every element of
     * this Enumerator. The returned Enumerator is linked to this one:
     * calling the {@link #next()} method on either on will cause both
     * enumerators to proceed to the next element.
     * 
     * @param func
     *      function which is applied to each element
     * @return
     *      Enumerator which applies the function {@code func} to each element
     *      of this Enumerator
     * @throws NullPointerException
     *      if {@code func} is {@code null}
     */
    public final <S> @NonNull Enumerator<S> map(final Function<@NonNull ? super T, ? extends S> func)
    {
        if (func == null) throw new NullPointerException();
        
        return new Enumerator<S>() {
            @Override
            public @Nullable S next()
            {
                @Nullable T ob = Enumerator.this.next();
                if (ob == null) {
                    return null;
                }
                else {
                    return func.apply(ob);
                }
            }
        };
    }

    /**
     * Returns an Enumerator which applies a function to every element of
     * this Enumerator and concatenates the returned Enumerators.
     *
     * @param func
     *      function which is applied to each element
     * @return
     *      Enumerator which enumerates the enumerators that result from
     *      applying the function {@code func} to each element of this
     *      Enumerator
     * @throws NullPointerException
     *      if {@code func} is {@code null}
     */
    public final <S> @NonNull Enumerator<S> flatMap(final Function<@NonNull ? super T, ? extends Enumerator<S>> func)
    {
        if (func == null) throw new NullPointerException();

        return new Enumerator<S>() {
            private Enumerator<S> currentEnumerator = null;
            @Override
            public @Nullable S next()
            {
                while (true) {
                    // current enumerator still has elements?
                    if (currentEnumerator != null) {
                        @Nullable S el = currentEnumerator.next();
                        if (el != null) {
                            return el;
                        }
                    }
                    // no, obtain next one by feeding our own next element to func
                    @Nullable T ob = Enumerator.this.next();
                    if (ob == null) {
                        return null;
                    }
                    else {
                        currentEnumerator = func.apply(ob);
                        // (potentially) fetch actual element in next while-loop iteration
                    }
                }
            }
        };
    }

    /**
     * Returns a Enumerator which enumerates all elements of this Enumerator
     * that satisfy a given condition. The returned Enumerator is linked to
     * this Enumerator: if {@link #next()} is called on the returned 
     * Enumerator, {@code next()} is also called on or more times on this
     * Enumerator and vice versa.
     * 
     * @param pred
     *      the predicate which tells which elements of this Enumerator
     *      are to be contained in the new Enumerator
     * @return
     *      Enumerator which returns all elements of this Enumerator for
     *      which the given predicate holds
     * @throws NullPointerException
     *      if {@code pred} is {@code null}
     */
    public final @NonNull Enumerator<T> filter(final Predicate<? super T> pred)
    {
        if (pred == null) throw new NullPointerException();
        
        return new Enumerator<T>() {
            @Override
            public @Nullable T next()
            {
                @Nullable T ob = Enumerator.this.next();
                while (ob != null && !pred.test(ob)) {
                    ob = Enumerator.this.next();
                }
                return ob;
            }
        };
    }

    /**
     * Returns an Iterator which returns the same elements in the same order
     * as this Enumeration.
     * <p>
     * It results in undefined behavior if both the returned Iterator and this
     * Enumerator's {@link #next()} methods are used. If this method is
     * called twice, the same Iterator object is returned.
     */
    @Override@Pure
    public final Iterator<T> iterator()
    {
        if (myIterator == null) myIterator = new EnumeratorIterator();
        return myIterator;
    }

    /**
     * Returns a Stream which returns the same elements in the same order
     * as this Enumeration.
     * <p>
     * The returned Stream is created from the iterator, by
     * {@code StreamSupport.stream(this, false)}. As a result, the same
     * restrictions as for {@link #iterator()} regarding re-using the
     * Enumerator apply.
     */
    @Pure
    public final Stream<T> stream()
    {
        return new EnumeratorStream<>(this);
    }

    /**
     * Returns a list which contains the rest of elements enumerated by this
     * Enumerator.
     * This method keeps calling {@link #next()} until it returns {@code null},
     * and collects all returned objects in a list. Finally, this list is
     * returned.
     * <p>
     * After calling this method, subsequent calls to {@link #next()} will
     * return {@code null}.
     * <p>
     * <em>Warning.</em> If this iterator produces an infinite stream of 
     * objects, this method will not return (or rather, it will cause an 
     * {@link OutOfMemoryError}).
     * 
     * @return
     *      a list containing the rest of the elements iterator by this 
     *      Enumerator
     */
    @SideEffectFree
    public final @NonNull List<T> createList()
    {
        ArrayList<T> result = new ArrayList<>();
        
        @Nullable T el = next();
        while (el != null) {
            result.add(el);
            el = next();
        }
        
        result.trimToSize();
        return result;
    }
    
    /**
     * Bridge class between Enumerator and Iterator.
     */
    private class EnumeratorIterator implements Iterator<T>
    {
        private @Nullable T nextObject = null;

        @Override@Pure
        public boolean hasNext()
        {
            if (nextObject == null) {
                nextObject = Enumerator.this.next();
            }
            return (nextObject != null);
        }

        @Override
        public T next()
        {
            if (nextObject != null) {
                T ob = nextObject;
                nextObject = null;
                return ob;
            }
            else {
                @Nullable T ob = Enumerator.this.next();
                if (ob == null) throw new NoSuchElementException();
                return ob;
            }
        }

        @Override
        public void remove()
        {
            throw new UnsupportedOperationException();
        }
    }

    /**
     * A {@link Stream} implementation that returns the elements of an {@link Enumerator}.
     * <p>
     * While it is possible to generate a {@link Stream} from the {@link Iterator} that is
     * already provided by {@link Enumerator}, the resulting stream is quite complex,
     * perhaps too complex for a simple "give me a bunch of elements and filter out a few
     * ones" task. This class implements common operations directly, while defaulting to
     * a classic {@link Stream} for all others.
     * @param <T> type of the elements
     */
	private static class EnumeratorStream<T> implements Stream<T> {
		Enumerator<T> enumerator;

		public EnumeratorStream(Enumerator<T> enumerator) {
			this.enumerator = enumerator;
		}

		private Stream<T> fullStream() {
			return StreamSupport.stream(enumerator, false);
		}

		@Override
		public Iterator<T> iterator() {
			return enumerator.iterator();
		}

		@Override
		public Spliterator<T> spliterator() {
			return enumerator.spliterator();
		}

		@Override
		public boolean isParallel() {
			return false;
		}

		@Override
		public Stream<T> sequential() {
			return this;
		}

		@Override
		public Stream<T> parallel() {
			return fullStream().parallel();
		}

		@Override
		public Stream<T> unordered() {
			return fullStream().unordered();
		}

		@Override
		public Stream<T> onClose(Runnable closeHandler) {
			return fullStream().onClose(closeHandler);
		}

		@Override
		public void close() {
			// nothing to do (this Stream does not have close handlers and if any were added, that's a new Stream)
		}

		@Override
		public Stream<T> filter(Predicate<? super T> predicate) {
			return enumerator.filter(predicate).stream();
		}

		@Override
		public <R> Stream<R> map(Function<? super T, ? extends R> mapper) {
			@SuppressWarnings("null")
			Enumerator<R> mapped = enumerator.map(mapper);
			return mapped.stream();
		}

		@Override
		public IntStream mapToInt(ToIntFunction<? super T> mapper) {
			return fullStream().mapToInt(mapper);
		}

		@Override
		public LongStream mapToLong(ToLongFunction<? super T> mapper) {
			return fullStream().mapToLong(mapper);
		}

		@Override
		public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
			return fullStream().mapToDouble(mapper);
		}

		@Override
		public <R> Stream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
			if (mapper == null)
				throw new NullPointerException();

			return new Enumerator<R>() {
				private Enumerator<? extends R> currentEnumerator = null;

				@Override
				public @Nullable R next() {
					while (true) {
						// current enumerator still has elements?
						if (currentEnumerator != null) {
							@Nullable R el = currentEnumerator.next();
							if (el != null) {
								return el;
							}
						}
						// no, obtain next one by feeding our own next element to func
						@Nullable T ob = enumerator.next();
						if (ob == null) {
							return null;
						} else {
							@SuppressWarnings("resource")
							Stream<? extends R> o = mapper.apply(ob);
							if (o == null) {
								currentEnumerator = null;
							} else if (o instanceof EnumeratorStream) {
								// is an Enumerator, use it directly
								currentEnumerator = ((EnumeratorStream<? extends R>) o).enumerator;
							} else {
								// some other type of stream, access it via its iterator
								currentEnumerator = Enumerators.enumerate(o.iterator());
							}
							// (potentially) fetch actual element in next while-loop iteration
						}
					}
				}
			}.stream();

		}

		@Override
		public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
			return fullStream().flatMapToInt(mapper);
		}

		@Override
		public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
			return fullStream().flatMapToLong(mapper);
		}

		@Override
		public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
			return fullStream().flatMapToDouble(mapper);
		}

		@Override
		public Stream<T> distinct() {
			return fullStream().distinct();
		}

		@Override
		public Stream<T> sorted() {
			return fullStream().sorted();
		}

		@Override
		public Stream<T> sorted(Comparator<? super T> comparator) {
			return fullStream().sorted(comparator);
		}

		@Override
		public Stream<T> peek(Consumer<? super T> action) {
			return fullStream().peek(action);
		}

		@Override
		public Stream<T> limit(long maxSize) {
			return fullStream().limit(maxSize);
		}

		@Override
		public Stream<T> skip(long n) {
			for (long i = 0; i < n; i++) {
				enumerator.next();
			}
			return this;
		}

		@Override
		public void forEach(Consumer<? super T> action) {
			while (true) {
				@Nullable T el = enumerator.next();
				if (el == null) return;
				action.accept(el);
			}
		}

		@Override
		public void forEachOrdered(Consumer<? super T> action) {
			forEach(action);
		}

		@Override
		public Object[] toArray() {
			return enumerator.createList().toArray();
		}

		@Override
		public <A> A[] toArray(IntFunction<A[]> generator) {
			return enumerator.createList().toArray(generator);
		}

		@Override
		public T reduce(T identity, BinaryOperator<T> accumulator) {
			return fullStream().reduce(identity, accumulator);
		}

		@Override
		public Optional<T> reduce(BinaryOperator<T> accumulator) {
			return fullStream().reduce(accumulator);
		}

		@Override
		public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
			return fullStream().reduce(identity, accumulator, combiner);
		}

		@Override
		public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
			return fullStream().collect(supplier, accumulator, combiner);
		}

		@Override
		public <R, A> R collect(Collector<? super T, A, R> collector) {
			return fullStream().collect(collector);
		}

		@Override
		public Optional<T> min(Comparator<? super T> comparator) {
			return fullStream().min(comparator);
		}

		@Override
		public Optional<T> max(Comparator<? super T> comparator) {
			return fullStream().max(comparator);
		}

		@Override
		public long count() {
			long n = 0;
			while (enumerator.next() != null) {
				n++;
			}
			return n;
		}

		@Override
		public boolean anyMatch(Predicate<? super T> predicate) {
			while (true) {
				@Nullable T el = enumerator.next();
				if (el == null) {
					// no (more) elements found for which predicate could evaluate to true
					return false;
				}
				if (predicate.test(el)) {
					return true;
				}
			}
		}

		@Override
		public boolean allMatch(Predicate<? super T> predicate) {
			while (true) {
				@Nullable T el = enumerator.next();
				if (el == null) {
					// no (more) elements found which invalidate the predicate
					return true;
				}
				if (!predicate.test(el)) {
					return false;
				}
			}
		}

		@Override
		public boolean noneMatch(Predicate<? super T> predicate) {
			// allMatch(predicate.negate())
			while (true) {
				@Nullable T el = enumerator.next();
				if (el == null) {
					// no (more) elements found which could satisfy the predicate
					return true;
				}
				if (predicate.test(el)) {
					return false;
				}
			}
		}

		@SuppressWarnings("null")
		@Override
		public Optional<T> findFirst() {
			return Optional.ofNullable(enumerator.next());
		}

		@Override
		public Optional<T> findAny() {
			return findFirst();
		}
	}
}
