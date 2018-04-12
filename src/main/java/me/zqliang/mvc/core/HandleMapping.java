package me.zqliang.mvc.core;

import java.lang.reflect.Method;

/**
 * 封装对象和方法
 * @author zqliang
 *
 */
public class HandleMapping {
	
	public HandleMapping(Object controller, Method method) {
		this.controller = controller;
		this.method		= method;
	}

	private Object controller;
	
	private Method method;

	public Object getController() {
		return controller;
	}

	public void setController(Object controller) {
		this.controller = controller;
	}

	public Method getMethod() {
		return method;
	}

	public void setMethod(Method method) {
		this.method = method;
	}
	
}
