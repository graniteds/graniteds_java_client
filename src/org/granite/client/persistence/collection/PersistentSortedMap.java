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

import java.io.ObjectInput;
import java.util.Comparator;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshotFactory;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedMap<K, V> extends AbstractPersistentMapCollection<K, V, SortedMap<K, V>> implements SortedMap<K, V>, PersistentSortedCollection<K> {

	public PersistentSortedMap() {
	}

	public PersistentSortedMap(boolean initialized) {
		this(initialized ? new TreeMap<K, V>() : null, false);
	}

	public PersistentSortedMap(SortedMap<K, V> collection) {		
		this(collection, true);
	}

	public PersistentSortedMap(SortedMap<K, V> collection, boolean clone) {	
		if (collection != null)
			init(clone ? new TreeMap<K, V>(collection) : collection, false);
	}

	public Comparator<? super K> comparator() {
		return getCollection().comparator();
	}

	public SortedMap<K, V> subMap(K fromKey, K toKey) {
		if (!checkInitializedRead())
			return null;
		return new SortedMapProxy<K, V>(getCollection().subMap(fromKey, toKey));
	}

	public SortedMap<K, V> headMap(K toKey) {
		if (!checkInitializedRead())
			return null;
		return new SortedMapProxy<K, V>(getCollection().headMap(toKey));
	}

	public SortedMap<K, V> tailMap(K fromKey) {
		if (!checkInitializedRead())
			return null;
		return new SortedMapProxy<K, V>(getCollection().tailMap(fromKey));
	}

	public K firstKey() {
		if (!checkInitializedRead())
			return null;
		return getCollection().firstKey();
	}

	public K lastKey() {
		checkInitializedRead();
		return getCollection().lastKey();
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance();
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot(true);
		return factory.newPersistentCollectionSnapshot(true, isDirty(), this);
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized()) {
			Comparator<? super K> comparator = null;
			try {
				comparator = snapshot.newComparator(in);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance of comparator", e);
			}
			SortedMap<K, V> map = new TreeMap<K, V>(comparator);
			map.putAll((Map<K, V>)snapshot.getElementsAsMap());
			init(map, snapshot.isDirty());
		}
		else
			init(null, false);
	}
	
    public PersistentSortedMap<K, V> clone(boolean uninitialize) {
    	PersistentSortedMap<K, V> map = new PersistentSortedMap<K, V>();
    	if (wasInitialized() && !uninitialize)
    		map.init(getCollection(), isDirty());
        return map;
    }
}
