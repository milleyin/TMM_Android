package com.qljl.tmmdp.modle.imageloader;


public interface  Cache<T> {
	
	public void addCacheItem(String key, T item);
	
	public T getCacheItem(String key);
	
	public void clear();
	
	public void removes(String key);
		
	public void Recycling();
}
