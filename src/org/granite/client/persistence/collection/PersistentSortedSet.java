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
import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.granite.messaging.persistence.PersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshotFactory;

/**
 * @author Franck WOLFF
 */
public class PersistentSortedSet<E> extends AbstractPersistentSimpleCollection<E, SortedSet<E>> implements SortedSet<E>, PersistentSortedCollection<E> {

	public PersistentSortedSet() {
	}

	public PersistentSortedSet(SortedSet<E> collection) {		
		this(collection, true);
	}

	public PersistentSortedSet(SortedSet<E> collection, boolean clone) {		
		if (collection != null)
			init(clone ? new TreeSet<E>(collection) : collection, false);
	}

	public Comparator<? super E> comparator() {
		return getCollection().comparator();
	}

	public SortedSet<E> subSet(E fromElement, E toElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().subSet(fromElement, toElement));
	}

	public SortedSet<E> headSet(E toElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().headSet(toElement));
	}

	public SortedSet<E> tailSet(E fromElement) {
		if (!checkInitializedRead())
			return null;
		return new SortedSetProxy<E>(getCollection().tailSet(fromElement));
	}

	public E first() {
		if (!checkInitializedRead())
			return null;
		return getCollection().first();
	}

	public E last() {
		if (!checkInitializedRead())
			return null;
		return getCollection().last();
	}

	@Override
	protected PersistentCollectionSnapshot createSnapshot(boolean forReading) {
		PersistentCollectionSnapshotFactory factory = PersistentCollectionSnapshotFactory.newInstance();
		if (forReading || !wasInitialized())
			return factory.newPersistentCollectionSnapshot(true);
		return factory.newPersistentCollectionSnapshot(true, isDirty(), getCollection());
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void updateFromSnapshot(ObjectInput in, PersistentCollectionSnapshot snapshot) {
		if (snapshot.isInitialized()) {
			Comparator<? super E> comparator = null;
			try {
				comparator = snapshot.newComparator(in);
			}
			catch (Exception e) {
				throw new RuntimeException("Could not create instance of comparator", e);
			}
			SortedSet<E> set = new TreeSet<E>(comparator);
			set.addAll((Collection<? extends E>)snapshot.getElementsAsCollection());
			init(set, snapshot.isDirty());
		}
		else
			init(null, false);
	}
	
    public PersistentSortedSet<E> clone(boolean uninitialize) {
    	PersistentSortedSet<E> set = new PersistentSortedSet<E>();
    	if (wasInitialized() && !uninitialize)
    		set.init(getCollection(), isDirty());
        return set; 
    }
}
