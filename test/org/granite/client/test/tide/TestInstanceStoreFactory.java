package org.granite.client.test.tide;

import org.granite.client.tide.Context;
import org.granite.client.tide.InstanceStore;
import org.granite.client.tide.InstanceStoreFactory;

public class TestInstanceStoreFactory implements InstanceStoreFactory {

	@Override
	public InstanceStore createStore(Context context) {
		return new TestInstanceStore(context);
	}
}