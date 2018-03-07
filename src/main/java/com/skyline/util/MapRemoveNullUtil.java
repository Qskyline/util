package com.skyline.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.apache.commons.lang.StringUtils;

public class MapRemoveNullUtil {
    public static void removeNullEntry(Map<?, ?> map){
        removeNullKey(map);
        removeNullValue(map);
    }

    public static void removeNullKey(Map<?, ?> map){
        Set<?> set = map.keySet();
        for (Iterator<?> iterator = set.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            remove(obj, iterator);
        }
    }  

    public static void removeNullValue(Map<?, ?> map){
        Set set = map.keySet();
        for (Iterator<?> iterator = set.iterator(); iterator.hasNext();) {
            Object obj = iterator.next();
            Object value = map.get(obj);
            remove(value, iterator);
        }
    }

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
        Map<Object, String> map = new HashMap<>();
        map.put(1, "第1个值是数字");  
        map.put("2", "第2个值是字符串");  
        map.put(new String[]{"1","2"},"第3个值是数组");  
        map.put(new ArrayList<>(), "第4个值是List");
        map.put(new HashMap<>(), "Map 无值");
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