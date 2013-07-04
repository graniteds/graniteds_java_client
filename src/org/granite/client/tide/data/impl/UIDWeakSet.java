/*
  GRANITE DATA SERVICES
  Copyright (C) 2012 GRANITE DATA SERVICES S.A.S.

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

package org.granite.client.tide.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.WeakHashMap;

import org.granite.client.persistence.Persistence;
import org.granite.client.tide.data.UIDUtil;

/**
 *  Implementation of HashSet that holds weak references to UID entities 
 *  
 *  @author William DRAI
 */
public class UIDWeakSet {
    
    private WeakHashMap<Object, Object>[] table;
    
    
    public UIDWeakSet() {
        this(64);
    }
    
    @SuppressWarnings("unchecked")
    public UIDWeakSet(int capacity) {
        table = new WeakHashMap[capacity];  
    }
    
    public void clear() {
        for (int i = 0; i < table.length; i++)
            table[i] = null;
    }
    
    public Object put(Object uidObject) {
        int h = hash(UIDUtil.getCacheKey(uidObject));
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic == null) {
            dic = new WeakHashMap<Object, Object>();
            table[h] = dic;
        }
        
        Object old = null;
        for (Object o : dic.keySet()) {
            if (o == uidObject)
                return o;
            
            if (Persistence.getUid(o) == Persistence.getUid(uidObject) && o.getClass().getName().equals(uidObject.getClass().getName())) {
                old = o;
                dic.remove(o);
                break;
            }
        }
        
        dic.put(uidObject, null);
        
        return old;
    }
    
    public Object get(String cacheKey) {
        int h = hash(cacheKey);
        
        Object uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if (UIDUtil.getCacheKey(o).equals(cacheKey)) {
                    uidObject = o;
                    break;
                }
            }
        }
        
        return uidObject;
    }

    public static interface Matcher {
        
        public boolean match(Object o);
    }
    
    public Object find(Matcher matcher) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet()) {
                    if (matcher.match(o))
                        return o;
                }
            }
        }
        return null;
    }

    public static interface Operation {
        
        public void apply(Object o);
    }
    
    public void apply(Operation operation) {
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null) {
                for (Object o : dic.keySet())
                    operation.apply(o);
            }
        }
    }
    
    public Object remove(String cacheKey) {
        int h = hash(cacheKey);
        
        Object uidObject = null;
        
        WeakHashMap<Object, Object> dic = table[h];
        if (dic != null) {
            for (Object o : dic.keySet()) {
                if (UIDUtil.getCacheKey(o).equals(cacheKey)) {
                    uidObject = o;
                    dic.remove(o);
                    break;
                }
            }
        }
        
        return uidObject;
    }
    
    public int size() {
        int size = 0;
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                size += dic.size();
        }
        
        return size;
    }
    
    public List<Object> data() {
        List<Object> d = new ArrayList<Object>();
        
        for (int i = 0; i < table.length; i++) {
            WeakHashMap<Object, Object> dic = table[i];
            if (dic != null)
                d.addAll(dic.keySet());
        }
        return d;
    }
    
    private int hash(String uid) {
        int h = 0;
        int max = uid.length();
        for (int i = 0; i < max; i++)
            h = (31 * h) + uid.charAt(i);
        return (Math.abs(h) % table.length);
    }
}