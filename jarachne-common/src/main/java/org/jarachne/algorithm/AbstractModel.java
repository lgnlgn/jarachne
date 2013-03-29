package org.jarachne.algorithm;

import java.util.Properties;

public abstract class AbstractModel {
		
	private AbstractModel(){};
	
	abstract public Object create(Object obj);
	abstract public Object fetch(Object obj);
	abstract public Object update(Object obj);
	abstract public Object delete(Object obj);
	abstract protected void init(Properties initProp);
	
	
	public static AbstractModel createModel(Class<? extends AbstractModel> clz, Properties initProp) throws Throwable{
		AbstractModel amodel = clz.newInstance();
		amodel.init(initProp);
		return amodel;
	}
}
