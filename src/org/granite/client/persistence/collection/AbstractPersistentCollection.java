/*
  GRANITE DATA SERVICES
  Copyright (C) 2013 GRANITE DATA SERVICES S.A.S.

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Library General Public License as published by
  the Free Software Foundation; either version 2 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Library General Public License
  for more details.

  You should have received a copy of the GNU Library General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
*/

package org.granite.client.persistence.collection;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import org.granite.client.persistence.LazyInitializationException;
import org.granite.client.persistence.Loader;
import org.granite.logging.Logger;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.util.TypeUtil;


/**
 * @author Franck WOLFF
 */
public abstract class AbstractPersistentCollection<C> implements PersistentCollection {
	
	private static final Logger log = Logger.getLogger(AbstractPersistentCollection.class);

	private volatile C collection = null;
	private volatile boolean dirty = false;
	private Loader<PersistentCollection> loader = new DefaultCollectionLoader();
    private List<InitializationListener> listeners = new ArrayList<InitializationListener>();
	
	protected AbstractPersistentCollection() {
	}
	
	protected void init(C collection, boolean dirty) {
		this.collection = collection;
		this.dirty = dirty;
	}
	
	public Loader<PersistentCollection> getLoader() {
		return this.loader;
	}
	public void setLoader(Loader<PersistentCollection> loader) {
		this.loader = loader;
	}
	
	protected boolean checkInitializedRead() {
		if (wasInitialized())
			return true;
		loader.load(this, null);
		return false;
	}
	
