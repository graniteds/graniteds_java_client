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

package org.granite.client.messaging.jmf.persistence;

import java.util.Collection;
import java.util.Map;

import org.granite.client.persistence.collection.PersistentCollectionSnapshotFactory;
import org.granite.messaging.jmf.persistence.JMFPersistentCollectionSnapshot;
import org.granite.messaging.persistence.PersistentCollectionSnapshot;

/**
 * @author Franck WOLFF
 */
public class JMFPersistentCollectionSnapshotFactory extends PersistentCollectionSnapshotFactory {

	@Override
	public PersistentCollectionSnapshot newPersistentCollectionSnapshot(String detachedState) {
		return new JMFPersistentCollectionSnapshot(detachedState);
	}

	@Override
	public PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean sorted, String detachedState) {
		return new JMFPersistentCollectionSnapshot(sorted, detachedState);
	}

	@Override
	public PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Collection<?> collection) {
		return new JMFPersistentCollectionSnapshot(initialized, detachedState, dirty, collection);
	}

	@Override
	public PersistentCollectionSnapshot newPersistentCollectionSnapshot(boolean initialized, String detachedState, boolean dirty, Map<?, ?> collection) {
		return new JMFPersistentCollectionSnapshot(initialized, detachedState, dirty, collection);
	}
}
