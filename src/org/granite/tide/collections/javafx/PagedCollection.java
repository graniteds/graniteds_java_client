package org.granite.tide.collections.javafx;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javafx.beans.InvalidationListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import org.granite.tide.collections.AbstractPagedCollection;
import org.granite.util.javafx.ListListenerHelper;


public abstract class PagedCollection<E> extends AbstractPagedCollection<E> implements ObservableList<E> {
	
    private List<E> internalWrappedList = new ArrayList<E>();
    protected ObservableList<E> wrappedList;
    
    
    @Override
    protected List<E> getInternalWrappedList() {
    	return internalWrappedList;
    }
    
    @Override
    protected List<E> getWrappedList() {
    	return wrappedList;
    }
	

	public PagedCollection() {
		super();
		
		wrappedList = FXCollections.observableList(internalWrappedList);
		wrappedList.addListener(new WrappedListListChangeListener());
	}
	
	
	@Override
	public boolean setAll(Collection<? extends E> coll) {
		return fullRefresh();
	}
	
	
	private ListListenerHelper<E> helper = new ListListenerHelper<E>();
	
	public void addListener(ListChangeListener<? super E> listener) {
		helper.addListener(listener);
    }

	public void removeListener(ListChangeListener<? super E> listener) {
		helper.removeListener(listener);
    }
	
	public void addListener(InvalidationListener listener) {
		helper.addListener(listener);
    }

	public void removeListener(InvalidationListener listener) {
		helper.removeListener(listener);
    }
	
	public class WrappedListListChangeListener implements ListChangeListener<E> {		
	    @Override
	    public void onChanged(ListChangeListener.Change<? extends E> change) {
	    	ListChangeWrapper wrappedChange = new ListChangeWrapper(wrappedList, change);
    		helper.fireValueChangedEvent(wrappedChange);
	    }
	}

	public class ListChangeWrapper extends ListChangeListener.Change<E> {            
	    private final ListChangeListener.Change<? extends E> wrappedChange;
	    
	    public ListChangeWrapper(ObservableList<E> list, ListChangeListener.Change<? extends E> wrappedChange) {
	        super(list);
	        this.wrappedChange = wrappedChange;
	    }

	    @Override
		public int getAddedSize() {
			return wrappedChange.getAddedSize();
		}

		@SuppressWarnings("unchecked")
		@Override
		public List<E> getAddedSubList() {
			return (List<E>)wrappedChange.getAddedSubList();
		}

		@Override
		public int getRemovedSize() {
			return wrappedChange.getRemovedSize();
		}

		@Override
		public boolean wasAdded() {
			return wrappedChange.wasAdded();
		}

		@Override
		public boolean wasPermutated() {
			return wrappedChange.wasPermutated();
		}

		@Override
		public boolean wasRemoved() {
			return wrappedChange.wasRemoved();
		}

		@Override
		public boolean wasReplaced() {
			return wrappedChange.wasReplaced();
		}

		@Override
		public boolean wasUpdated() {
			return wrappedChange.wasUpdated();
		}

		@Override
	    public int getFrom() {
			int from = wrappedChange.getFrom();
	        return from+first;
	    }

	    @Override
	    public int getTo() {
	    	int to = wrappedChange.getTo();
	        return to+first;
	    }

	    @Override
	    protected int[] getPermutation() {
	        // TODO
	        return new int[0]; // wrappedChange.getPermutation();
	    }

	    @Override
	    public int getPermutation(int num) {
	        return wrappedChange.getPermutation(num);
	    }

	    @SuppressWarnings("unchecked")
		@Override
	    public List<E> getRemoved() {
	        return (List<E>)wrappedChange.getRemoved();
	    }

	    @Override
	    public boolean next() {
	        return wrappedChange.next();
	    }

	    @Override
	    public void reset() {
	        wrappedChange.reset();
	    }        
	}
	
	
	@Override
	public Object[] toArray() {
		return wrappedList.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return wrappedList.toArray(a);
	}

	@Override
	public boolean add(E e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void add(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends E> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(E... arg0) {
		throw new UnsupportedOperationException();
	}
	
	@Override
	public boolean remove(Object o) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E remove(int index) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void remove(int arg0, int arg1) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean removeAll(E... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(E... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean setAll(E... arg0) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public E set(int index, E element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<E> subList(int fromIndex, int toIndex) {
		throw new UnsupportedOperationException();
	}
}