	protected void checkInitializedWrite() {
		if (!wasInitialized())
			throw new LazyInitializationException(getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(this)));
	}
	
	protected C getCollection() {
		return collection;
	}
	
	protected ClassLoader getClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public boolean wasInitialized() {
		return collection != null;
	}

	public boolean isDirty() {
		return dirty;
	}

	public void dirty() {
		dirty = true;
	}

	public void clearDirty() {
		dirty = false;
	}
	
	
	public PersistentCollection clone(boolean uninitialize) {
		try {
		    @SuppressWarnings("unchecked")
			AbstractPersistentCollection<C> collection = TypeUtil.newInstance(getClass(), AbstractPersistentCollection.class);
	    	if (wasInitialized() && !uninitialize)
	    		collection.init(getCollection(), isDirty());
	        return collection;
		}
		catch (Exception e) {
			throw new RuntimeException("Could not clone collection " + this.getClass().getName(), e);
		}
    }

	
	protected abstract PersistentCollectionSnapshot createSnapshot(boolean forReading);
	protected abstract void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot);

	public void writeExternal(ObjectOutput out) throws IOException {
		PersistentCollectionSnapshot snapshot = createSnapshot(false);
		snapshot.writeExternal(out);
	}

	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		PersistentCollectionSnapshot snapshot = createSnapshot(true);
		snapshot.readExternal(in);
		updateFromSnapshot(in, snapshot);
	}
	
	@Override
	public String toString() {
		return getClass().getName() + " {initialized=" + wasInitialized() + ", dirty=" + isDirty() + "}" +
			(collection != null ? ": " + collection.toString() : "");
	}

	class IteratorProxy<E> implements Iterator<E> {
		
		private final Iterator<E> iterator;
		
		public IteratorProxy(Iterator<E> iterator) {
			this.iterator = iterator;
		}

		public boolean hasNext() {
			return iterator.hasNext();
		}

		public E next() {
			if (!checkInitializedRead())
				return null;
			return iterator.next();
		}
		
		public void remove() {
			checkInitializedWrite();
			iterator.remove();
			dirty();
		}
		
		@Override
		public int hashCode() {
			return iterator.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return iterator.equals(obj);
		}
	}
	
	class ListIteratorProxy<E> implements ListIterator<E> {
		
		private final ListIterator<E> iterator;

		private E lastNextOrPrevious = null;
		
		public ListIteratorProxy(ListIterator<E> iterator) {
			this.iterator = iterator;
		}
		
		public boolean hasNext() {
			return iterator.hasNext();
		}

		public E next() {
			if (!checkInitializedRead())
				return null;
			return (lastNextOrPrevious = iterator.next());
		}

		public boolean hasPrevious() {
			return iterator.hasPrevious();
		}

		public E previous() {
			if (!checkInitializedRead())
				return null;
			return (lastNextOrPrevious = iterator.previous());
		}

		public int nextIndex() {
			return iterator.nextIndex();
		}

		public int previousIndex() {
			return iterator.previousIndex();
		}

		public void remove() {
			checkInitializedWrite();
			iterator.remove();
			lastNextOrPrevious = null;
			dirty();
		}

		public void set(E e) {
			checkInitializedWrite();
			iterator.set(e);
			if (e == null ? lastNextOrPrevious != null : !e.equals(lastNextOrPrevious))
				dirty();
		}

		public void add(E e) {
			checkInitializedWrite();
			iterator.add(e);
			lastNextOrPrevious = null;
			dirty();
		}

		@Override
		public int hashCode() {
			return iterator.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return iterator.equals(obj);
		}
	}
	
	class CollectionProxy<E> implements Collection<E> {
		
		protected final Collection<E> collection;

		public CollectionProxy(Collection<E> collection) {
			this.collection = collection;
		}

		public int size() {
			return collection.size();
		}

		public boolean isEmpty() {
			return collection.isEmpty();
		}

		public boolean contains(Object o) {
			return collection.contains(o);
		}

		public Iterator<E> iterator() {
			return new IteratorProxy<E>(collection.iterator());
		}

		public Object[] toArray() {
			return collection.toArray();
		}

		public <T> T[] toArray(T[] a) {
			return collection.toArray(a);
		}

		public boolean add(E e) {
			if (collection.add(e)) {
				dirty();
				return true;
			}
			return false;
		}

		public boolean remove(Object o) {
			if (collection.remove(o)) {
				dirty();
				return true;
			}
			return false;
		}

		public boolean containsAll(Collection<?> c) {
			return collection.containsAll(c);
		}

		public boolean addAll(Collection<? extends E> c) {
			if (collection.addAll(c)) {
				dirty();
				return true;
			}
			return false;
		}

		public boolean removeAll(Collection<?> c) {
			if (collection.removeAll(c)) {
				dirty();
				return true;
			}
			return false;
		}

		public boolean retainAll(Collection<?> c) {
			if (collection.retainAll(c)) {
				dirty();
				return true;
			}
			return false;
		}

		public void clear() {
			if (!collection.isEmpty()) {
				collection.clear();
				dirty();
			}
		}

		@Override
		public int hashCode() {
			return collection.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return collection.equals(obj);
		}
	}
	
	class SetProxy<E> extends CollectionProxy<E> implements Set<E> {

		public SetProxy(Set<E> collection) {
			super(collection);
		}
	}
	
	class ListProxy<E> extends CollectionProxy<E> implements List<E> {

		public ListProxy(List<E> collection) {
			super(collection);
		}

		public boolean addAll(int index, Collection<? extends E> c) {
			if (((List<E>)collection).addAll(index, c)) {
				dirty();
				return true;
			}
			return false;
		}

		public E get(int index) {
			return ((List<E>)collection).get(index);
		}
		
		public E set(int index, E element) {
			E previousElement = ((List<E>)collection).set(index, element);
			if (previousElement == null ? element != null : !previousElement.equals(element))
				dirty();
			return previousElement;
		}

		public void add(int index, E element) {
			((List<E>)collection).add(index, element);
			dirty();
		}

		public E remove(int index) {
			E removedElement = ((List<E>)collection).remove(index);
			dirty();
			return removedElement;
		}

		public int indexOf(Object o) {
			return ((List<E>)collection).indexOf(o);
		}

		public int lastIndexOf(Object o) {
			return ((List<E>)collection).lastIndexOf(o);
		}

		public ListIterator<E> listIterator() {
			return listIterator(0);
		}

		public ListIterator<E> listIterator(int index) {
			return new ListIteratorProxy<E>(((List<E>)collection).listIterator(index));
		}

		public List<E> subList(int fromIndex, int toIndex) {
			return new ListProxy<E>(((List<E>)collection).subList(fromIndex, toIndex));
		}
	}
	
	class SortedSetProxy<E> extends SetProxy<E> implements SortedSet<E> {

		public SortedSetProxy(SortedSet<E> collection) {
			super(collection);
		}

		public Comparator<? super E> comparator() {
			return ((SortedSet<E>)collection).comparator();
		}

		public SortedSet<E> subSet(E fromElement, E toElement) {
			return new SortedSetProxy<E>(((SortedSet<E>)collection).subSet(fromElement, toElement));
		}

		public SortedSet<E> headSet(E toElement) {
			return new SortedSetProxy<E>(((SortedSet<E>)collection).headSet(toElement));
		}

		public SortedSet<E> tailSet(E fromElement) {
			return new SortedSetProxy<E>(((SortedSet<E>)collection).tailSet(fromElement));
		}

		public E first() {
			return ((SortedSet<E>)collection).first();
		}

		public E last() {
			return ((SortedSet<E>)collection).last();
		}
	}
	
	class SortedMapProxy<K, V> implements SortedMap<K, V> {
		
		protected final SortedMap<K, V> sortedMap;
		
		public SortedMapProxy(SortedMap<K, V> sortedMap) {
			this.sortedMap = sortedMap;
		}

		public int size() {
			return sortedMap.size();
		}

		public boolean isEmpty() {
			return sortedMap.isEmpty();
		}

		public boolean containsKey(Object key) {
			return sortedMap.containsKey(key);
		}

		public boolean containsValue(Object value) {
			return sortedMap.containsValue(value);
		}

		public V get(Object key) {
			return sortedMap.get(key);
		}

		public V put(K key, V value) {
			boolean containsKey = sortedMap.containsKey(key);
			V previousValue = sortedMap.put(key, value);
			if (!containsKey || (previousValue == null ? value != null : !previousValue.equals(value)))
				dirty();
			return previousValue;
		}

		public V remove(Object key) {
			boolean containsKey = sortedMap.containsKey(key);
			V removedValue = sortedMap.remove(key);
			if (containsKey)
				dirty();
			return removedValue;
		}

		public void putAll(Map<? extends K, ? extends V> m) {
			for (Map.Entry<? extends K, ? extends V> entry : m.entrySet())
				put(entry.getKey(), entry.getValue());
		}

		public void clear() {
			if (!sortedMap.isEmpty()) {
				sortedMap.clear();
				dirty();
			}
		}

		public Comparator<? super K> comparator() {
			return sortedMap.comparator();
		}

		public SortedMap<K, V> subMap(K fromKey, K toKey) {
			return new SortedMapProxy<K, V>(sortedMap.subMap(fromKey, toKey));
		}

		public SortedMap<K, V> headMap(K toKey) {
			return new SortedMapProxy<K, V>(sortedMap.headMap(toKey));
		}

		public SortedMap<K, V> tailMap(K fromKey) {
			return new SortedMapProxy<K, V>(sortedMap.tailMap(fromKey));
		}

		public K firstKey() {
			return sortedMap.firstKey();
		}

		public K lastKey() {
			return sortedMap.lastKey();
		}

		public Set<K> keySet() {
			return new SetProxy<K>(sortedMap.keySet());
		}

		public Collection<V> values() {
			return new CollectionProxy<V>(sortedMap.values());
		}

		public Set<Entry<K, V>> entrySet() {
			return new SetProxy<Entry<K, V>>(sortedMap.entrySet());
		}

		@Override
		public int hashCode() {
			return sortedMap.hashCode();
		}

		@Override
		public boolean equals(Object obj) {
			return sortedMap.equals(obj);
		}
	}
	
	
	private static class DefaultCollectionLoader implements Loader<PersistentCollection> {
		
		public void load(PersistentCollection collection, InitializationCallback callback) {
			throw new LazyInitializationException(callback.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(callback)));
		}
		
		public void onInitializing() {
		}
		
		public void onInitialize() {
		}
		
		public void onUninitialize() {			
		}
	}
    
    public void addListener(InitializationListener listener) {
        if (!listeners.contains(listener))
            listeners.add(listener);
    }
    
    public void removeListener(InitializationListener listener) {
        listeners.remove(listener);
    }
    
    public void initializing() {
    	loader.onInitializing();
    }
    
    public void initialize() {
    	loader.onInitialize();
        
        for (InitializationListener listener : listeners)
            listener.initialized(this);
        
        log.debug("initialized");
    }
    
    public void uninitialize() {
        loader.onUninitialize();
        
        for (InitializationListener listener : listeners)
            listener.uninitialized(this);
        
        log.debug("uninitialized");
    }
    
    public void withInitialized(InitializationCallback callback) {
        if (wasInitialized())
            callback.call(this);
        else
        	loader.load(this, callback);
    }
}
