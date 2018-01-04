package com.skyline.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class MapRemoveNullUtil {
    /** 
     * 移除map中空key或者value空值 
     * @param map 
     */  
    public static void removeNullEntry(Map<?, ?> map){  
        removeNullKey(map);  
        removeNullValue(map);  
    }  
      
    /** 
     * 移除map的空key 
     * @param map 
     * @return 
     */  
    public static void removeNullKey(Map<?, ?> map){
        Set<?> set = map.keySet();
        for (Iterator<?> iterator = set.iterator(); iterator.hasNext();) {  
            Object obj = (Object)iterator.next();  
            remove(obj, iterator);  
        }  
    }  
      
    /** 
     * 移除map中的value空值 
     * @param map 
     * @return 
     */  
    public static void removeNullValue(Map<?, ?> map){  
        Set<?> set = map.keySet();
        for (Iterator<?> iterator = set.iterator(); iterator.hasNext();) {
            Object obj = (Object)iterator.next();  
            Object value =(Object)map.get(obj);  
            remove(value, iterator);
        }  
    }  
      
    /** 
     * Iterator 是工作在一个独立的线程中，并且拥有一个 mutex 锁。  
     * Iterator 被创建之后会建立一个指向原来对象的单链索引表，当原来的对象数量发生变化时，这个索引表的内容不会同步改变， 
     * 所以当索引指针往后移动的时候就找不到要迭代的对象，所以按照 fail-fast 原则 Iterator 会马上抛出 java.util.ConcurrentModificationException 异常。 
     * 所以 Iterator 在工作的时候是不允许被迭代的对象被改变的。 
     * 但你可以使用 Iterator 本身的方法 remove() 来删除对象， Iterator.remove() 方法会在删除当前迭代对象的同时维护索引的一致性。 
     * @param obj 
     * @param iterator 
     */  
    private static void remove(Object obj,Iterator<?> iterator){
    	if(obj == null){
    		iterator.remove();
    	} else if(obj instanceof String){
            String str = (String)obj;
            if(StringUtils.isEmpty(str)){
                iterator.remove();
            }
        } else if(obj instanceof Collection){  
        	Collection<?> col = (Collection<?>)obj;
            if(col.isEmpty()){
                iterator.remove();
            }              
        } else if(obj instanceof Map){  
            Map<?, ?> temp = (Map<?, ?>)obj;
            if(temp.isEmpty()){  
                iterator.remove();  
            }              
        } else if(obj instanceof Object[]){  
            Object[] array =(Object[])obj;
            if(array.length <= 0){  
                iterator.remove();  
            }
        }
    }
    
    public static void main(String[] args) { 
        Map<Object, String> map = new HashMap<Object, String>();  
        map.put(1, "第1个值是数字");  
        map.put("2", "第2个值是字符串");  
        map.put(new String[]{"1","2"},"第3个值是数组");  
        map.put(new ArrayList<Object>(), "第4个值是List");  
        map.put(new HashMap<Object, Object>(), "Map 无值");  
        map.put("5", "第5个");  
        map.put("6",null);  
        map.put("7", "");
        map.put("8", "  ");
        System.out.println(map);  
        MapRemoveNullUtil.removeNullKey(map); 
        System.out.println(map);  
        MapRemoveNullUtil.removeNullValue(map);  
        System.out.println(map);  
    }
}